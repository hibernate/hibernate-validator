/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.manager;

import java.util.List;
import java.util.Optional;

import javax.validation.valueextraction.ValueExtractor;

import org.hibernate.validator.internal.engine.groups.ValidationOrderGenerator;
import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorManager;
import org.hibernate.validator.internal.metadata.aggregated.BeanMetaData;
import org.hibernate.validator.internal.metadata.aggregated.BeanMetaDataImpl;
import org.hibernate.validator.internal.metadata.aggregated.PropertyHolderBeanMetaDataBuilder;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.provider.PropertyHolderMetaDataProvider;
import org.hibernate.validator.internal.metadata.raw.propertyholder.PropertyHolderConfiguration;
import org.hibernate.validator.internal.util.TypeResolutionHelper;
import org.hibernate.validator.internal.util.stereotypes.Immutable;

/**
 * @author Marko Bekhta
 */
public class PropertyHolderBeanMetaDataProvider {

	@Immutable
	private final List<PropertyHolderMetaDataProvider> propertyHolderMetaDataProviderList;

	/**
	 * Helper for builtin constraints and their validator implementations
	 */
	private final ConstraintHelper constraintHelper;

	/**
	 * Used for resolving generic type information.
	 */
	private final TypeResolutionHelper typeResolutionHelper;

	/**
	 * The {@link ValueExtractor} manager.
	 */
	private final ValueExtractorManager valueExtractorManager;

	private final ValidationOrderGenerator validationOrderGenerator;

	private final MetaDataCache<PropertyHolderMetadataKey> metaDataCache;

	public PropertyHolderBeanMetaDataProvider(List<PropertyHolderMetaDataProvider> propertyHolderMetaDataProviderList, ConstraintHelper constraintHelper, TypeResolutionHelper typeResolutionHelper, ValueExtractorManager valueExtractorManager, ValidationOrderGenerator validationOrderGenerator) {
		this.propertyHolderMetaDataProviderList = propertyHolderMetaDataProviderList;
		this.constraintHelper = constraintHelper;
		this.typeResolutionHelper = typeResolutionHelper;
		this.valueExtractorManager = valueExtractorManager;
		this.validationOrderGenerator = validationOrderGenerator;

		this.metaDataCache = new MetaDataCache<>();
	}

	@SuppressWarnings("unchecked")
	public <T> BeanMetaData<T> getBeanMetaData(Class<T> propertyHolderClass, String mapping) {
		return (BeanMetaData<T>) metaDataCache.computeIfAbsent(
				new PropertyHolderMetadataKey( propertyHolderClass, mapping ),
				key -> createBeanMetaData( key )
		);
	}

	private <T> BeanMetaDataImpl<T> createBeanMetaData(PropertyHolderMetadataKey metadataKey) {
		PropertyHolderBeanMetaDataBuilder builder = PropertyHolderBeanMetaDataBuilder.getInstance(
				constraintHelper, typeResolutionHelper, valueExtractorManager, validationOrderGenerator, metadataKey.propertyHolderClass
		);

		for ( PropertyHolderMetaDataProvider metaDataProvider : propertyHolderMetaDataProviderList ) {
			Optional<PropertyHolderConfiguration> beanConfiguration = metaDataProvider.getBeanConfiguration( metadataKey.mapping );
			if ( beanConfiguration.isPresent() ) {
				builder.add( beanConfiguration.get() );
			}
		}
		return builder.build();
	}

	public void clear() {
		metaDataCache.clear();
	}

	public int numberOfCachedBeanMetaDataInstances() {
		return metaDataCache.size();
	}

	private static class PropertyHolderMetadataKey {
		private String mapping;
		private Class<?> propertyHolderClass;
		private int hashCode;

		public PropertyHolderMetadataKey(Class<?> propertyHolderClass, String mapping) {
			this.mapping = mapping;
			this.propertyHolderClass = propertyHolderClass;

			this.hashCode = buildHashCode();
		}

		private int buildHashCode() {
			int result = mapping.hashCode();
			result = 31 * result + propertyHolderClass.hashCode();
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
			if ( !propertyHolderClass.equals( that.propertyHolderClass ) ) {
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
