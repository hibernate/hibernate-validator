/*
* JBoss, Home of Professional Open Source
* Copyright 2011, Red Hat, Inc. and/or its affiliates, and individual contributors
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.hibernate.validator.metadata;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.validator.metadata.constrained.ConstrainedElement;
import org.hibernate.validator.metadata.constrained.ConstrainedElement.ConstrainedElementKind;
import org.hibernate.validator.metadata.constrained.ConstrainedField;
import org.hibernate.validator.metadata.constrained.ConstrainedMethod;
import org.hibernate.validator.metadata.constrained.ConstrainedType;
import org.hibernate.validator.metadata.provider.AnnotationMetaDataProvider;
import org.hibernate.validator.metadata.provider.MetaDataProvider;
import org.hibernate.validator.util.ReflectionHelper;

import static org.hibernate.validator.util.CollectionHelper.newArrayList;
import static org.hibernate.validator.util.CollectionHelper.newHashMap;
import static org.hibernate.validator.util.CollectionHelper.newHashSet;

/**
 * <p>
 * This manager is in charge of providing all constraint related meta data
 * required by the validation engine.
 * </p>
 * <p>
 * Actual retrieval as meta data is delegated to {@link MetaDataProvider}
 * implementations which load meta-data based e.g. based on annotations or XML.
 * </p>
 * <p>
 * For performance reasons a cache is used which stores all meta data once
 * loaded for repeated retrieval. Upon initialization this cache is populated
 * with meta data provided by the given <i>eager</i> providers. If the cache
 * doesn't contain the meta data for a requested type it will be retrieved on
 * demand using the annotation based provider.
 * </p>
 *
 * @author Gunnar Morling
 */
public class BeanMetaDataManager {

	/**
	 * Default provider which is always used for meta data retrieval.
	 */
	private MetaDataProvider defaultProvider;

	/**
	 * Eager providers which are additionally used for meta data retrieval if
	 * the XML and/or programmatic configuration is used.
	 */
	private final List<MetaDataProvider> eagerMetaDataProviders;

	private final ConstraintHelper constraintHelper;

	/**
	 * Used to cache the constraint meta data for validated entities
	 */
	private final BeanMetaDataCache beanMetaDataCache;

	private AnnotationIgnores annotationIgnores;

	//TODO GM: can this be merged with cache?
	private Map<Class<?>, BeanConfiguration<?>> configurationsByClass;

	public BeanMetaDataManager(ConstraintHelper constraintHelper, MetaDataProvider... eagerMetaDataProviders) {
		this( constraintHelper, Arrays.asList( eagerMetaDataProviders ) );
	}

	/**
	 * @param constraintHelper
	 * @param eagerMetaDataProviders
	 */
	public BeanMetaDataManager(ConstraintHelper constraintHelper, List<MetaDataProvider> eagerMetaDataProviders) {

		this.constraintHelper = constraintHelper;
		this.eagerMetaDataProviders = eagerMetaDataProviders;

		configurationsByClass = newHashMap();
		beanMetaDataCache = new BeanMetaDataCache();

		loadConfigurationsFromEagerProviders();
		loadConfigurationsFromDefaultProvider();
		convertEagerConfigurationsToBeanMetaData();
	}

	private void loadConfigurationsFromEagerProviders() {

		//load meta data from eager providers
		for ( MetaDataProvider oneProvider : eagerMetaDataProviders ) {
			//TODO GM: merge, if also programmatic provider has this option
			if ( oneProvider.getAnnotationIgnores() != null ) {
				annotationIgnores = oneProvider.getAnnotationIgnores();
			}
			addOrMergeAll( oneProvider.getAllBeanConfigurations() );
		}

		if ( annotationIgnores == null ) {
			annotationIgnores = new AnnotationIgnores();
		}
	}

	private void loadConfigurationsFromDefaultProvider() {

		//load annotation meta data for eagerly configured types and their hierarchy
		Set<Class<?>> preconfiguredClasses = new HashSet<Class<?>>( configurationsByClass.keySet() );

		this.defaultProvider = new AnnotationMetaDataProvider( constraintHelper, annotationIgnores );
		//TODO GM: don't retrieve annotation meta data several times per type
		for ( Class<?> oneConfiguredClass : preconfiguredClasses ) {
			addOrMergeAll( defaultProvider.getBeanConfigurationForHierarchy( oneConfiguredClass ) );
		}
	}

	private void convertEagerConfigurationsToBeanMetaData() {
		//store eagerly loaded meta data in cache
		List<BeanMetaDataImpl<?>> allMetaData = configurationAsBeanMetaData();

		for ( BeanMetaDataImpl<?> oneBeanMetaData : allMetaData ) {
			registerWithCache( oneBeanMetaData );
		}
	}

	public <T> BeanMetaData<T> getBeanMetaData(Class<T> beanClass) {
		BeanMetaDataImpl<T> beanMetaData = beanMetaDataCache.getBeanMetaData( beanClass );
		if ( beanMetaData == null ) {

			addOrMergeAll( defaultProvider.getBeanConfigurationForHierarchy( beanClass ) );

			beanMetaData = mergeWithMetaDataFromHierarchy( getConfigurationForClass( beanClass ) );

			final BeanMetaDataImpl<T> cachedBeanMetaData = beanMetaDataCache.addBeanMetaData( beanClass, beanMetaData );
			if ( cachedBeanMetaData != null ) {
				beanMetaData = cachedBeanMetaData;
			}
		}
		return beanMetaData;
	}

	private <T> void registerWithCache(BeanMetaDataImpl<T> metaData) {
		beanMetaDataCache.addBeanMetaData( metaData.getBeanClass(), metaData );
	}

	private void addOrMergeAll(Iterable<BeanConfiguration<?>> configurations) {
		for ( BeanConfiguration<?> oneBeanConfiguration : configurations ) {
			addOrMerge( oneBeanConfiguration );
		}
	}

	private <T> void addOrMerge(BeanConfiguration<T> beanConfiguration) {

		BeanConfiguration<T> existingConfiguration = getConfigurationForClass( beanConfiguration.getBeanClass() );

		if ( existingConfiguration == null ) {
			configurationsByClass.put(
					beanConfiguration.getBeanClass(), beanConfiguration
			);
		}
		else {
			existingConfiguration.merge( beanConfiguration );
		}
	}

	@SuppressWarnings("unchecked")
	private <T> BeanConfiguration<T> getConfigurationForClass(Class<T> clazz) {
		return (BeanConfiguration<T>) configurationsByClass.get( clazz );
	}

	/**
	 * @return
	 */
	private List<BeanMetaDataImpl<?>> configurationAsBeanMetaData() {


		List<BeanMetaDataImpl<?>> theValue = newArrayList();

		for ( BeanConfiguration<?> oneConfiguration : configurationsByClass.values() ) {
			theValue.add( mergeWithMetaDataFromHierarchy( oneConfiguration ) );
		}

		return theValue;
	}

	private <T> BeanMetaDataImpl<T> mergeWithMetaDataFromHierarchy(BeanConfiguration<T> rootConfiguration) {

		Class<T> beanClass = rootConfiguration.getBeanClass();

		Set<BuilderDelegate> builders = newHashSet();

		for ( Class<?> oneHierarchyClass : ReflectionHelper.computeClassHierarchy( beanClass, true ) ) {

			BeanConfiguration<?> configurationForHierarchyClass = getConfigurationForClass( oneHierarchyClass );

			if ( configurationForHierarchyClass == null ) {
				continue;
			}

			for ( ConstrainedElement oneConstrainedElement : configurationForHierarchyClass.getConstrainableElements() ) {
				addMetaDataToBuilder( oneConstrainedElement, builders );
			}
		}

		Set<ConstraintMetaData> aggregatedElements = newHashSet();
		for ( BuilderDelegate oneBuilder : builders ) {
			aggregatedElements.addAll( oneBuilder.build() );
		}

		return new BeanMetaDataImpl<T>(
				beanClass,
				rootConfiguration.getDefaultGroupSequence(),
				rootConfiguration.getDefaultGroupSequenceProvider(),
				aggregatedElements
		);
	}

	private void addMetaDataToBuilder(ConstrainedElement constrainableElement, Set<BuilderDelegate> builders) {

		for ( BuilderDelegate oneBuilder : builders ) {
			boolean foundBuilder = oneBuilder.add( constrainableElement );

			if ( foundBuilder ) {
				return;
			}
		}

		builders.add( new BuilderDelegate( constrainableElement, constraintHelper ) );
	}

	private static class BuilderDelegate {

		private PropertyMetaData.Builder propertyBuilder;

		private MethodMetaData.Builder methodBuilder;

		public BuilderDelegate(ConstrainedElement constrainedElement, ConstraintHelper constraintHelper) {

			switch ( constrainedElement.getConstrainedElementKind() ) {

				case FIELD:

					ConstrainedField constrainedField = (ConstrainedField) constrainedElement;
					propertyBuilder = new PropertyMetaData.Builder( constrainedField, constraintHelper );
					break;

				case METHOD:

					ConstrainedMethod constrainedMethod = (ConstrainedMethod) constrainedElement;
					methodBuilder = new MethodMetaData.Builder( constrainedMethod );

					if ( constrainedMethod.isGetterMethod() ) {
						propertyBuilder = new PropertyMetaData.Builder( constrainedMethod, constraintHelper );
					}
					break;

				case TYPE:

					ConstrainedType constrainedType = (ConstrainedType) constrainedElement;
					propertyBuilder = new PropertyMetaData.Builder( constrainedType, constraintHelper );
					break;
			}
		}


		public boolean add(ConstrainedElement constrainedElement) {

			boolean added = false;

			if ( methodBuilder != null && methodBuilder.accepts( constrainedElement ) ) {
				methodBuilder.add( constrainedElement );
				added = true;
			}

			if ( propertyBuilder != null && propertyBuilder.accepts( constrainedElement ) ) {
				propertyBuilder.add( constrainedElement );

				if ( added == false && constrainedElement.getConstrainedElementKind() == ConstrainedElementKind.METHOD && methodBuilder == null ) {
					methodBuilder = new MethodMetaData.Builder( (ConstrainedMethod) constrainedElement );
				}

				added = true;
			}

			return added;
		}

		public Set<ConstraintMetaData> build() {

			Set<ConstraintMetaData> theValue = newHashSet();

			if ( propertyBuilder != null ) {
				theValue.add( propertyBuilder.build() );
			}

			if ( methodBuilder != null ) {
				theValue.add( methodBuilder.build() );
			}

			return theValue;
		}

	}

}
