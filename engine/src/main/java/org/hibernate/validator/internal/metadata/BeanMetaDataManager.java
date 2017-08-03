/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata;

import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;
import static org.hibernate.validator.internal.util.ConcurrentReferenceHashMap.Option.IDENTITY_COMPARISONS;
import static org.hibernate.validator.internal.util.ConcurrentReferenceHashMap.ReferenceType.SOFT;
import static org.hibernate.validator.internal.util.logging.Messages.MESSAGES;

import java.util.EnumSet;
import java.util.List;

import javax.validation.valueextraction.ValueExtractor;

import org.hibernate.validator.internal.engine.MethodValidationConfiguration;
import org.hibernate.validator.internal.engine.groups.ValidationOrderGenerator;
import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorManager;
import org.hibernate.validator.internal.metadata.aggregated.BeanMetaData;
import org.hibernate.validator.internal.metadata.aggregated.BeanMetaDataImpl;
import org.hibernate.validator.internal.metadata.aggregated.BeanMetaDataImpl.BeanMetaDataBuilder;
import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptions;
import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptionsImpl;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.provider.AnnotationMetaDataProvider;
import org.hibernate.validator.internal.metadata.provider.MetaDataProvider;
import org.hibernate.validator.internal.metadata.raw.BeanConfiguration;
import org.hibernate.validator.internal.util.ConcurrentReferenceHashMap;
import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.ExecutableHelper;
import org.hibernate.validator.internal.util.ExecutableParameterNameProvider;
import org.hibernate.validator.internal.util.TypeResolutionHelper;
import org.hibernate.validator.internal.util.classhierarchy.ClassHierarchyHelper;

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
 * @author Chris Beckey &lt;cbeckey@paypal.com&gt;
 * @author Guillaume Smet
*/
public class BeanMetaDataManager {
	/**
	 * The default initial capacity for this cache.
	 */
	private static final int DEFAULT_INITIAL_CAPACITY = 16;

	/**
	 * The default load factor for this cache.
	 */
	private static final float DEFAULT_LOAD_FACTOR = 0.75f;

	/**
	 * The default concurrency level for this cache.
	 */
	private static final int DEFAULT_CONCURRENCY_LEVEL = 16;

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
	 * Used for resolving generic type information.
	 */
	private final TypeResolutionHelper typeResolutionHelper;

	/**
	 * The {@link ValueExtractor} manager.
	 */
	private final ValueExtractorManager valueExtractorManager;

	private final ExecutableParameterNameProvider parameterNameProvider;

	/**
	 * Used to cache the constraint meta data for validated entities
	 */
	private final ConcurrentReferenceHashMap<Class<?>, BeanMetaData<?>> beanMetaDataCache;

	/**
	 * Used for resolving type parameters. Thread-safe.
	 */
	private final ExecutableHelper executableHelper;

	private final ValidationOrderGenerator validationOrderGenerator;

	/**
	 * the three properties in this field affect the invocation of rules associated to section 4.5.5
	 * of the specification.  By default they are all false, if true they allow
	 * for relaxation of the Liskov Substitution Principal.
	 */
	private final MethodValidationConfiguration methodValidationConfiguration;

	public BeanMetaDataManager(ConstraintHelper constraintHelper,
			ExecutableHelper executableHelper,
			TypeResolutionHelper typeResolutionHelper,
			ExecutableParameterNameProvider parameterNameProvider,
			ValueExtractorManager valueExtractorManager,
			ValidationOrderGenerator validationOrderGenerator,
			List<MetaDataProvider> optionalMetaDataProviders,
			MethodValidationConfiguration methodValidationConfiguration) {
		this.constraintHelper = constraintHelper;
		this.executableHelper = executableHelper;
		this.typeResolutionHelper = typeResolutionHelper;
		this.valueExtractorManager = valueExtractorManager;
		this.parameterNameProvider = parameterNameProvider;
		this.validationOrderGenerator = validationOrderGenerator;

		this.metaDataProviders = newArrayList();
		this.metaDataProviders.addAll( optionalMetaDataProviders );

		this.methodValidationConfiguration = methodValidationConfiguration;

		this.beanMetaDataCache = new ConcurrentReferenceHashMap<>(
				DEFAULT_INITIAL_CAPACITY,
				DEFAULT_LOAD_FACTOR,
				DEFAULT_CONCURRENCY_LEVEL,
				SOFT,
				SOFT,
				EnumSet.of( IDENTITY_COMPARISONS )
		);

		AnnotationProcessingOptions annotationProcessingOptions = getAnnotationProcessingOptionsFromNonDefaultProviders();
		AnnotationMetaDataProvider defaultProvider = new AnnotationMetaDataProvider(
					constraintHelper,
					typeResolutionHelper,
					valueExtractorManager,
					annotationProcessingOptions
			);

		this.metaDataProviders.add( defaultProvider );
	}

	@SuppressWarnings("unchecked")
	public <T> BeanMetaData<T> getBeanMetaData(Class<T> beanClass) {
		Contracts.assertNotNull( beanClass, MESSAGES.beanTypeCannotBeNull() );

		BeanMetaData<T> beanMetaData = (BeanMetaData<T>) beanMetaDataCache.computeIfAbsent( beanClass,
				bc -> createBeanMetaData( bc ) );

		return beanMetaData;
	}

	public void clear() {
		beanMetaDataCache.clear();
	}

	public int numberOfCachedBeanMetaDataInstances() {
		return beanMetaDataCache.size();
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
		BeanMetaDataBuilder<T> builder = BeanMetaDataBuilder.getInstance(
				constraintHelper, executableHelper, typeResolutionHelper, valueExtractorManager, parameterNameProvider, validationOrderGenerator, clazz, methodValidationConfiguration );

		for ( MetaDataProvider provider : metaDataProviders ) {
			for ( BeanConfiguration<? super T> beanConfiguration : getBeanConfigurationForHierarchy( provider, clazz ) ) {
				builder.add( beanConfiguration );
			}
		}

		return builder.build();
	}

	/**
	 * @return returns the annotation ignores from the non annotation based meta data providers
	 */
	private AnnotationProcessingOptions getAnnotationProcessingOptionsFromNonDefaultProviders() {
		AnnotationProcessingOptions options = new AnnotationProcessingOptionsImpl();
		for ( MetaDataProvider metaDataProvider : metaDataProviders ) {
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
	private <T> List<BeanConfiguration<? super T>> getBeanConfigurationForHierarchy(MetaDataProvider provider, Class<T> beanClass) {
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
