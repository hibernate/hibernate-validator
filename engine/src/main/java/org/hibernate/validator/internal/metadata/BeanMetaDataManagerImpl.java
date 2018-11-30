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

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import org.hibernate.validator.engine.HibernateConstrainedType;
import org.hibernate.validator.internal.engine.ConstraintCreationContext;
import org.hibernate.validator.internal.engine.MethodValidationConfiguration;
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
import org.hibernate.validator.internal.util.ConcurrentReferenceHashMap;
import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.ExecutableHelper;
import org.hibernate.validator.internal.util.ExecutableParameterNameProvider;
import org.hibernate.validator.internal.util.stereotypes.Immutable;

/**
 * This manager is in charge of providing all constraint related meta data
 * required by the validation engine.
 * <p>
 * Actual retrieval of meta data is delegated to {@link MetaDataProvider}
 * implementations which load meta-data based e.g. based on annotations or XML.
 * <p>
 * For performance reasons a cache is used which stores all meta data once
 * loaded for repeated retrieval. Upon initialization this cache is populated
 * with meta data provided by the given <i>eager</i> providers. If the cache
 * doesn't contain the meta data for a requested type it will be retrieved on
 * demand using the annotation based provider.
 *
 * @author Gunnar Morling
 * @author Chris Beckey &lt;cbeckey@paypal.com&gt;
 * @author Guillaume Smet
 */
public class BeanMetaDataManagerImpl implements BeanMetaDataManager {
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
	@Immutable
	private final List<MetaDataProvider> metaDataProviders;

	/**
	 * The constraint creation context containing all the helpers necessary to the constraint creation.
	 */
	private final ConstraintCreationContext constraintCreationContext;

	private final ExecutableParameterNameProvider parameterNameProvider;

	/**
	 * Used to cache the constraint meta data for validated entities
	 */
	private final ConcurrentReferenceHashMap<HibernateConstrainedType<?>, BeanMetaData<?>> beanMetaDataCache;

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

	public BeanMetaDataManagerImpl(ConstraintCreationContext constraintCreationContext,
			ExecutableHelper executableHelper,
			ExecutableParameterNameProvider parameterNameProvider,
			JavaBeanHelper javaBeanHelper,
			ValidationOrderGenerator validationOrderGenerator,
			List<MetaDataProvider> optionalMetaDataProviders,
			MethodValidationConfiguration methodValidationConfiguration) {
		this.constraintCreationContext = constraintCreationContext;
		this.executableHelper = executableHelper;
		this.parameterNameProvider = parameterNameProvider;
		this.validationOrderGenerator = validationOrderGenerator;

		this.methodValidationConfiguration = methodValidationConfiguration;

		this.beanMetaDataCache = new ConcurrentReferenceHashMap<>(
				DEFAULT_INITIAL_CAPACITY,
				DEFAULT_LOAD_FACTOR,
				DEFAULT_CONCURRENCY_LEVEL,
				SOFT,
				SOFT,
				EnumSet.of( IDENTITY_COMPARISONS )
		);

		AnnotationProcessingOptions annotationProcessingOptions = getAnnotationProcessingOptionsFromNonDefaultProviders( optionalMetaDataProviders );
		AnnotationMetaDataProvider defaultProvider = new AnnotationMetaDataProvider(
				constraintCreationContext,
				javaBeanHelper,
				annotationProcessingOptions
		);
		List<MetaDataProvider> tmpMetaDataProviders = new ArrayList<>( optionalMetaDataProviders.size() + 1 );
		// We add the annotation based metadata provider at the first position so that the entire metadata model is assembled
		// first.
		// The other optional metadata providers will then contribute their additional metadata to the preexisting model.
		// This helps to mitigate issues like HV-1450.
		tmpMetaDataProviders.add( defaultProvider );
		tmpMetaDataProviders.addAll( optionalMetaDataProviders );

		this.metaDataProviders = CollectionHelper.toImmutableList( tmpMetaDataProviders );
	}

	@SuppressWarnings("unchecked")
	public <T> BeanMetaData<T> getBeanMetaData(HibernateConstrainedType<T> constrainedType) {
		Contracts.assertNotNull( constrainedType, MESSAGES.beanTypeCannotBeNull() );

		// First, let's do a simple lookup as it's the default case
		BeanMetaData<T> beanMetaData = (BeanMetaData<T>) beanMetaDataCache.get( constrainedType );

		if ( beanMetaData != null ) {
			return beanMetaData;
		}

		beanMetaData = createBeanMetaData( constrainedType );
		BeanMetaData<T> previousBeanMetaData = (BeanMetaData<T>) beanMetaDataCache.putIfAbsent( constrainedType, beanMetaData );

		// we return the previous value if not null
		if ( previousBeanMetaData != null ) {
			return previousBeanMetaData;
		}

		return beanMetaData;
	}

	@Override
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
	 * @param constrainedType The type's class.
	 *
	 * @return A bean meta data object for the given type.
	 */
	private <T> BeanMetaData<T> createBeanMetaData(HibernateConstrainedType<T> constrainedType) {
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
			list.add( 0, beanMetaDataCache.computeIfAbsent( type, cType -> findSingleBeanMetaData( cType, list ) ) );
		}

		return (BeanMetaData<T>) list.get( 0 );
	}

	private <T> BeanMetaData<T> findSingleBeanMetaData(HibernateConstrainedType<T> constrainedType, List<BeanMetaData<?>> hierarchy) {
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
	private AnnotationProcessingOptions getAnnotationProcessingOptionsFromNonDefaultProviders(List<MetaDataProvider> optionalMetaDataProviders) {
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
	private <T> List<BeanConfiguration<? super T>> getBeanConfigurationForHierarchy(MetaDataProvider provider, HibernateConstrainedType<T> constrainedType) {
		List<BeanConfiguration<? super T>> configurations = newArrayList();

		for ( HibernateConstrainedType<? super T> type : constrainedType.getHierarchy() ) {
			BeanConfiguration<? super T> configuration = provider.getBeanConfiguration( type );
			if ( configuration != null ) {
				configurations.add( configuration );
			}
		}

		return configurations;
	}
}
