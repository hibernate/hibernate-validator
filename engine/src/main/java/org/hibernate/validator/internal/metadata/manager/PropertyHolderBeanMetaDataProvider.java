/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.manager;

import java.util.List;
import java.util.Optional;

import org.hibernate.validator.internal.metadata.aggregated.BeanMetaData;
import org.hibernate.validator.internal.metadata.aggregated.BeanMetaDataImpl;
import org.hibernate.validator.internal.metadata.provider.PropertyHolderMetaDataProvider;
import org.hibernate.validator.internal.metadata.raw.propertyholder.PropertyHolderConfiguration;
import org.hibernate.validator.internal.util.stereotypes.Immutable;

/**
 * @author Marko Bekhta
 */
public class PropertyHolderBeanMetaDataProvider {

	@Immutable
	private final List<PropertyHolderMetaDataProvider> propertyHolderMetaDataProviderList;

	private final MetaDataCache<PropertyHolderMetadataKey> metaDataCache;

	public PropertyHolderBeanMetaDataProvider(List<PropertyHolderMetaDataProvider> propertyHolderMetaDataProviderList) {
		this.propertyHolderMetaDataProviderList = propertyHolderMetaDataProviderList;

		this.metaDataCache = new MetaDataCache<>();
	}

	@SuppressWarnings("unchecked")
	public <T> BeanMetaData<T> getBeanMetaData(Class<T> beanClass, String mapping) {
		return (BeanMetaData<T>) metaDataCache.computeIfAbsent(
				new PropertyHolderMetadataKey( beanClass, mapping ),
				key -> createBeanMetaData( key )
		);
	}

	private <T> BeanMetaDataImpl<T> createBeanMetaData(PropertyHolderMetadataKey metadataKey) {
		for ( PropertyHolderMetaDataProvider metaDataProvider : propertyHolderMetaDataProviderList ) {
			Optional<PropertyHolderConfiguration> beanConfiguration = metaDataProvider.getBeanConfiguration( metadataKey.mapping );
			if ( beanConfiguration.isPresent() ) {

			}
		}
		return null;
	}

	private static class PropertyHolderMetadataKey {
		private String mapping;
		private Class<?> beanClass;
		private int hashCode;

		public PropertyHolderMetadataKey(Class<?> beanClass, String mapping) {
			this.mapping = mapping;
			this.beanClass = beanClass;

			this.hashCode = buildHashCode();
		}

		private int buildHashCode() {
			int result = mapping.hashCode();
			result = 31 * result + beanClass.hashCode();
			return result;
		}

		@Override
		public boolean equals(Object o) {
			if ( this == o ) {
				return true;
			}
			if ( o == null || getClass() != o.getClass() ) {
				return false;
			}

			PropertyHolderMetadataKey that = (PropertyHolderMetadataKey) o;

			if ( !mapping.equals( that.mapping ) ) {
				return false;
			}
			if ( !beanClass.equals( that.beanClass ) ) {
				return false;
			}

			return true;
		}

		@Override
		public int hashCode() {
			return hashCode;
		}
	}
}
