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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.validator.engine.HibernateConstrainedType;
import org.hibernate.validator.internal.engine.ConstraintCreationContext;
import org.hibernate.validator.internal.engine.MethodValidationConfiguration;
import org.hibernate.validator.internal.engine.constrainedtype.JavaBeanConstrainedType;
import org.hibernate.validator.internal.engine.constrainedtype.NormalizedJavaBeanConstrainedType;
import org.hibernate.validator.internal.engine.groups.ValidationOrderGenerator;
import org.hibernate.validator.internal.metadata.aggregated.BeanMetaData;
import org.hibernate.validator.internal.metadata.aggregated.BeanMetaDataBuilder;
import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptions;
import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptionsImpl;
import org.hibernate.validator.internal.metadata.provider.AnnotationMetaDataProvider;
import org.hibernate.validator.internal.metadata.provider.MetaDataProvider;
import org.hibernate.validator.internal.metadata.raw.BeanConfiguration;
import org.hibernate.validator.internal.properties.javabean.JavaBeanHelper;
import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.ExecutableHelper;
import org.hibernate.validator.internal.util.ExecutableParameterNameProvider;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.metadata.BeanMetaDataClassNormalizer;

public class PredefinedScopeBeanMetaDataManager implements BeanMetaDataManager {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private final BeanMetaDataClassNormalizer beanMetaDataClassNormalizer;

	/**
	 * Used to cache the constraint meta data for validated entities
	 */
	private final Map<HibernateConstrainedType<?>, BeanMetaData<?>> beanMetaDataMap;

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

		Map<HibernateConstrainedType<?>, BeanMetaData<?>> tmpBeanMetadataMap = new HashMap<>();

		for ( Class<?> validatedClass : beanClassesToInitialize ) {
			JavaBeanConstrainedType constrainedType = new NormalizedJavaBeanConstrainedType( beanMetaDataClassNormalizer, beanMetaDataClassNormalizer.normalize( validatedClass ) );
			BeanMetaData<?> beanMetaData = createBeanMetaData( constraintCreationContext, executableHelper, parameterNameProvider,
					javaBeanHelper, validationOrderGenerator, methodValidationConfiguration,
					metaDataProviders, tmpBeanMetadataMap, constrainedType );

			tmpBeanMetadataMap.put( beanMetaData.getConstrainedType(), beanMetaData );
			for ( BeanMetaData<?> metaData : beanMetaData.getBeanMetadataHierarchy() ) {
				tmpBeanMetadataMap.put( metaData.getConstrainedType(), metaData );
			}
		}

		this.beanMetaDataMap = CollectionHelper.toImmutableMap( tmpBeanMetadataMap );

		this.beanMetaDataClassNormalizer = beanMetaDataClassNormalizer;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> BeanMetaData<T> getBeanMetaData(HibernateConstrainedType<T> constrainedType) {
		HibernateConstrainedType<T> key = constrainedType;
		if ( constrainedType instanceof JavaBeanConstrainedType ) {
			key = ( (JavaBeanConstrainedType<T>) constrainedType ).normalize( beanMetaDataClassNormalizer );
		}
		BeanMetaData<T> beanMetaData = (BeanMetaData<T>) beanMetaDataMap.get( key );
		if ( beanMetaData == null ) {
			throw LOG.uninitializedBeanMetaData( constrainedType.getActuallClass() );
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
	 * @param beanMetaDataCache The temporary map that might have some of the requried metadata already created
	 * @param constrainedType The type's class.
	 *
	 * @return A bean meta data object for the given type.
	 */
	private static <T> BeanMetaData<T> createBeanMetaData(ConstraintCreationContext constraintCreationContext,
			ExecutableHelper executableHelper,
			ExecutableParameterNameProvider parameterNameProvider,
			JavaBeanHelper javaBeanHelper,
			ValidationOrderGenerator validationOrderGenerator,
			MethodValidationConfiguration methodValidationConfiguration,
			List<MetaDataProvider> metaDataProviders,
			Map<HibernateConstrainedType<?>, BeanMetaData<?>> beanMetaDataCache,
			HibernateConstrainedType<T> constrainedType) {
		List<HibernateConstrainedType<? super T>> hierarchy = constrainedType.getHierarchy();

		if ( hierarchy.isEmpty() ) {
			// it means that our `constrained type is an interface and we don't care about super-type bean metadata.
			// can happen if not a real class is passed for validation but for example a Validator#getConstraintsForClass(Class<?>)
			// is called
			hierarchy = Collections.singletonList( constrainedType );
		}

		List<BeanMetaData<?>> list = new ArrayList<>( hierarchy.size() );
		for ( int index = hierarchy.size() - 1; index > -1; index-- ) {
			HibernateConstrainedType<? super T> type = hierarchy.get( index );

			// we skip interfaces if any occur, unless constrained type is an interface itself...
			if ( !constrainedType.equals( type ) && type.isInterface() ) {
				continue;
			}

			list.add( 0, beanMetaDataCache.computeIfAbsent(
					type,
					cType -> findSingleBeanMetaData( constraintCreationContext, executableHelper, parameterNameProvider,
							validationOrderGenerator, methodValidationConfiguration,
							metaDataProviders, type, list )
					)
			);
		}

		return (BeanMetaData<T>) list.get( 0 );
	}

	private static <T> BeanMetaData<T> findSingleBeanMetaData(ConstraintCreationContext constraintCreationContext,
			ExecutableHelper executableHelper,
			ExecutableParameterNameProvider parameterNameProvider,
			ValidationOrderGenerator validationOrderGenerator,
			MethodValidationConfiguration methodValidationConfiguration,
			List<MetaDataProvider> metaDataProviders,
			HibernateConstrainedType<T> constrainedType,
			List<BeanMetaData<?>> hierarchy) {
		BeanMetaDataBuilder<T> builder = BeanMetaDataBuilder.getInstance(
				constraintCreationContext, executableHelper, parameterNameProvider,
				validationOrderGenerator, constrainedType, methodValidationConfiguration );

		for ( MetaDataProvider provider : metaDataProviders ) {
			for ( BeanConfiguration<? super T> beanConfiguration : getBeanConfigurationForHierarchy( provider, constrainedType ) ) {
				builder.add( beanConfiguration );
			}
		}
		return builder.build( hierarchy );
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
	 * @param constrainedType The type of interest.
	 * @param <T> The type of the class to get the configurations for.
	 *
	 * @return A set with the configurations for the complete hierarchy of the given type. May be empty, but never
	 * {@code null}.
	 */
	private static <T> List<BeanConfiguration<? super T>> getBeanConfigurationForHierarchy(MetaDataProvider provider, HibernateConstrainedType<T> constrainedType) {
		List<BeanConfiguration<? super T>> configurations = newArrayList();

		for ( HibernateConstrainedType<? super T> clazz : constrainedType.getHierarchy() ) {
			BeanConfiguration<? super T> configuration = provider.getBeanConfiguration( clazz );
			if ( configuration != null ) {
				configurations.add( configuration );
			}
		}

		return configurations;
	}
}
