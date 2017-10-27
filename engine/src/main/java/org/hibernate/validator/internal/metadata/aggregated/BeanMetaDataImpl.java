/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.aggregated;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashMap;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;

import java.lang.annotation.ElementType;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Executable;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.ElementKind;
import javax.validation.groups.Default;
import javax.validation.metadata.BeanDescriptor;
import javax.validation.metadata.ConstructorDescriptor;
import javax.validation.metadata.PropertyDescriptor;

import org.hibernate.validator.internal.engine.MethodValidationConfiguration;
import org.hibernate.validator.internal.engine.groups.Sequence;
import org.hibernate.validator.internal.engine.groups.ValidationOrder;
import org.hibernate.validator.internal.engine.groups.ValidationOrderGenerator;
import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorManager;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.descriptor.BeanDescriptorImpl;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.internal.metadata.descriptor.ExecutableDescriptorImpl;
import org.hibernate.validator.internal.metadata.facets.Cascadable;
import org.hibernate.validator.internal.metadata.raw.BeanConfiguration;
import org.hibernate.validator.internal.metadata.raw.ConfigurationSource;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement.ConstrainedElementKind;
import org.hibernate.validator.internal.metadata.raw.ConstrainedExecutable;
import org.hibernate.validator.internal.metadata.raw.ConstrainedField;
import org.hibernate.validator.internal.metadata.raw.ConstrainedType;
import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.ExecutableHelper;
import org.hibernate.validator.internal.util.ExecutableParameterNameProvider;
import org.hibernate.validator.internal.util.TypeResolutionHelper;
import org.hibernate.validator.internal.util.classhierarchy.ClassHierarchyHelper;
import org.hibernate.validator.internal.util.classhierarchy.Filters;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.util.stereotypes.Immutable;
import org.hibernate.validator.spi.group.DefaultGroupSequenceProvider;

/**
 * This class encapsulates all meta data needed for validation. Implementations of {@code Validator} interface can
 * instantiate an instance of this class and delegate the metadata extraction to it.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 * @author Chris Beckey &lt;cbeckey@paypal.com&gt;
 * @author Guillaume Smet
 */
public final class BeanMetaDataImpl<T> implements BeanMetaData<T> {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	/**
	 * Represents the "sequence" of just Default.class.
	 */
	private static final List<Class<?>> DEFAULT_GROUP_SEQUENCE = Collections.<Class<?>>singletonList( Default.class );

	/**
	 * Whether there are any constraints or cascades at all.
	 */
	private final boolean hasConstraints;

	private final ValidationOrderGenerator validationOrderGenerator;

	/**
	 * The root bean class for this meta data.
	 */
	private final Class<T> beanClass;

	/**
	 * Set of all constraints for this bean type (defined on any implemented interfaces or super types)
	 */
	@Immutable
	private final Set<MetaConstraint<?>> allMetaConstraints;

	/**
	 * Set of all constraints which are directly defined on the bean or any of the directly implemented interfaces
	 */
	@Immutable
	private final Set<MetaConstraint<?>> directMetaConstraints;

	/**
	 * Contains constrained related meta data for all the constrained methods and constructors of the type represented
	 * by this bean meta data. Keyed by executable, values are an aggregated view on each executable together with all
	 * the executables from the inheritance hierarchy with the same signature.
	 * <p>
	 * An entry will be stored once under the signature of the represented method and all the methods it overrides
	 * (there will only be more than one entry in case of generics in the parameters, e.g. in case of a super-type
	 * method {@code foo(T)} and an overriding sub-type method {@code foo(String)} two entries for the same executable
	 * meta-data will be stored).
	 */
	@Immutable
	private final Map<String, ExecutableMetaData> executableMetaDataMap;

	/**
	 * The set of unconstrained executables of the bean. It contains all the relevant signatures, following the same
	 * rules as {@code executableMetaDataMap}.
	 */
	@Immutable
	private final Set<String> unconstrainedExecutables;

	/**
	 * Property meta data keyed against the property name
	 */
	@Immutable
	private final Map<String, PropertyMetaData> propertyMetaDataMap;

	/**
	 * The cascaded properties of this bean.
	 */
	@Immutable
	private final Set<Cascadable> cascadedProperties;

	/**
	 * The bean descriptor for this bean.
	 */
	private final BeanDescriptor beanDescriptor;

	/**
	 * The default groups sequence for this bean class.
	 */
	@Immutable
	private final List<Class<?>> defaultGroupSequence;

	/**
	 * The default group sequence provider.
	 *
	 * @see org.hibernate.validator.group.GroupSequenceProvider
	 * @see DefaultGroupSequenceProvider
	 */
	private final DefaultGroupSequenceProvider<? super T> defaultGroupSequenceProvider;

	private final ValidationOrder validationOrder;

	/**
	 * The class hierarchy for this class starting with the class itself going up the inheritance chain. Interfaces
	 * are not included.
	 */
	@Immutable
	private final List<Class<? super T>> classHierarchyWithoutInterfaces;

	/**
	 * Creates a new {@link BeanMetaDataImpl}
	 *
	 * @param beanClass The Java type represented by this meta data object.
	 * @param defaultGroupSequence The default group sequence.
	 * @param defaultGroupSequenceProvider The default group sequence provider if set.
	 * @param constraintMetaDataSet All constraint meta data relating to the represented type.
	 */
	public BeanMetaDataImpl(Class<T> beanClass,
							List<Class<?>> defaultGroupSequence,
							DefaultGroupSequenceProvider<? super T> defaultGroupSequenceProvider,
							Set<ConstraintMetaData> constraintMetaDataSet,
							ValidationOrderGenerator validationOrderGenerator) {

		this.validationOrderGenerator = validationOrderGenerator;
		this.beanClass = beanClass;
		this.propertyMetaDataMap = newHashMap();

		Set<PropertyMetaData> propertyMetaDataSet = newHashSet();

		Set<ExecutableMetaData> executableMetaDataSet = newHashSet();
		Set<String> tmpUnconstrainedExecutables = newHashSet();

		boolean hasConstraints = false;

		for ( ConstraintMetaData constraintMetaData : constraintMetaDataSet ) {
			boolean elementHasConstraints = constraintMetaData.isCascading() || constraintMetaData.isConstrained();
			hasConstraints |= elementHasConstraints;

			if ( constraintMetaData.getKind() == ElementKind.PROPERTY ) {
				propertyMetaDataSet.add( (PropertyMetaData) constraintMetaData );
			}
			else {
				ExecutableMetaData executableMetaData = (ExecutableMetaData) constraintMetaData;
				if ( elementHasConstraints ) {
					executableMetaDataSet.add( executableMetaData );
				}
				else {
					tmpUnconstrainedExecutables.addAll( executableMetaData.getSignatures() );
				}
			}
		}

		Set<Cascadable> cascadedProperties = newHashSet();
		Set<MetaConstraint<?>> allMetaConstraints = newHashSet();

		for ( PropertyMetaData propertyMetaData : propertyMetaDataSet ) {
			propertyMetaDataMap.put( propertyMetaData.getName(), propertyMetaData );
			cascadedProperties.addAll( propertyMetaData.getCascadables() );
			allMetaConstraints.addAll( propertyMetaData.getAllConstraints() );
		}

		this.hasConstraints = hasConstraints;
		this.cascadedProperties = CollectionHelper.toImmutableSet( cascadedProperties );
		this.allMetaConstraints = CollectionHelper.toImmutableSet( allMetaConstraints );

		this.classHierarchyWithoutInterfaces = CollectionHelper.toImmutableList( ClassHierarchyHelper.getHierarchy(
				beanClass,
				Filters.excludeInterfaces()
		) );

		DefaultGroupSequenceContext<? super T> defaultGroupContext = getDefaultGroupSequenceData( beanClass, defaultGroupSequence, defaultGroupSequenceProvider, validationOrderGenerator );
		this.defaultGroupSequenceProvider = defaultGroupContext.defaultGroupSequenceProvider;
		this.defaultGroupSequence = CollectionHelper.toImmutableList( defaultGroupContext.defaultGroupSequence );
		this.validationOrder = defaultGroupContext.validationOrder;

		this.directMetaConstraints = getDirectConstraints();

		this.executableMetaDataMap = CollectionHelper.toImmutableMap( bySignature( executableMetaDataSet ) );
		this.unconstrainedExecutables = CollectionHelper.toImmutableSet( tmpUnconstrainedExecutables );

		boolean defaultGroupSequenceIsRedefined = defaultGroupSequenceIsRedefined();
		List<Class<?>> resolvedDefaultGroupSequence = getDefaultGroupSequence( null );

		Map<String, PropertyDescriptor> propertyDescriptors = getConstrainedPropertiesAsDescriptors(
				propertyMetaDataMap,
				defaultGroupSequenceIsRedefined,
				resolvedDefaultGroupSequence
		);

		Map<String, ExecutableDescriptorImpl> methodsDescriptors = getConstrainedMethodsAsDescriptors(
				executableMetaDataMap,
				defaultGroupSequenceIsRedefined,
				resolvedDefaultGroupSequence
		);

		Map<String, ConstructorDescriptor> constructorsDescriptors = getConstrainedConstructorsAsDescriptors(
				executableMetaDataMap,
				defaultGroupSequenceIsRedefined,
				resolvedDefaultGroupSequence
		);

		this.beanDescriptor = new BeanDescriptorImpl(
				beanClass,
				getClassLevelConstraintsAsDescriptors( allMetaConstraints ),
				propertyDescriptors,
				methodsDescriptors,
				constructorsDescriptors,
				defaultGroupSequenceIsRedefined,
				resolvedDefaultGroupSequence
		);
	}

	@Override
	public Class<T> getBeanClass() {
		return beanClass;
	}

	@Override
	public boolean hasConstraints() {
		return hasConstraints;
	}

	@Override
	public BeanDescriptor getBeanDescriptor() {
		return beanDescriptor;
	}

	@Override
	public Set<Cascadable> getCascadables() {
		return cascadedProperties;
	}

	@Override
	public PropertyMetaData getMetaDataFor(String propertyName) {
		PropertyMetaData propertyMetaData = propertyMetaDataMap.get( propertyName );

		if ( propertyMetaData == null ) {
			throw LOG.getPropertyNotDefinedByValidatedTypeException( beanClass, propertyName );
		}

		return propertyMetaData;
	}

	@Override
	public Set<MetaConstraint<?>> getMetaConstraints() {
		return allMetaConstraints;
	}

	@Override
	public Set<MetaConstraint<?>> getDirectMetaConstraints() {
		return directMetaConstraints;
	}

	@Override
	public Optional<ExecutableMetaData> getMetaDataFor(Executable executable) {
		String signature = ExecutableHelper.getSignature( executable );

		if ( unconstrainedExecutables.contains( signature ) ) {
			return Optional.empty();
		}

		ExecutableMetaData executableMetaData = executableMetaDataMap.get( ExecutableHelper.getSignature( executable ) );

		if ( executableMetaData == null ) {
			// there is no executable metadata - specified object and method do not match
			throw LOG.getMethodOrConstructorNotDefinedByValidatedTypeException(
					beanClass,
					executable
			);
		}

		return Optional.of( executableMetaData );
	}

	@Override
	public List<Class<?>> getDefaultGroupSequence(T beanState) {
		if ( hasDefaultGroupSequenceProvider() ) {
			List<Class<?>> providerDefaultGroupSequence = defaultGroupSequenceProvider.getValidationGroups( beanState );
			return getValidDefaultGroupSequence( beanClass, providerDefaultGroupSequence );
		}

		return defaultGroupSequence;
	}

	@Override
	public Iterator<Sequence> getDefaultValidationSequence(T beanState) {
		if ( hasDefaultGroupSequenceProvider() ) {
			List<Class<?>> providerDefaultGroupSequence = defaultGroupSequenceProvider.getValidationGroups( beanState );
			return validationOrderGenerator.getDefaultValidationOrder(
						beanClass,
						getValidDefaultGroupSequence( beanClass, providerDefaultGroupSequence )
					)
					.getSequenceIterator();
		}
		else {
			return validationOrder.getSequenceIterator();
		}
	}

	@Override
	public boolean defaultGroupSequenceIsRedefined() {
		return defaultGroupSequence.size() > 1 || hasDefaultGroupSequenceProvider();
	}

	@Override
	public List<Class<? super T>> getClassHierarchy() {
		return classHierarchyWithoutInterfaces;
	}

	private static Set<ConstraintDescriptorImpl<?>> getClassLevelConstraintsAsDescriptors(Set<MetaConstraint<?>> constraints) {
		return constraints.stream()
				.filter( c -> c.getElementType() == ElementType.TYPE )
				.map( MetaConstraint::getDescriptor )
				.collect( Collectors.toSet() );
	}

	private static Map<String, PropertyDescriptor> getConstrainedPropertiesAsDescriptors(Map<String, PropertyMetaData> propertyMetaDataMap,
			boolean defaultGroupSequenceIsRedefined, List<Class<?>> resolvedDefaultGroupSequence) {
		Map<String, PropertyDescriptor> theValue = newHashMap();

		for ( Entry<String, PropertyMetaData> entry : propertyMetaDataMap.entrySet() ) {
			if ( entry.getValue().isConstrained() && entry.getValue().getName() != null ) {
				theValue.put(
						entry.getKey(),
						entry.getValue().asDescriptor(
								defaultGroupSequenceIsRedefined,
								resolvedDefaultGroupSequence
						)
				);
			}
		}

		return theValue;
	}

	private static Map<String, ExecutableDescriptorImpl> getConstrainedMethodsAsDescriptors(Map<String, ExecutableMetaData> executableMetaDataMap,
			boolean defaultGroupSequenceIsRedefined, List<Class<?>> resolvedDefaultGroupSequence) {
		Map<String, ExecutableDescriptorImpl> constrainedMethodDescriptors = newHashMap();

		for ( ExecutableMetaData executableMetaData : executableMetaDataMap.values() ) {
			if ( executableMetaData.getKind() == ElementKind.METHOD
					&& executableMetaData.isConstrained() ) {
				ExecutableDescriptorImpl descriptor = executableMetaData.asDescriptor(
								defaultGroupSequenceIsRedefined,
								resolvedDefaultGroupSequence
						);

				for ( String signature : executableMetaData.getSignatures() ) {
					constrainedMethodDescriptors.put( signature, descriptor );
				}
			}
		}

		return constrainedMethodDescriptors;
	}

	private static Map<String, ConstructorDescriptor> getConstrainedConstructorsAsDescriptors(Map<String, ExecutableMetaData> executableMetaDataMap,
			boolean defaultGroupSequenceIsRedefined, List<Class<?>> resolvedDefaultGroupSequence) {
		Map<String, ConstructorDescriptor> constrainedMethodDescriptors = newHashMap();

		for ( ExecutableMetaData executableMetaData : executableMetaDataMap.values() ) {
			if ( executableMetaData.getKind() == ElementKind.CONSTRUCTOR && executableMetaData.isConstrained() ) {
				constrainedMethodDescriptors.put(
						// constructors never override, so there will be exactly one identifier
						executableMetaData.getSignatures().iterator().next(),
						executableMetaData.asDescriptor(
								defaultGroupSequenceIsRedefined,
								resolvedDefaultGroupSequence
						)
				);
			}
		}

		return constrainedMethodDescriptors;
	}

	private static <T> DefaultGroupSequenceContext<T> getDefaultGroupSequenceData(Class<?> beanClass, List<Class<?>> defaultGroupSequence, DefaultGroupSequenceProvider<? super T> defaultGroupSequenceProvider, ValidationOrderGenerator validationOrderGenerator) {
		if ( defaultGroupSequence != null && defaultGroupSequenceProvider != null ) {
			throw LOG.getInvalidDefaultGroupSequenceDefinitionException();
		}

		DefaultGroupSequenceContext<T> context = new DefaultGroupSequenceContext<>();

		if ( defaultGroupSequenceProvider != null ) {
			context.defaultGroupSequenceProvider = defaultGroupSequenceProvider;
			context.defaultGroupSequence = Collections.emptyList();
			context.validationOrder = null;
		}
		else if ( defaultGroupSequence != null && !defaultGroupSequence.isEmpty() ) {
			context.defaultGroupSequence = getValidDefaultGroupSequence( beanClass, defaultGroupSequence );
			context.validationOrder = validationOrderGenerator.getDefaultValidationOrder( beanClass, context.defaultGroupSequence );
		}
		else {
			context.defaultGroupSequence = DEFAULT_GROUP_SEQUENCE;
			context.validationOrder = ValidationOrder.DEFAULT_SEQUENCE;
		}

		return context;
	}

	private Set<MetaConstraint<?>> getDirectConstraints() {
		Set<MetaConstraint<?>> constraints = newHashSet();

		Set<Class<?>> classAndInterfaces = newHashSet();
		classAndInterfaces.add( beanClass );
		classAndInterfaces.addAll( ClassHierarchyHelper.getDirectlyImplementedInterfaces( beanClass ) );

		for ( Class<?> clazz : classAndInterfaces ) {
			for ( MetaConstraint<?> metaConstraint : allMetaConstraints ) {
				if ( metaConstraint.getLocation().getDeclaringClass().equals( clazz ) ) {
					constraints.add( metaConstraint );
				}
			}
		}

		return CollectionHelper.toImmutableSet( constraints );
	}

	/**
	 * Builds up the method meta data for this type; each meta-data entry will be stored under the signature of the
	 * represented method and all the methods it overrides.
	 */
	private Map<String, ExecutableMetaData> bySignature(Set<ExecutableMetaData> executables) {
		Map<String, ExecutableMetaData> theValue = newHashMap();

		for ( ExecutableMetaData executableMetaData : executables ) {
			for ( String signature : executableMetaData.getSignatures() ) {
				theValue.put( signature, executableMetaData );
			}
		}

		return theValue;
	}

	private static List<Class<?>> getValidDefaultGroupSequence(Class<?> beanClass, List<Class<?>> groupSequence) {
		List<Class<?>> validDefaultGroupSequence = new ArrayList<>();

		boolean groupSequenceContainsDefault = false;
		if ( groupSequence != null ) {
			for ( Class<?> group : groupSequence ) {
				if ( group.getName().equals( beanClass.getName() ) ) {
					validDefaultGroupSequence.add( Default.class );
					groupSequenceContainsDefault = true;
				}
				else if ( group.getName().equals( Default.class.getName() ) ) {
					throw LOG.getNoDefaultGroupInGroupSequenceException();
				}
				else {
					validDefaultGroupSequence.add( group );
				}
			}
		}
		if ( !groupSequenceContainsDefault ) {
			throw LOG.getBeanClassMustBePartOfRedefinedDefaultGroupSequenceException( beanClass );
		}
		if ( LOG.isTraceEnabled() ) {
			LOG.tracef(
					"Members of the default group sequence for bean %s are: %s.",
					beanClass.getName(),
					validDefaultGroupSequence
			);
		}

		return validDefaultGroupSequence;
	}

	private boolean hasDefaultGroupSequenceProvider() {
		return defaultGroupSequenceProvider != null;
	}

	@Override
	public String toString() {
		return "BeanMetaDataImpl"
				+ "{beanClass=" + beanClass.getSimpleName()
				+ ", constraintCount=" + getMetaConstraints().size()
				+ ", cascadedPropertiesCount=" + cascadedProperties.size()
				+ ", defaultGroupSequence=" + getDefaultGroupSequence( null ) + '}';
	}

	public static class BeanMetaDataBuilder<T> {

		private final ConstraintHelper constraintHelper;
		private final ValidationOrderGenerator validationOrderGenerator;
		private final Class<T> beanClass;
		private final Set<BuilderDelegate> builders = newHashSet();
		private final ExecutableHelper executableHelper;
		private final TypeResolutionHelper typeResolutionHelper;
		private final ValueExtractorManager valueExtractorManager;
		private final ExecutableParameterNameProvider parameterNameProvider;
		private final MethodValidationConfiguration methodValidationConfiguration;

		private ConfigurationSource sequenceSource;
		private ConfigurationSource providerSource;
		private List<Class<?>> defaultGroupSequence;
		private DefaultGroupSequenceProvider<? super T> defaultGroupSequenceProvider;


		private BeanMetaDataBuilder(
				ConstraintHelper constraintHelper,
				ExecutableHelper executableHelper,
				TypeResolutionHelper typeResolutionHelper,
				ValueExtractorManager valueExtractorManager,
				ExecutableParameterNameProvider parameterNameProvider,
				ValidationOrderGenerator validationOrderGenerator,
				Class<T> beanClass,
				MethodValidationConfiguration methodValidationConfiguration) {
			this.beanClass = beanClass;
			this.constraintHelper = constraintHelper;
			this.validationOrderGenerator = validationOrderGenerator;
			this.executableHelper = executableHelper;
			this.typeResolutionHelper = typeResolutionHelper;
			this.valueExtractorManager = valueExtractorManager;
			this.parameterNameProvider = parameterNameProvider;
			this.methodValidationConfiguration = methodValidationConfiguration;
		}

		public static <T> BeanMetaDataBuilder<T> getInstance(
				ConstraintHelper constraintHelper,
				ExecutableHelper executableHelper,
				TypeResolutionHelper typeResolutionHelper,
				ValueExtractorManager valueExtractorManager,
				ExecutableParameterNameProvider parameterNameProvider,
				ValidationOrderGenerator validationOrderGenerator,
				Class<T> beanClass,
				MethodValidationConfiguration methodValidationConfiguration) {
			return new BeanMetaDataBuilder<>(
					constraintHelper,
					executableHelper,
					typeResolutionHelper,
					valueExtractorManager,
					parameterNameProvider,
					validationOrderGenerator,
					beanClass,
					methodValidationConfiguration );
		}

		public void add(BeanConfiguration<? super T> configuration) {
			if ( configuration.getBeanClass().equals( beanClass ) ) {
				if ( configuration.getDefaultGroupSequence() != null
						&& ( sequenceSource == null || configuration.getSource()
						.getPriority() >= sequenceSource.getPriority() ) ) {

					sequenceSource = configuration.getSource();
					defaultGroupSequence = configuration.getDefaultGroupSequence();
				}

				if ( configuration.getDefaultGroupSequenceProvider() != null
						&& ( providerSource == null || configuration.getSource()
						.getPriority() >= providerSource.getPriority() ) ) {

					providerSource = configuration.getSource();
					defaultGroupSequenceProvider = configuration.getDefaultGroupSequenceProvider();
				}
			}

			for ( ConstrainedElement constrainedElement : configuration.getConstrainedElements() ) {
				addMetaDataToBuilder( constrainedElement, builders );
			}
		}

		private void addMetaDataToBuilder(ConstrainedElement constrainableElement, Set<BuilderDelegate> builders) {
			for ( BuilderDelegate builder : builders ) {
				boolean foundBuilder = builder.add( constrainableElement );

				if ( foundBuilder ) {
					return;
				}
			}

			builders.add(
					new BuilderDelegate(
							beanClass,
							constrainableElement,
							constraintHelper,
							executableHelper,
							typeResolutionHelper,
							valueExtractorManager,
							parameterNameProvider,
							methodValidationConfiguration
					)
			);
		}

		public BeanMetaDataImpl<T> build() {
			Set<ConstraintMetaData> aggregatedElements = newHashSet();

			for ( BuilderDelegate builder : builders ) {
				aggregatedElements.addAll( builder.build() );
			}

			return new BeanMetaDataImpl<>(
					beanClass,
					defaultGroupSequence,
					defaultGroupSequenceProvider,
					aggregatedElements,
					validationOrderGenerator
			);
		}
	}

	private static class BuilderDelegate {
		private final Class<?> beanClass;
		private final ConstrainedElement constrainedElement;
		private final ConstraintHelper constraintHelper;
		private final ExecutableHelper executableHelper;
		private final TypeResolutionHelper typeResolutionHelper;
		private final ValueExtractorManager valueExtractorManager;
		private final ExecutableParameterNameProvider parameterNameProvider;
		private MetaDataBuilder propertyBuilder;
		private ExecutableMetaData.Builder methodBuilder;
		private final MethodValidationConfiguration methodValidationConfiguration;
		private final int hashCode;

		public BuilderDelegate(
				Class<?> beanClass,
				ConstrainedElement constrainedElement,
				ConstraintHelper constraintHelper,
				ExecutableHelper executableHelper,
				TypeResolutionHelper typeResolutionHelper,
				ValueExtractorManager valueExtractorManager,
				ExecutableParameterNameProvider parameterNameProvider,
				MethodValidationConfiguration methodValidationConfiguration
		) {
			this.beanClass = beanClass;
			this.constrainedElement = constrainedElement;
			this.constraintHelper = constraintHelper;
			this.executableHelper = executableHelper;
			this.typeResolutionHelper = typeResolutionHelper;
			this.valueExtractorManager = valueExtractorManager;
			this.parameterNameProvider = parameterNameProvider;
			this.methodValidationConfiguration = methodValidationConfiguration;

			switch ( constrainedElement.getKind() ) {
				case FIELD:
					ConstrainedField constrainedField = (ConstrainedField) constrainedElement;
					propertyBuilder = new PropertyMetaData.Builder(
							beanClass,
							constrainedField,
							constraintHelper,
							typeResolutionHelper,
							valueExtractorManager
					);
					break;
				case CONSTRUCTOR:
				case METHOD:
					ConstrainedExecutable constrainedExecutable = (ConstrainedExecutable) constrainedElement;
					Member member = constrainedExecutable.getExecutable();

					// HV-890 Not adding meta-data for private super-type methods to the method meta-data of this bean;
					// It is not needed and it may conflict with sub-type methods of the same signature
					if ( !Modifier.isPrivate( member.getModifiers() ) || beanClass == member.getDeclaringClass() ) {
						methodBuilder = new ExecutableMetaData.Builder(
								beanClass,
								constrainedExecutable,
								constraintHelper,
								executableHelper,
								typeResolutionHelper,
								valueExtractorManager,
								parameterNameProvider,
								methodValidationConfiguration
						);
					}

					if ( constrainedExecutable.isGetterMethod() ) {
						propertyBuilder = new PropertyMetaData.Builder(
								beanClass,
								constrainedExecutable,
								constraintHelper,
								typeResolutionHelper,
								valueExtractorManager
						);
					}
					break;
				case TYPE:
					ConstrainedType constrainedType = (ConstrainedType) constrainedElement;
					propertyBuilder = new PropertyMetaData.Builder(
							beanClass,
							constrainedType,
							constraintHelper,
							typeResolutionHelper,
							valueExtractorManager
					);
					break;
			}

			this.hashCode = buildHashCode();
		}

		public boolean add(ConstrainedElement constrainedElement) {
			boolean added = false;

			if ( methodBuilder != null && methodBuilder.accepts( constrainedElement ) ) {
				methodBuilder.add( constrainedElement );
				added = true;
			}

			if ( propertyBuilder != null && propertyBuilder.accepts( constrainedElement ) ) {
				propertyBuilder.add( constrainedElement );

				if ( !added && constrainedElement.getKind() == ConstrainedElementKind.METHOD && methodBuilder == null ) {
					ConstrainedExecutable constrainedMethod = (ConstrainedExecutable) constrainedElement;
					methodBuilder = new ExecutableMetaData.Builder(
							beanClass,
							constrainedMethod,
							constraintHelper,
							executableHelper,
							typeResolutionHelper,
							valueExtractorManager,
							parameterNameProvider,
							methodValidationConfiguration
					);
				}

				added = true;
			}

			return added;
		}

		public Set<ConstraintMetaData> build() {
			Set<ConstraintMetaData> metaDataSet = newHashSet();

			if ( propertyBuilder != null ) {
				metaDataSet.add( propertyBuilder.build() );
			}

			if ( methodBuilder != null ) {
				metaDataSet.add( methodBuilder.build() );
			}

			return metaDataSet;
		}

		@Override
		public int hashCode() {
			return hashCode;
		}

		private int buildHashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + beanClass.hashCode();
			result = prime * result + constrainedElement.hashCode();
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if ( this == obj ) {
				return true;
			}
			if ( !super.equals( obj ) ) {
				return false;
			}
			if ( getClass() != obj.getClass() ) {
				return false;
			}
			BuilderDelegate other = (BuilderDelegate) obj;
			if ( !beanClass.equals( other.beanClass ) ) {
				return false;
			}
			if ( !constrainedElement.equals( other.constrainedElement ) ) {
				return false;
			}
			return true;
		}
	}

	/**
	 * Tuple for returning default group sequence, provider and validation order at once.
	 */
	private static class DefaultGroupSequenceContext<T> {
		List<Class<?>> defaultGroupSequence;
		DefaultGroupSequenceProvider<? super T> defaultGroupSequenceProvider;
		ValidationOrder validationOrder;
	}
}
