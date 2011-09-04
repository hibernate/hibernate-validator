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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.validator.metadata.BeanMetaDataImpl.BeanMetaDataBuilder;
import org.hibernate.validator.metadata.constrained.BeanConfiguration;
import org.hibernate.validator.metadata.provider.AnnotationMetaDataProvider;
import org.hibernate.validator.metadata.provider.MetaDataProvider;
import org.hibernate.validator.util.ReflectionHelper;

import static org.hibernate.validator.util.CollectionHelper.newHashMap;
import static org.hibernate.validator.util.CollectionHelper.newHashSet;

/**
 * <p>
 * This manager is in charge of providing all constraint related meta data
 * required by the validation engine.
 * </p>
 * <p>
 * Actual retrieval of meta data is delegated to {@link MetaDataProvider}
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
	private final MetaDataProvider defaultProvider;

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

	private final Map<Class<?>, Set<BeanConfiguration<?>>> configurationsByClass;

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

		defaultProvider = new AnnotationMetaDataProvider( constraintHelper, annotationIgnores );
	}

	public <T> BeanMetaData<T> getBeanMetaData(Class<T> beanClass) {

		BeanMetaData<T> beanMetaData = beanMetaDataCache.getBeanMetaData( beanClass );

		if ( beanMetaData == null ) {

			addAll( defaultProvider.getBeanConfigurationForHierarchy( beanClass ) );
			beanMetaData = createBeanMetaData( beanClass );

			final BeanMetaData<T> cachedBeanMetaData = beanMetaDataCache.addBeanMetaData( beanClass, beanMetaData );
			if ( cachedBeanMetaData != null ) {
				beanMetaData = cachedBeanMetaData;
			}
		}

		return beanMetaData;
	}

	/**
	 * Creates a {@link BeanMetaData} containing the meta data from all meta
	 * data providers for the given type and its hierarchy.
	 *
	 * @param <T> The type of interest.
	 * @param clazz The type's class.
	 *
	 * @return A bean meta data object for the given type.
	 */
	private <T> BeanMetaDataImpl<T> createBeanMetaData(Class<T> clazz) {

		BeanMetaDataBuilder<T> builder = BeanMetaDataBuilder.getInstance( constraintHelper, clazz );

		for ( Class<?> oneHierarchyClass : ReflectionHelper.computeClassHierarchy( clazz, true ) ) {
			for ( BeanConfiguration<?> oneConfiguration : configurationsByClass.get( oneHierarchyClass ) ) {
				builder.add( oneConfiguration );
			}
		}

		return builder.build();
	}

	/**
	 * Loads all {@link BeanConfiguration}s from the registered eager meta data
	 * providers.
	 */
	private void loadConfigurationsFromEagerProviders() {

		for ( MetaDataProvider oneProvider : eagerMetaDataProviders ) {
			//TODO GM: merge, if also programmatic provider has this option
			if ( oneProvider.getAnnotationIgnores() != null ) {
				annotationIgnores = oneProvider.getAnnotationIgnores();
			}
			addAll( oneProvider.getAllBeanConfigurations() );
		}

		if ( annotationIgnores == null ) {
			annotationIgnores = new AnnotationIgnores();
		}
	}

	private void addAll(Set<BeanConfiguration<?>> configurations) {

		for ( BeanConfiguration<?> beanConfiguration : configurations ) {

			Set<BeanConfiguration<?>> configurationsForType = configurationsByClass.get( beanConfiguration.getBeanClass() );

			if ( configurationsForType == null ) {
				configurationsForType = newHashSet();
				configurationsByClass.put( beanConfiguration.getBeanClass(), configurationsForType );
			}

			configurationsForType.add( beanConfiguration );
		}
	}
}
