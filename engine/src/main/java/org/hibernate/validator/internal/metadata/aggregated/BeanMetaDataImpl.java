/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.aggregated;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashMap;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Executable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.validation.ElementKind;
import jakarta.validation.groups.Default;
import jakarta.validation.metadata.BeanDescriptor;
import jakarta.validation.metadata.ConstructorDescriptor;
import jakarta.validation.metadata.PropertyDescriptor;

import org.hibernate.validator.internal.engine.groups.Sequence;
import org.hibernate.validator.internal.engine.groups.ValidationOrder;
import org.hibernate.validator.internal.engine.groups.ValidationOrderGenerator;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.descriptor.BeanDescriptorImpl;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.internal.metadata.descriptor.ExecutableDescriptorImpl;
import org.hibernate.validator.internal.metadata.facets.Cascadable;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation.ConstraintLocationKind;
import org.hibernate.validator.internal.properties.Signature;
import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.ExecutableHelper;
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
 * @author Marko Bekhta
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
	private final Map<Signature, ExecutableMetaData> executableMetaDataMap;

	/**
	 * The set of unconstrained executables of the bean. It contains all the relevant signatures, following the same
	 * rules as {@code executableMetaDataMap}.
	 */
	@Immutable
	private final Set<Signature> unconstrainedExecutables;

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
	 * {code true} if the default group sequence is redefined, either via a group sequence redefinition or a group
	 * sequence provider.
	 */
	private final boolean defaultGroupSequenceRedefined;

	/**
	 * The resolved default group sequence.
	 */
	private final List<Class<?>> resolvedDefaultGroupSequence;

	/**
	 * The bean descriptor for this bean. Lazily created.
	 */
	private volatile BeanDescriptor beanDescriptor;

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
		Set<Signature> tmpUnconstrainedExecutables = newHashSet();

		boolean hasConstraints = false;
		Set<MetaConstraint<?>> allMetaConstraints = newHashSet();

		for ( ConstraintMetaData constraintMetaData : constraintMetaDataSet ) {
			boolean elementHasConstraints = constraintMetaData.isCascading() || constraintMetaData.isConstrained();
			hasConstraints |= elementHasConstraints;

			if ( constraintMetaData.getKind() == ElementKind.PROPERTY ) {
				propertyMetaDataSet.add( (PropertyMetaData) constraintMetaData );
			}
			else if ( constraintMetaData.getKind() == ElementKind.BEAN ) {
				allMetaConstraints.addAll( ( (ClassMetaData) constraintMetaData ).getAllConstraints() );
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

		// We initialize those elements eagerly so that any eventual error is thrown when bootstrapping the bean metadata
		this.defaultGroupSequenceRedefined = this.defaultGroupSequence.size() > 1 || hasDefaultGroupSequenceProvider();
		this.resolvedDefaultGroupSequence = getDefaultGroupSequence( null );
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
		BeanDescriptor beanDescriptor = this.beanDescriptor;

		if ( beanDescriptor == null ) {
			synchronized (this) {
				beanDescriptor = this.beanDescriptor;

				if ( beanDescriptor == null ) {
					beanDescriptor = createBeanDescriptor( beanClass, allMetaConstraints, propertyMetaDataMap, executableMetaDataMap,
							defaultGroupSequenceRedefined, resolvedDefaultGroupSequence );

					this.beanDescriptor = beanDescriptor;
				}
			}
		}

		return beanDescriptor;
	}

	@Override
	public Set<Cascadable> getCascadables() {
		return cascadedProperties;
	}

	@Override
	public boolean hasCascadables() {
		return !cascadedProperties.isEmpty();
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
		Signature signature = ExecutableHelper.getSignature( executable );

		if ( unconstrainedExecutables.contains( signature ) ) {
			return Optional.empty();
		}

		ExecutableMetaData executableMetaData = executableMetaDataMap.get( signature );

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
	public boolean isDefaultGroupSequenceRedefined() {
		return defaultGroupSequenceRedefined;
	}

	@Override
	public List<Class<? super T>> getClassHierarchy() {
		return classHierarchyWithoutInterfaces;
	}

	private static BeanDescriptor createBeanDescriptor(Class<?> beanClass, Set<MetaConstraint<?>> allMetaConstraints,
			Map<String, PropertyMetaData> propertyMetaDataMap, Map<Signature, ExecutableMetaData> executableMetaDataMap,
			boolean defaultGroupSequenceRedefined,
			List<Class<?>> resolvedDefaultGroupSequence) {
		Map<String, PropertyDescriptor> propertyDescriptors = getConstrainedPropertiesAsDescriptors(
				propertyMetaDataMap,
				defaultGroupSequenceRedefined,
				resolvedDefaultGroupSequence
		);

		Map<Signature, ExecutableDescriptorImpl> methodsDescriptors = getConstrainedMethodsAsDescriptors(
				executableMetaDataMap,
				defaultGroupSequenceRedefined,
				resolvedDefaultGroupSequence
		);

		Map<Signature, ConstructorDescriptor> constructorsDescriptors = getConstrainedConstructorsAsDescriptors(
				executableMetaDataMap,
				defaultGroupSequenceRedefined,
				resolvedDefaultGroupSequence
		);

		return new BeanDescriptorImpl(
				beanClass,
				getClassLevelConstraintsAsDescriptors( allMetaConstraints ),
				propertyDescriptors,
				methodsDescriptors,
				constructorsDescriptors,
				defaultGroupSequenceRedefined,
				resolvedDefaultGroupSequence
		);
	}

	private static Set<ConstraintDescriptorImpl<?>> getClassLevelConstraintsAsDescriptors(Set<MetaConstraint<?>> constraints) {
		return constraints.stream()
				.filter( c -> c.getConstraintLocationKind() == ConstraintLocationKind.TYPE )
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

	private static Map<Signature, ExecutableDescriptorImpl> getConstrainedMethodsAsDescriptors(
			Map<Signature, ExecutableMetaData> executableMetaDataMap,
			boolean defaultGroupSequenceIsRedefined, List<Class<?>> resolvedDefaultGroupSequence) {
		Map<Signature, ExecutableDescriptorImpl> constrainedMethodDescriptors = newHashMap();

		for ( ExecutableMetaData executableMetaData : executableMetaDataMap.values() ) {
			if ( executableMetaData.getKind() == ElementKind.METHOD
					&& executableMetaData.isConstrained() ) {
				ExecutableDescriptorImpl descriptor = executableMetaData.asDescriptor(
						defaultGroupSequenceIsRedefined,
						resolvedDefaultGroupSequence
				);

				for ( Signature signature : executableMetaData.getSignatures() ) {
					constrainedMethodDescriptors.put( signature, descriptor );
				}
			}
		}

		return constrainedMethodDescriptors;
	}

	private static Map<Signature, ConstructorDescriptor> getConstrainedConstructorsAsDescriptors(Map<Signature, ExecutableMetaData> executableMetaDataMap,
			boolean defaultGroupSequenceIsRedefined, List<Class<?>> resolvedDefaultGroupSequence) {
		Map<Signature, ConstructorDescriptor> constrainedMethodDescriptors = newHashMap();

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
	private Map<Signature, ExecutableMetaData> bySignature(Set<ExecutableMetaData> executables) {
		Map<Signature, ExecutableMetaData> theValue = newHashMap();

		for ( ExecutableMetaData executableMetaData : executables ) {
			for ( Signature signature : executableMetaData.getSignatures() ) {
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

	/**
	 * Tuple for returning default group sequence, provider and validation order at once.
	 */
	private static class DefaultGroupSequenceContext<T> {
		List<Class<?>> defaultGroupSequence;
		DefaultGroupSequenceProvider<? super T> defaultGroupSequenceProvider;
		ValidationOrder validationOrder;
	}
}
