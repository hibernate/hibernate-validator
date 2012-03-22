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
package org.hibernate.validator.internal.metadata;

import java.util.Arrays;
import java.util.List;

import org.hibernate.validator.internal.metadata.aggregated.BeanMetaData;
import org.hibernate.validator.internal.metadata.aggregated.BeanMetaDataImpl;
import org.hibernate.validator.internal.metadata.aggregated.BeanMetaDataImpl.BeanMetaDataBuilder;
import org.hibernate.validator.internal.metadata.core.AnnotationIgnores;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.provider.MetaDataProviderAnnotationSource;
import org.hibernate.validator.internal.metadata.provider.MetaDataProvider;
import org.hibernate.validator.internal.metadata.raw.BeanConfiguration;
import org.hibernate.validator.internal.util.SoftLimitMRUCache;

import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;

/**
 * This manager is in charge of providing all constraint related meta data
 * required by the validation engine.
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
	 * Additional metadata providers used for meta data retrieval if
	 * the XML and/or programmatic configuration is used.
	 */
	private final List<MetaDataProvider> metaDataProviders;

	/**
	 * Helper for builtin constraints and their validator implementations
	 */
	private final ConstraintHelper constraintHelper;

	/**
	 * Used to cache the constraint meta data for validated entities
	 */
	private final SoftLimitMRUCache<Class<?>, BeanMetaData<?>> beanMetaDataCache;

	public BeanMetaDataManager(ConstraintHelper constraintHelper, MetaDataProvider... metaDataProviders) {
		this( constraintHelper, Arrays.asList( metaDataProviders ) );
	}

	/**
	 * @param constraintHelper the constraint helper
	 * @param optionalMetaDataProviders optional meta data provider used on top of the annotation based provider
	 */
	public BeanMetaDataManager(ConstraintHelper constraintHelper, List<MetaDataProvider> optionalMetaDataProviders) {
		this.constraintHelper = constraintHelper;
		this.metaDataProviders = newArrayList();
		this.metaDataProviders.addAll( optionalMetaDataProviders );
		this.beanMetaDataCache = new SoftLimitMRUCache<Class<?>, BeanMetaData<?>>();
		AnnotationIgnores annotationIgnores = getAnnotationIgnoresNonDefaultProviders();
		MetaDataProviderAnnotationSource defaultProvider = new MetaDataProviderAnnotationSource(
				constraintHelper,
				annotationIgnores
		);
		this.metaDataProviders.add( defaultProvider );
	}

	@SuppressWarnings("unchecked")
	public <T> BeanMetaData<T> getBeanMetaData(Class<T> beanClass) {
		BeanMetaData<T> beanMetaData = (BeanMetaData<T>) beanMetaDataCache.get( beanClass );

		// create a new BeanMetaData in case none is cached
		if ( beanMetaData == null ) {
			beanMetaData = createBeanMetaData( beanClass );

			final BeanMetaData<T> cachedBeanMetaData = (BeanMetaData<T>) beanMetaDataCache.put(
					beanClass,
					beanMetaData
			);
			if ( cachedBeanMetaData != null ) {
				beanMetaData = cachedBeanMetaData;
			}
		}

		return beanMetaData;
	}

	/**
	 * Creates a {@link org.hibernate.validator.internal.metadata.aggregated.BeanMetaData} containing the meta data from all meta
	 * data providers for the given type and its hierarchy.
	 *
	 * @param <T> The type of interest.
	 * @param clazz The type's class.
	 *
	 * @return A bean meta data object for the given type.
	 */
	private <T> BeanMetaDataImpl<T> createBeanMetaData(Class<T> clazz) {
		BeanMetaDataBuilder<T> builder = BeanMetaDataBuilder.getInstance( constraintHelper, clazz );

		for ( MetaDataProvider provider : metaDataProviders ) {
			for ( BeanConfiguration<?> beanConfiguration : provider.getBeanConfigurationForHierarchy( clazz ) ) {
				builder.add( beanConfiguration );
			}
		}

		return builder.build();
	}

	/**
	 * @return returns the annotation ignores from the non annotation based meta data providers
	 */
	private AnnotationIgnores getAnnotationIgnoresNonDefaultProviders() {
		AnnotationIgnores ignores = null;
		for ( MetaDataProvider metaDataProvider : metaDataProviders ) {
			//TODO GM: merge, if also programmatic provider has this option
			if ( metaDataProvider.getAnnotationIgnores() != null ) {
				ignores = metaDataProvider.getAnnotationIgnores();
			}
		}

		if ( ignores == null ) {
			ignores = new AnnotationIgnores();
		}
		return ignores;
	}
}
