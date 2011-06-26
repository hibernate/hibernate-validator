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

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.validator.metadata.AggregatedMethodMetaData.Builder;
import org.hibernate.validator.metadata.provider.AnnotationMetaDataProvider;
import org.hibernate.validator.metadata.provider.MetaDataProvider;
import org.hibernate.validator.util.ReflectionHelper;

import static org.hibernate.validator.util.CollectionHelper.newArrayList;
import static org.hibernate.validator.util.CollectionHelper.newHashMap;
import static org.hibernate.validator.util.CollectionHelper.newHashSet;

/**
 * @author Gunnar Morling
 */
public class BeanMetaDataManager {

	private final List<MetaDataProvider> eagerMetaDataProviders;

	private final ConstraintHelper constraintHelper;

	/**
	 * Used to cache the constraint meta data for validated entities
	 */
	private final BeanMetaDataCache beanMetaDataCache;

	private AnnotationIgnores annotationIgnores;

	//TODO GM: can this be merged with cache?
	private Map<Class<?>, BeanConfiguration<?>> configurationsByClass;

	/**
	 * @param constraintHelper
	 * @param beanMetaDataCache
	 * @param mappingStreams
	 * @param mapping
	 */
	public BeanMetaDataManager(ConstraintHelper constraintHelper, List<MetaDataProvider> eagerMetaDataProviders) {

		this.constraintHelper = constraintHelper;
		this.eagerMetaDataProviders = eagerMetaDataProviders;

		configurationsByClass = newHashMap();
		beanMetaDataCache = new BeanMetaDataCache();

		cacheMetaDataFromEagerProviders();
	}

	private void cacheMetaDataFromEagerProviders() {

		//load meta data from eager providers
		for ( MetaDataProvider oneProvider : eagerMetaDataProviders ) {
			//TODO GM: merge, if also programmatic provider has this option
			if ( oneProvider.getAnnotationIgnores() != null ) {
				annotationIgnores = oneProvider.getAnnotationIgnores();
			}
			for ( BeanConfiguration<?> oneConfiguration : oneProvider.getAllBeanConfigurations() ) {
				addOrMerge( oneConfiguration );
			}
		}

		//load annotation meta data for eagerly configured types and their hierarchy

		Set<BeanConfiguration<?>> annotationConfigurations = newHashSet();

		//TODO GM: don't retrieve annotation meta data several times per type
		for ( Class<?> oneConfiguredClass : configurationsByClass.keySet() ) {
			for ( Class<?> oneHierarchyClass : ReflectionHelper.computeClassHierarchy( oneConfiguredClass, true ) ) {
				MetaDataProvider annotationMetaDataProvider = new AnnotationMetaDataProvider(
						constraintHelper, oneHierarchyClass
				);
				for ( BeanConfiguration<?> oneConfiguration : annotationMetaDataProvider.getAllBeanConfigurations() ) {
					annotationConfigurations.add( oneConfiguration );
				}
			}
		}

		for ( BeanConfiguration<?> beanConfiguration : annotationConfigurations ) {
			addOrMerge( beanConfiguration );
		}

		//store eagerly loaded meta data in cache
		List<BeanMetaDataImpl<?>> allMetaData = configurationAsBeanMetaData();

		for ( BeanMetaDataImpl<?> oneBeanMetaData : allMetaData ) {
			registerWithCache( oneBeanMetaData );
		}
	}

	public <T> BeanMetaData<T> getBeanMetaData(Class<T> beanClass) {
		BeanMetaDataImpl<T> beanMetaData = beanMetaDataCache.getBeanMetaData( beanClass );
		if ( beanMetaData == null ) {
			for ( Class<?> oneHierarchyClass : ReflectionHelper.computeClassHierarchy( beanClass, true ) ) {
				MetaDataProvider annotationMetaDataProvider = new AnnotationMetaDataProvider(
						constraintHelper, oneHierarchyClass
				);
				for ( BeanConfiguration<?> oneConfiguration : annotationMetaDataProvider.getAllBeanConfigurations() ) {
					addOrMerge( oneConfiguration );
				}
			}

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

		Set<Member> allCascadedMembers = newHashSet();
		Map<Class<?>, List<BeanMetaConstraint<?>>> allConstraints = newHashMap();
		Set<AggregatedMethodMetaData.Builder> builders = newHashSet();

		for ( Class<?> oneHierarchyClass : ReflectionHelper.computeClassHierarchy( beanClass, true ) ) {

			BeanConfiguration<?> configurationForHierarchyClass = getConfigurationForClass( oneHierarchyClass );

			if ( configurationForHierarchyClass == null ) {
				continue;
			}

			for ( MethodMetaData oneMethodMetaData : configurationForHierarchyClass.getMethodMetaData() ) {
				addMetaDataToBuilder( oneMethodMetaData, builders );

			}
			List<BeanMetaConstraint<?>> adaptedConstraints = newArrayList();
			for ( BeanMetaConstraint<?> beanMetaConstraint : configurationForHierarchyClass.getConstraints() ) {
				adaptedConstraints.add( adaptOrigin( beanMetaConstraint, beanClass ) );
			}

			allConstraints.put( oneHierarchyClass, adaptedConstraints );
			allCascadedMembers.addAll( configurationForHierarchyClass.getCascadedMembers() );
		}

		Set<AggregatedMethodMetaData> allMethodMetaData = newHashSet();
		for ( Builder oneBuilder : builders ) {
			allMethodMetaData.add( oneBuilder.build() );
		}

		return new BeanMetaDataImpl<T>(
				beanClass,
				constraintHelper,
				rootConfiguration.getDefaultGroupSequence(),
				rootConfiguration.getDefaultGroupSequenceProvider(),
				allConstraints,
				allMethodMetaData,
				allCascadedMembers,
				annotationIgnores != null ? annotationIgnores : new AnnotationIgnores(),
				beanMetaDataCache
		);
	}

	private void addMetaDataToBuilder(MethodMetaData methodMetaData, Set<AggregatedMethodMetaData.Builder> builders) {
		for ( AggregatedMethodMetaData.Builder OneBuilder : builders ) {
			if ( OneBuilder.accepts( methodMetaData ) ) {
				OneBuilder.addMetaData( methodMetaData );
				return;
			}
		}
		AggregatedMethodMetaData.Builder builder = new AggregatedMethodMetaData.Builder( methodMetaData );
		builders.add( builder );
	}

	private <A extends Annotation> BeanMetaConstraint<A> adaptOrigin(BeanMetaConstraint<A> constraint, Class<?> beanClass) {

		ConstraintOrigin definedIn = definedIn( beanClass, constraint.getLocation().getBeanClass() );

		if ( definedIn == ConstraintOrigin.DEFINED_LOCALLY ) {
			return constraint;
		}

		ConstraintDescriptorImpl<A> descriptor = new ConstraintDescriptorImpl<A>(
				(A) constraint.getDescriptor().getAnnotation(),
				constraintHelper,
				constraint.getElementType(),
				definedIn
		);

		return new BeanMetaConstraint<A>(
				descriptor,
				constraint.getLocation().getBeanClass(),
				constraint.getLocation().getMember()
		);
	}

	/**
	 * @param rootClass The root class. That is the class for which we currently create a  {@code BeanMetaData}
	 * @param hierarchyClass The class on which the current constraint is defined on
	 *
	 * @return Returns {@code ConstraintOrigin.DEFINED_LOCALLY} if the constraint was defined on the root bean,
	 *         {@code ConstraintOrigin.DEFINED_IN_HIERARCHY} otherwise.
	 */
	private ConstraintOrigin definedIn(Class<?> rootClass, Class<?> hierarchyClass) {
		if ( hierarchyClass.equals( rootClass ) ) {
			return ConstraintOrigin.DEFINED_LOCALLY;
		}
		else {
			return ConstraintOrigin.DEFINED_IN_HIERARCHY;
		}
	}

}
