/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata;

import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.validator.internal.engine.ConstraintCreationContext;
import org.hibernate.validator.internal.engine.MethodValidationConfiguration;
import org.hibernate.validator.internal.engine.groups.ValidationOrderGenerator;
import org.hibernate.validator.internal.metadata.aggregated.BeanMetaData;
import org.hibernate.validator.internal.metadata.aggregated.BeanMetaDataBuilder;
import org.hibernate.validator.internal.metadata.aggregated.BeanMetaDataImpl;
import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptions;
import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptionsImpl;
import org.hibernate.validator.internal.metadata.provider.AnnotationMetaDataProvider;
import org.hibernate.validator.internal.metadata.provider.MetaDataProvider;
import org.hibernate.validator.internal.metadata.raw.BeanConfiguration;
import org.hibernate.validator.internal.properties.javabean.JavaBeanHelper;
import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.ExecutableHelper;
import org.hibernate.validator.internal.util.ExecutableParameterNameProvider;
import org.hibernate.validator.internal.util.classhierarchy.ClassHierarchyHelper;
import org.hibernate.validator.internal.util.classhierarchy.Filters;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.metadata.BeanMetaDataClassNormalizer;

public class PredefinedScopeBeanMetaDataManager implements BeanMetaDataManager {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private final BeanMetaDataClassNormalizer beanMetaDataClassNormalizer;

	/**
	 * Used to cache the constraint meta data for validated entities
	 */
	private final Map<Class<?>, BeanMetaData<?>> beanMetaDataMap;

	public PredefinedScopeBeanMetaDataManager(ConstraintCreationContext constraintCreationContext,
			ExecutableHelper executableHelper,
			ExecutableParameterNameProvider parameterNameProvider,
			JavaBeanHelper javaBeanHelper,
			ValidationOrderGenerator validationOrderGenerator,
			List<MetaDataProvider> optionalMetaDataProviders,
			MethodValidationConfiguration methodValidationConfiguration,
			BeanMetaDataClassNormalizer beanMetaDataClassNormalizer,
			Set<Class<?>> beanClassesToInitialize) {
		AnnotationProcessingOptions annotationProcessingOptions = getAnnotationProcessingOptionsFromNonDefaultProviders( optionalMetaDataProviders );
		AnnotationMetaDataProvider defaultProvider = new AnnotationMetaDataProvider(
				constraintCreationContext,
				javaBeanHelper,
				annotationProcessingOptions
		);

		List<MetaDataProvider> metaDataProviders = new ArrayList<>( optionalMetaDataProviders.size() + 1 );
		// We add the annotation based metadata provider at the first position so that the entire metadata model is assembled
		// first.
		// The other optional metadata providers will then contribute their additional metadata to the preexisting model.
		// This helps to mitigate issues like HV-1450.
		metaDataProviders.add( defaultProvider );
		metaDataProviders.addAll( optionalMetaDataProviders );

		Map<Class<?>, BeanMetaData<?>> tmpBeanMetadataMap = new HashMap<>();

		for ( Class<?> validatedClass : beanClassesToInitialize ) {
			BeanMetaData<?> beanMetaData = createBeanMetaData( this, constraintCreationContext, executableHelper, parameterNameProvider,
					javaBeanHelper, validationOrderGenerator, optionalMetaDataProviders, methodValidationConfiguration,
					metaDataProviders, validatedClass );

			tmpBeanMetadataMap.put( validatedClass, beanMetaData );
			for ( BeanMetaData<?> metaData : beanMetaData.getBeanMetadataHierarchy() ) {
				tmpBeanMetadataMap.put( metaData.getBeanClass(), metaData );
			}
		}

		this.beanMetaDataMap = CollectionHelper.toImmutableMap( tmpBeanMetadataMap );

		this.beanMetaDataClassNormalizer = beanMetaDataClassNormalizer;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> BeanMetaData<T> getBeanMetaData(Class<T> beanClass) {
		BeanMetaData<T> beanMetaData = (BeanMetaData<T>) beanMetaDataMap.get( beanMetaDataClassNormalizer.normalize( beanClass ) );
		if ( beanMetaData == null ) {
			throw LOG.uninitializedBeanMetaData( beanClass );
		}
		return beanMetaData;
	}

	@Override
	public void clear() {
		beanMetaDataMap.clear();
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
	private static <T> BeanMetaDataImpl<T> createBeanMetaData(BeanMetaDataManager beanMetaDataManager,
			ConstraintCreationContext constraintCreationContext,
			ExecutableHelper executableHelper,
			ExecutableParameterNameProvider parameterNameProvider,
			JavaBeanHelper javaBeanHelper,
			ValidationOrderGenerator validationOrderGenerator,
			List<MetaDataProvider> optionalMetaDataProviders,
			MethodValidationConfiguration methodValidationConfiguration,
			List<MetaDataProvider> metaDataProviders,
			Class<T> clazz) {
		BeanMetaDataBuilder<T> builder = BeanMetaDataBuilder.getInstance(
				constraintCreationContext, executableHelper, parameterNameProvider,
				validationOrderGenerator, clazz, methodValidationConfiguration );

		for ( MetaDataProvider provider : metaDataProviders ) {
			for ( BeanConfiguration<? super T> beanConfiguration : getBeanConfigurationForHierarchy( provider, clazz ) ) {
				builder.add( beanConfiguration );
			}
		}

		return builder.build( beanMetaDataManager );
	}

	/**
	 * @return returns the annotation ignores from the non annotation based meta data providers
	 */
	private static AnnotationProcessingOptions getAnnotationProcessingOptionsFromNonDefaultProviders(List<MetaDataProvider> optionalMetaDataProviders) {
		AnnotationProcessingOptions options = new AnnotationProcessingOptionsImpl();
		for ( MetaDataProvider metaDataProvider : optionalMetaDataProviders ) {
			options.merge( metaDataProvider.getAnnotationProcessingOptions() );
		}

		return options;
	}

	/**
	 * Returns a list with the configurations for all types contained in the given type's hierarchy (including
	 * implemented interfaces) starting at the specified type.
	 *
	 * @param beanClass The type of interest.
	 * @param <T> The type of the class to get the configurations for.
	 * @return A set with the configurations for the complete hierarchy of the given type. May be empty, but never
	 * {@code null}.
	 */
	private static <T> List<BeanConfiguration<? super T>> getBeanConfigurationForHierarchy(MetaDataProvider provider, Class<T> beanClass) {
		List<BeanConfiguration<? super T>> configurations = newArrayList();

		for ( Class<? super T> clazz : ClassHierarchyHelper.getHierarchy( beanClass ) ) {
			BeanConfiguration<? super T> configuration = provider.getBeanConfiguration( clazz );
			if ( configuration != null ) {
				configurations.add( configuration );
			}
		}

		return configurations;
	}
}
