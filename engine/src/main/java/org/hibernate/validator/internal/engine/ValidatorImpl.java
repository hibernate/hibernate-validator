/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine;

import static org.hibernate.validator.internal.util.logging.Messages.MESSAGES;

import java.lang.annotation.ElementType;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.validation.ConstraintValidatorFactory;
import javax.validation.ConstraintViolation;
import javax.validation.ElementKind;
import javax.validation.Path;
import javax.validation.TraversableResolver;
import javax.validation.Validator;
import javax.validation.executable.ExecutableValidator;
import javax.validation.groups.Default;
import javax.validation.metadata.BeanDescriptor;
import javax.validation.valueextraction.ValueExtractor;

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorInitializationContext;
import org.hibernate.validator.internal.engine.ValidationContext.ValidationContextBuilder;
import org.hibernate.validator.internal.engine.ValidationContext.ValidatorScopedContext;
import org.hibernate.validator.internal.engine.ValidatorFactoryImpl.ValidatorFactoryScopedContext;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorManager;
import org.hibernate.validator.internal.engine.constraintvalidation.HibernateConstraintValidatorInitializationContextImpl;
import org.hibernate.validator.internal.engine.groups.Group;
import org.hibernate.validator.internal.engine.groups.GroupWithInheritance;
import org.hibernate.validator.internal.engine.groups.Sequence;
import org.hibernate.validator.internal.engine.groups.ValidationOrder;
import org.hibernate.validator.internal.engine.groups.ValidationOrderGenerator;
import org.hibernate.validator.internal.engine.path.NodeImpl;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.hibernate.validator.internal.engine.resolver.TraversableResolvers;
import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorDescriptor;
import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorHelper;
import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorManager;
import org.hibernate.validator.internal.metadata.BeanMetaDataManager;
import org.hibernate.validator.internal.metadata.aggregated.BeanMetaData;
import org.hibernate.validator.internal.metadata.aggregated.CascadingMetaData;
import org.hibernate.validator.internal.metadata.aggregated.ContainerCascadingMetaData;
import org.hibernate.validator.internal.metadata.aggregated.ExecutableMetaData;
import org.hibernate.validator.internal.metadata.aggregated.ParameterMetaData;
import org.hibernate.validator.internal.metadata.aggregated.PropertyMetaData;
import org.hibernate.validator.internal.metadata.aggregated.ReturnValueMetaData;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.facets.Cascadable;
import org.hibernate.validator.internal.metadata.facets.Validatable;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.internal.metadata.location.FieldConstraintLocation;
import org.hibernate.validator.internal.metadata.location.GetterConstraintLocation;
import org.hibernate.validator.internal.metadata.location.TypeArgumentConstraintLocation;
import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.ExecutableHelper;
import org.hibernate.validator.internal.util.ReflectionHelper;
import org.hibernate.validator.internal.util.TypeHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * The main Bean Validation class. This is the core processing class of Hibernate Validator.
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 * @author Guillaume Smet
 */
public class ValidatorImpl implements Validator, ExecutableValidator {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	/**
	 * The default group array used in case any of the validate methods is called without a group.
	 */
	private static final Collection<Class<?>> DEFAULT_GROUPS = Collections.<Class<?>>singletonList( Default.class );

	/**
	 * Used to resolve the group execution order for a validate call.
	 */
	private final transient ValidationOrderGenerator validationOrderGenerator;

	/**
	 * Reference to shared {@code ConstraintValidatorFactory}.
	 */
	private final ConstraintValidatorFactory constraintValidatorFactory;

	/**
	 * {@link TraversableResolver} as passed to the constructor of this instance.
	 * Never use it directly, always use {@link #getCachingTraversableResolver()} to retrieved the single threaded caching wrapper.
	 */
	private final TraversableResolver traversableResolver;

	/**
	 * Used to get access to the bean meta data. Used to avoid to parsing the constraint configuration for each call
	 * of a given entity.
	 */
	private final BeanMetaDataManager beanMetaDataManager;

	/**
	 * Manages the life cycle of constraint validator instances
	 */
	private final ConstraintValidatorManager constraintValidatorManager;

	private final ValueExtractorManager valueExtractorManager;

	/**
	 * Context containing all {@link Validator} level helpers and configuration properties.
	 */
	private final ValidatorScopedContext validatorScopedContext;

	/**
	 * The constraint initialization context is stored at this level to prevent creating a new instance each time we
	 * initialize a new constraint validator as, for now, it only contains Validator scoped objects.
	 */
	private final HibernateConstraintValidatorInitializationContext constraintValidatorInitializationContext;

	public ValidatorImpl(ConstraintValidatorFactory constraintValidatorFactory,
			BeanMetaDataManager beanMetaDataManager,
			ValueExtractorManager valueExtractorManager,
			ConstraintValidatorManager constraintValidatorManager,
			ValidationOrderGenerator validationOrderGenerator,
			ValidatorFactoryScopedContext validatorFactoryScopedContext) {
		this.constraintValidatorFactory = constraintValidatorFactory;
		this.beanMetaDataManager = beanMetaDataManager;
		this.valueExtractorManager = valueExtractorManager;
		this.constraintValidatorManager = constraintValidatorManager;
		this.validationOrderGenerator = validationOrderGenerator;
		this.validatorScopedContext = new ValidatorScopedContext( validatorFactoryScopedContext );
		this.traversableResolver = validatorFactoryScopedContext.getTraversableResolver();
		this.constraintValidatorInitializationContext = new HibernateConstraintValidatorInitializationContextImpl( validatorScopedContext.getScriptEvaluatorFactory(), validatorScopedContext.getClockProvider(), validatorScopedContext.getTemporalValidationTolerance() );
	}

	@Override
	public final <T> Set<ConstraintViolation<T>> validate(T object, Class<?>... groups) {
		Contracts.assertNotNull( object, MESSAGES.validatedObjectMustNotBeNull() );
		sanityCheckGroups( groups );

		ValidationContext<T> validationContext = getValidationContextBuilder().forValidate( object );

		if ( !validationContext.getRootBeanMetaData().hasConstraints() ) {
			return Collections.emptySet();
		}

		ValidationOrder validationOrder = determineGroupValidationOrder( groups );
		ValueContext<?, Object> valueContext = ValueContext.getLocalExecutionContext(
				validatorScopedContext.getParameterNameProvider(),
				object,
				validationContext.getRootBeanMetaData(),
				PathImpl.createRootPath()
		);

		return validateInContext( validationContext, valueContext, validationOrder );
	}

	@Override
	public final <T> Set<ConstraintViolation<T>> validateProperty(T object, String propertyName, Class<?>... groups) {
		Contracts.assertNotNull( object, MESSAGES.validatedObjectMustNotBeNull() );
		sanityCheckPropertyPath( propertyName );
		sanityCheckGroups( groups );

		ValidationContext<T> validationContext = getValidationContextBuilder().forValidateProperty( object );

		if ( !validationContext.getRootBeanMetaData().hasConstraints() ) {
			return Collections.emptySet();
		}

		PathImpl propertyPath = PathImpl.createPathFromString( propertyName );
		ValueContext<?, Object> valueContext = getValueContextForPropertyValidation( validationContext, propertyPath );

		if ( valueContext.getCurrentBean() == null ) {
			throw LOG.getUnableToReachPropertyToValidateException( validationContext.getRootBean(), propertyPath );
		}

		ValidationOrder validationOrder = determineGroupValidationOrder( groups );

		return validateInContext( validationContext, valueContext, validationOrder );
	}

	@Override
	public final <T> Set<ConstraintViolation<T>> validateValue(Class<T> beanType, String propertyName, Object value, Class<?>... groups) {
		Contracts.assertNotNull( beanType, MESSAGES.beanTypeCannotBeNull() );
		sanityCheckPropertyPath( propertyName );
		sanityCheckGroups( groups );

		ValidationContext<T> validationContext = getValidationContextBuilder().forValidateValue( beanType );

		if ( !validationContext.getRootBeanMetaData().hasConstraints() ) {
			return Collections.emptySet();
		}

		ValidationOrder validationOrder = determineGroupValidationOrder( groups );

		return validateValueInContext(
				validationContext,
				value,
				PathImpl.createPathFromString( propertyName ),
				validationOrder
		);
	}

	@Override
	public <T> Set<ConstraintViolation<T>> validateParameters(T object, Method method, Object[] parameterValues, Class<?>... groups) {
		Contracts.assertNotNull( object, MESSAGES.validatedObjectMustNotBeNull() );
		Contracts.assertNotNull( method, MESSAGES.validatedMethodMustNotBeNull() );
		Contracts.assertNotNull( parameterValues, MESSAGES.validatedParameterArrayMustNotBeNull() );

		return validateParameters( object, (Executable) method, parameterValues, groups );
	}

	@Override
	public <T> Set<ConstraintViolation<T>> validateConstructorParameters(Constructor<? extends T> constructor, Object[] parameterValues, Class<?>... groups) {
		Contracts.assertNotNull( constructor, MESSAGES.validatedConstructorMustNotBeNull() );
		Contracts.assertNotNull( parameterValues, MESSAGES.validatedParameterArrayMustNotBeNull() );

		return validateParameters( null, constructor, parameterValues, groups );
	}

	@Override
	public <T> Set<ConstraintViolation<T>> validateConstructorReturnValue(Constructor<? extends T> constructor, T createdObject, Class<?>... groups) {
		Contracts.assertNotNull( constructor, MESSAGES.validatedConstructorMustNotBeNull() );
		Contracts.assertNotNull( createdObject, MESSAGES.validatedConstructorCreatedInstanceMustNotBeNull() );

		return validateReturnValue( null, constructor, createdObject, groups );
	}

	@Override
	public <T> Set<ConstraintViolation<T>> validateReturnValue(T object, Method method, Object returnValue, Class<?>... groups) {
		Contracts.assertNotNull( object, MESSAGES.validatedObjectMustNotBeNull() );
		Contracts.assertNotNull( method, MESSAGES.validatedMethodMustNotBeNull() );

		return validateReturnValue( object, (Executable) method, returnValue, groups );
	}

	private <T> Set<ConstraintViolation<T>> validateParameters(T object, Executable executable, Object[] parameterValues, Class<?>... groups) {
		sanityCheckGroups( groups );

		ValidationContext<T> validationContext = getValidationContextBuilder().forValidateParameters(
				validatorScopedContext.getParameterNameProvider(),
				object,
				executable,
				parameterValues
		);

		if ( !validationContext.getRootBeanMetaData().hasConstraints() ) {
			return Collections.emptySet();
		}

		ValidationOrder validationOrder = determineGroupValidationOrder( groups );

		validateParametersInContext( validationContext, parameterValues, validationOrder );

		return validationContext.getFailingConstraints();
	}

	private <T> Set<ConstraintViolation<T>> validateReturnValue(T object, Executable executable, Object returnValue, Class<?>... groups) {
		sanityCheckGroups( groups );

		ValidationContext<T> validationContext = getValidationContextBuilder().forValidateReturnValue(
				object,
				executable,
				returnValue
		);

		if ( !validationContext.getRootBeanMetaData().hasConstraints() ) {
			return Collections.emptySet();
		}

		ValidationOrder validationOrder = determineGroupValidationOrder( groups );

		validateReturnValueInContext( validationContext, object, returnValue, validationOrder );

		return validationContext.getFailingConstraints();
	}

	@Override
	public final BeanDescriptor getConstraintsForClass(Class<?> clazz) {
		return beanMetaDataManager.getBeanMetaData( clazz ).getBeanDescriptor();
	}

	@Override
	public final <T> T unwrap(Class<T> type) {
		//allow unwrapping into public super types; intentionally not exposing the
		//fact that ExecutableValidator is implemented by this class as well as this
		//might change
		if ( type.isAssignableFrom( Validator.class ) ) {
			return type.cast( this );
		}

		throw LOG.getTypeNotSupportedForUnwrappingException( type );
	}

	@Override
	public ExecutableValidator forExecutables() {
		return this;
	}

	private ValidationContextBuilder getValidationContextBuilder() {
		return ValidationContext.getValidationContextBuilder(
				beanMetaDataManager,
				constraintValidatorManager,
				constraintValidatorFactory,
				validatorScopedContext,
				TraversableResolvers.wrapWithCachingForSingleValidation( traversableResolver, validatorScopedContext.isTraversableResolverResultCacheEnabled() ),
				constraintValidatorInitializationContext

		);
	}

	private void sanityCheckPropertyPath(String propertyName) {
		if ( propertyName == null || propertyName.length() == 0 ) {
			throw LOG.getInvalidPropertyPathException();
		}
	}

	private void sanityCheckGroups(Class<?>[] groups) {
		Contracts.assertNotNull( groups, MESSAGES.groupMustNotBeNull() );
		for ( Class<?> clazz : groups ) {
			if ( clazz == null ) {
				throw new IllegalArgumentException( MESSAGES.groupMustNotBeNull() );
			}
		}
	}

	private ValidationOrder determineGroupValidationOrder(Class<?>[] groups) {
		Collection<Class<?>> resultGroups;
		// if no groups is specified use the default
		if ( groups.length == 0 ) {
			resultGroups = DEFAULT_GROUPS;
		}
		else {
			resultGroups = Arrays.asList( groups );
		}
		return validationOrderGenerator.getValidationOrder( resultGroups );
	}

	/**
	 * Validates the given object using the available context information.
	 * @param validationContext the global validation context
	 * @param valueContext the current validation context
	 * @param validationOrder Contains the information which and in which order groups have to be executed
	 *
	 * @param <T> The root bean type
	 *
	 * @return Set of constraint violations or the empty set if there were no violations.
	 */
	private <T, U> Set<ConstraintViolation<T>> validateInContext(ValidationContext<T> validationContext, ValueContext<U, Object> valueContext,
			ValidationOrder validationOrder) {
		if ( valueContext.getCurrentBean() == null ) {
			return Collections.emptySet();
		}

		BeanMetaData<U> beanMetaData = valueContext.getCurrentBeanMetaData();
		if ( beanMetaData.defaultGroupSequenceIsRedefined() ) {
			validationOrder.assertDefaultGroupSequenceIsExpandable( beanMetaData.getDefaultGroupSequence( valueContext.getCurrentBean() ) );
		}

		// process first single groups. For these we can optimise object traversal by first running all validations on the current bean
		// before traversing the object.
		Iterator<Group> groupIterator = validationOrder.getGroupIterator();
		while ( groupIterator.hasNext() ) {
			Group group = groupIterator.next();
			valueContext.setCurrentGroup( group.getDefiningClass() );
			validateConstraintsForCurrentGroup( validationContext, valueContext );
			if ( shouldFailFast( validationContext ) ) {
				return validationContext.getFailingConstraints();
			}
		}
		groupIterator = validationOrder.getGroupIterator();
		while ( groupIterator.hasNext() ) {
			Group group = groupIterator.next();
			valueContext.setCurrentGroup( group.getDefiningClass() );
			validateCascadedConstraints( validationContext, valueContext );
			if ( shouldFailFast( validationContext ) ) {
				return validationContext.getFailingConstraints();
			}
		}

		// now we process sequences. For sequences I have to traverse the object graph since I have to stop processing when an error occurs.
		Iterator<Sequence> sequenceIterator = validationOrder.getSequenceIterator();
		while ( sequenceIterator.hasNext() ) {
			Sequence sequence = sequenceIterator.next();
			for ( GroupWithInheritance groupOfGroups : sequence ) {
				int numberOfViolations = validationContext.getFailingConstraints().size();

				for ( Group group : groupOfGroups ) {
					valueContext.setCurrentGroup( group.getDefiningClass() );

					validateConstraintsForCurrentGroup( validationContext, valueContext );
					if ( shouldFailFast( validationContext ) ) {
						return validationContext.getFailingConstraints();
					}

					validateCascadedConstraints( validationContext, valueContext );
					if ( shouldFailFast( validationContext ) ) {
						return validationContext.getFailingConstraints();
					}
				}
				if ( validationContext.getFailingConstraints().size() > numberOfViolations ) {
					break;
				}
			}
		}
		return validationContext.getFailingConstraints();
	}

	private void validateConstraintsForCurrentGroup(ValidationContext<?> validationContext, ValueContext<?, Object> valueContext) {
		// we are not validating the default group there is nothing special to consider. If we are validating the default
		// group sequence we have to consider that a class in the hierarchy could redefine the default group sequence.
		if ( !valueContext.validatingDefault() ) {
			validateConstraintsForNonDefaultGroup( validationContext, valueContext );
		}
		else {
			validateConstraintsForDefaultGroup( validationContext, valueContext );
		}
	}

	private <U> void validateConstraintsForDefaultGroup(ValidationContext<?> validationContext, ValueContext<U, Object> valueContext) {
		final BeanMetaData<U> beanMetaData = valueContext.getCurrentBeanMetaData();
		final Map<Class<?>, Class<?>> validatedInterfaces = new HashMap<>();

		// evaluating the constraints of a bean per class in hierarchy, this is necessary to detect potential default group re-definitions
		for ( Class<? super U> clazz : beanMetaData.getClassHierarchy() ) {
			BeanMetaData<? super U> hostingBeanMetaData = beanMetaDataManager.getBeanMetaData( clazz );
			boolean defaultGroupSequenceIsRedefined = hostingBeanMetaData.defaultGroupSequenceIsRedefined();

			// if the current class redefined the default group sequence, this sequence has to be applied to all the class hierarchy.
			if ( defaultGroupSequenceIsRedefined ) {
				Iterator<Sequence> defaultGroupSequence = hostingBeanMetaData.getDefaultValidationSequence( valueContext.getCurrentBean() );
				Set<MetaConstraint<?>> metaConstraints = hostingBeanMetaData.getMetaConstraints();

				while ( defaultGroupSequence.hasNext() ) {
					for ( GroupWithInheritance groupOfGroups : defaultGroupSequence.next() ) {
						boolean validationSuccessful = true;

						for ( Group defaultSequenceMember : groupOfGroups ) {
							validationSuccessful = validateConstraintsForSingleDefaultGroupElement( validationContext, valueContext, validatedInterfaces, clazz,
									metaConstraints, defaultSequenceMember );
						}
						if ( !validationSuccessful ) {
							break;
						}
					}
				}
			}
			// fast path in case the default group sequence hasn't been redefined
			else {
				Set<MetaConstraint<?>> metaConstraints = hostingBeanMetaData.getDirectMetaConstraints();
				validateConstraintsForSingleDefaultGroupElement( validationContext, valueContext, validatedInterfaces, clazz, metaConstraints,
						Group.DEFAULT_GROUP );
			}

			validationContext.markCurrentBeanAsProcessed( valueContext );

			// all constraints in the hierarchy has been validated, stop validation.
			if ( defaultGroupSequenceIsRedefined ) {
				break;
			}
		}
	}

	private <U> boolean validateConstraintsForSingleDefaultGroupElement(ValidationContext<?> validationContext, ValueContext<U, Object> valueContext, final Map<Class<?>, Class<?>> validatedInterfaces,
			Class<? super U> clazz, Set<MetaConstraint<?>> metaConstraints, Group defaultSequenceMember) {
		boolean validationSuccessful = true;

		valueContext.setCurrentGroup( defaultSequenceMember.getDefiningClass() );

		for ( MetaConstraint<?> metaConstraint : metaConstraints ) {
			// HV-466, an interface implemented more than one time in the hierarchy has to be validated only one
			// time. An interface can define more than one constraint, we have to check the class we are validating.
			final Class<?> declaringClass = metaConstraint.getLocation().getDeclaringClass();
			if ( declaringClass.isInterface() ) {
				Class<?> validatedForClass = validatedInterfaces.get( declaringClass );
				if ( validatedForClass != null && !validatedForClass.equals( clazz ) ) {
					continue;
				}
				validatedInterfaces.put( declaringClass, clazz );
			}

			boolean tmp = validateMetaConstraint( validationContext, valueContext, valueContext.getCurrentBean(), metaConstraint );
			if ( shouldFailFast( validationContext ) ) {
				return false;
			}

			validationSuccessful = validationSuccessful && tmp;
		}
		return validationSuccessful;
	}

	private void validateConstraintsForNonDefaultGroup(ValidationContext<?> validationContext, ValueContext<?, Object> valueContext) {
		validateMetaConstraints( validationContext, valueContext, valueContext.getCurrentBean(), valueContext.getCurrentBeanMetaData().getMetaConstraints() );
		validationContext.markCurrentBeanAsProcessed( valueContext );
	}

	private void validateMetaConstraints(ValidationContext<?> validationContext, ValueContext<?, Object> valueContext, Object parent,
			Iterable<MetaConstraint<?>> constraints) {

		for ( MetaConstraint<?> metaConstraint : constraints ) {
			validateMetaConstraint( validationContext, valueContext, parent, metaConstraint );
			if ( shouldFailFast( validationContext ) ) {
				break;
			}
		}
	}

	private boolean validateMetaConstraint(ValidationContext<?> validationContext, ValueContext<?, Object> valueContext, Object parent, MetaConstraint<?> metaConstraint) {
		ValueContext.ValueState<Object> originalValueState = valueContext.getCurrentValueState();
		valueContext.appendNode( metaConstraint.getLocation() );
		boolean success = true;

		if ( isValidationRequired( validationContext, valueContext, metaConstraint ) ) {

			if ( parent != null ) {
				valueContext.setCurrentValidatedValue( valueContext.getValue( parent, metaConstraint.getLocation() ) );
			}

			success = metaConstraint.validateConstraint( validationContext, valueContext );

			validationContext.markConstraintProcessed( valueContext.getCurrentBean(), valueContext.getPropertyPath(), metaConstraint );
		}

		// reset the value context to the state before this call
		valueContext.resetValueState( originalValueState );

		return success;
	}

	/**
	 * Validates all cascaded constraints for the given bean using the current group set in the execution context.
	 * This method must always be called after validateConstraints for the same context.
	 *
	 * @param validationContext The execution context
	 * @param valueContext Collected information for single validation
	 */
	private void validateCascadedConstraints(ValidationContext<?> validationContext, ValueContext<?, Object> valueContext) {
		Validatable validatable = valueContext.getCurrentValidatable();
		ValueContext.ValueState<Object> originalValueState = valueContext.getCurrentValueState();

		for ( Cascadable cascadable : validatable.getCascadables() ) {
			valueContext.appendNode( cascadable );

			ElementType elementType = cascadable.getElementType();
			if ( isCascadeRequired( validationContext, valueContext.getCurrentBean(), valueContext.getPropertyPath(), elementType ) ) {
				Object value = getCascadableValue( validationContext, valueContext.getCurrentBean(), cascadable );
				CascadingMetaData cascadingMetaData = cascadable.getCascadingMetaData();

				if ( value != null ) {
					CascadingMetaData effectiveCascadingMetaData = cascadingMetaData.addRuntimeLegacyCollectionSupport( value.getClass() );

					// validate cascading on the annotated object
					if ( effectiveCascadingMetaData.isCascading() ) {
						validateCascadedAnnotatedObjectForCurrentGroup( value, validationContext, valueContext, effectiveCascadingMetaData );
					}

					if ( effectiveCascadingMetaData.isContainer() ) {
						ContainerCascadingMetaData containerCascadingMetaData = effectiveCascadingMetaData.as( ContainerCascadingMetaData.class );

						if ( containerCascadingMetaData.hasContainerElementsMarkedForCascading() ) {
							// validate cascading on the container elements
							validateCascadedContainerElementsForCurrentGroup( value, validationContext, valueContext,
									containerCascadingMetaData.getContainerElementTypesCascadingMetaData() );
						}
					}
				}
			}

			// reset the value context
			valueContext.resetValueState( originalValueState );
		}
	}

	private void validateCascadedAnnotatedObjectForCurrentGroup(Object value, ValidationContext<?> validationContext, ValueContext<?, Object> valueContext,
			CascadingMetaData cascadingMetaData) {
		if ( validationContext.isBeanAlreadyValidated( value, valueContext.getCurrentGroup(), valueContext.getPropertyPath() ) ||
				shouldFailFast( validationContext ) ) {
			return;
		}

		Class<?> originalGroup = valueContext.getCurrentGroup();
		Class<?> currentGroup = cascadingMetaData.convertGroup( originalGroup );

		// expand the group only if was created by group conversion;
		// otherwise we're looping through the right validation order
		// already and need only to pass the current element
		ValidationOrder validationOrder = validationOrderGenerator.getValidationOrder( currentGroup, currentGroup != originalGroup );

		ValueContext<?, Object> cascadedValueContext = buildNewLocalExecutionContext( valueContext, value );

		validateInContext( validationContext, cascadedValueContext, validationOrder );
	}

	private void validateCascadedContainerElementsForCurrentGroup(Object value, ValidationContext<?> validationContext, ValueContext<?, ?> valueContext,
			List<ContainerCascadingMetaData> containerElementTypesCascadingMetaData) {
		for ( ContainerCascadingMetaData cascadingMetaData : containerElementTypesCascadingMetaData ) {
			if ( !cascadingMetaData.isMarkedForCascadingOnAnnotatedObjectOrContainerElements() ) {
				continue;
			}

			ValueExtractorDescriptor extractor = valueExtractorManager.getMaximallySpecificAndContainerElementCompliantValueExtractor(
					cascadingMetaData.getValueExtractorCandidates(),
					value.getClass()
			);

			if ( extractor == null ) {
				throw LOG.getNoValueExtractorFoundForTypeException( cascadingMetaData.getEnclosingType(), cascadingMetaData.getTypeParameter(), value.getClass() );
			}

			CascadingValueReceiver receiver = new CascadingValueReceiver( validationContext, valueContext, cascadingMetaData );
			ValueExtractorHelper.extractValues( extractor, value, receiver );
		}
	}

	private class CascadingValueReceiver implements ValueExtractor.ValueReceiver {

		private final ValidationContext<?> validationContext;
		private final ValueContext<?, ?> valueContext;
		private final ContainerCascadingMetaData cascadingMetaData;

		public CascadingValueReceiver(ValidationContext<?> validationContext, ValueContext<?, ?> valueContext, ContainerCascadingMetaData cascadingMetaData) {
			this.validationContext = validationContext;
			this.valueContext = valueContext;
			this.cascadingMetaData = cascadingMetaData;
		}

		@Override
		public void value(String nodeName, Object value) {
			doValidate( value, nodeName );
		}

		@Override
		public void iterableValue(String nodeName, Object value) {
			valueContext.markCurrentPropertyAsIterable();
			doValidate( value, nodeName );
		}

		@Override
		public void indexedValue(String nodeName, int index, Object value) {
			valueContext.markCurrentPropertyAsIterableAndSetIndex( index );
			doValidate( value, nodeName );
		}

		@Override
		public void keyedValue(String nodeName, Object key, Object value) {
			valueContext.markCurrentPropertyAsIterableAndSetKey( key );
			doValidate( value, nodeName );
		}

		private void doValidate(Object value, String nodeName) {
			if ( value == null ||
					validationContext.isBeanAlreadyValidated( value, valueContext.getCurrentGroup(), valueContext.getPropertyPath() ) ||
					shouldFailFast( validationContext ) ) {
				return;
			}

			Class<?> originalGroup = valueContext.getCurrentGroup();
			Class<?> currentGroup = cascadingMetaData.convertGroup( originalGroup );

			// expand the group only if was created by group conversion;
			// otherwise we're looping through the right validation order
			// already and need only to pass the current element
			ValidationOrder validationOrder = validationOrderGenerator.getValidationOrder( currentGroup, currentGroup != originalGroup );

			ValueContext<?, Object> cascadedValueContext = buildNewLocalExecutionContext( valueContext, value );

			if ( cascadingMetaData.getDeclaredContainerClass() != null ) {
				cascadedValueContext.setTypeParameter( cascadingMetaData.getDeclaredContainerClass(), cascadingMetaData.getDeclaredTypeParameter() );
			}

			// Cascade validation
			if ( cascadingMetaData.isCascading() ) {
				validateInContext( validationContext, cascadedValueContext, validationOrder );
			}

			// Cascade validation to container elements if we are dealing with a container element
			if ( cascadingMetaData.hasContainerElementsMarkedForCascading() ) {
				ValueContext<?, Object> cascadedTypeArgumentValueContext = buildNewLocalExecutionContext( valueContext, value );
				if ( cascadingMetaData.getTypeParameter() != null ) {
					cascadedValueContext.setTypeParameter( cascadingMetaData.getDeclaredContainerClass(), cascadingMetaData.getDeclaredTypeParameter() );
				}

				if ( nodeName != null ) {
					cascadedTypeArgumentValueContext.appendTypeParameterNode( nodeName );
				}

				validateCascadedContainerElementsInContext( value, validationContext, cascadedTypeArgumentValueContext, cascadingMetaData, validationOrder );
			}
		}
	}

	private void validateCascadedContainerElementsInContext(Object value, ValidationContext<?> validationContext, ValueContext<?, ?> valueContext,
			ContainerCascadingMetaData cascadingMetaData, ValidationOrder validationOrder) {
		Iterator<Group> groupIterator = validationOrder.getGroupIterator();
		while ( groupIterator.hasNext() ) {
			Group group = groupIterator.next();
			valueContext.setCurrentGroup( group.getDefiningClass() );
			validateCascadedContainerElementsForCurrentGroup( value, validationContext, valueContext,
					cascadingMetaData.getContainerElementTypesCascadingMetaData() );
			if ( shouldFailFast( validationContext ) ) {
				return;
			}
		}

		Iterator<Sequence> sequenceIterator = validationOrder.getSequenceIterator();
		while ( sequenceIterator.hasNext() ) {
			Sequence sequence = sequenceIterator.next();
			for ( GroupWithInheritance groupOfGroups : sequence ) {
				int numberOfViolations = validationContext.getFailingConstraints().size();

				for ( Group group : groupOfGroups ) {
					valueContext.setCurrentGroup( group.getDefiningClass() );

					validateCascadedContainerElementsForCurrentGroup( value, validationContext, valueContext,
							cascadingMetaData.getContainerElementTypesCascadingMetaData() );
					if ( shouldFailFast( validationContext ) ) {
						return;
					}
				}
				if ( validationContext.getFailingConstraints().size() > numberOfViolations ) {
					break;
				}
			}
		}
	}

	private ValueContext<?, Object> buildNewLocalExecutionContext(ValueContext<?, ?> valueContext, Object value) {
		ValueContext<?, Object> newValueContext;
		if ( value != null ) {
			newValueContext = ValueContext.getLocalExecutionContext(
					validatorScopedContext.getParameterNameProvider(),
					value,
					beanMetaDataManager.getBeanMetaData( value.getClass() ),
					valueContext.getPropertyPath()
			);
			newValueContext.setCurrentValidatedValue( value );
		}
		else {
			newValueContext = ValueContext.getLocalExecutionContext(
					validatorScopedContext.getParameterNameProvider(),
					valueContext.getCurrentBeanType(),
					valueContext.getCurrentBeanMetaData(),
					valueContext.getPropertyPath()
			);
		}

		return newValueContext;
	}

	private <T> Set<ConstraintViolation<T>> validateValueInContext(ValidationContext<T> validationContext, Object value, PathImpl propertyPath,
			ValidationOrder validationOrder) {
		ValueContext<?, Object> valueContext = getValueContextForValueValidation( validationContext, propertyPath );
		valueContext.setCurrentValidatedValue( value );

		BeanMetaData<?> beanMetaData = valueContext.getCurrentBeanMetaData();
		if ( beanMetaData.defaultGroupSequenceIsRedefined() ) {
			validationOrder.assertDefaultGroupSequenceIsExpandable( beanMetaData.getDefaultGroupSequence( null ) );
		}

		// process first single groups
		Iterator<Group> groupIterator = validationOrder.getGroupIterator();
		while ( groupIterator.hasNext() ) {
			Group group = groupIterator.next();
			valueContext.setCurrentGroup( group.getDefiningClass() );
			validateConstraintsForCurrentGroup( validationContext, valueContext );
			if ( shouldFailFast( validationContext ) ) {
				return validationContext.getFailingConstraints();
			}
		}

		// now process sequences, stop after the first erroneous group
		Iterator<Sequence> sequenceIterator = validationOrder.getSequenceIterator();
		while ( sequenceIterator.hasNext() ) {
			Sequence sequence = sequenceIterator.next();
			for ( GroupWithInheritance groupOfGroups : sequence ) {
				int numberOfConstraintViolationsBefore = validationContext.getFailingConstraints().size();
				for ( Group group : groupOfGroups ) {
					valueContext.setCurrentGroup( group.getDefiningClass() );
					validateConstraintsForCurrentGroup( validationContext, valueContext );
					if ( shouldFailFast( validationContext ) ) {
						return validationContext.getFailingConstraints();
					}
				}
				if ( validationContext.getFailingConstraints().size() > numberOfConstraintViolationsBefore ) {
					break;
				}
			}
		}

		return validationContext.getFailingConstraints();
	}

	private <T> void validateParametersInContext(ValidationContext<T> validationContext,
			Object[] parameterValues,
			ValidationOrder validationOrder) {
		BeanMetaData<T> beanMetaData = validationContext.getRootBeanMetaData();

		Optional<ExecutableMetaData> executableMetaDataOptional = beanMetaData.getMetaDataFor( validationContext.getExecutable() );

		if ( !executableMetaDataOptional.isPresent() ) {
			// the method is unconstrained
			return;
		}

		ExecutableMetaData executableMetaData = executableMetaDataOptional.get();

		if ( parameterValues.length != executableMetaData.getParameterTypes().length ) {
			throw LOG.getInvalidParameterCountForExecutableException(
					ExecutableHelper.getExecutableAsString(
							executableMetaData.getType().toString() + "#" + executableMetaData.getName(),
							executableMetaData.getParameterTypes()
					),
					executableMetaData.getParameterTypes().length,
					parameterValues.length
			);
		}

		if ( beanMetaData.defaultGroupSequenceIsRedefined() ) {
			validationOrder.assertDefaultGroupSequenceIsExpandable(
					beanMetaData.getDefaultGroupSequence(
							validationContext.getRootBean()
					)
			);
		}

		// process first single groups
		Iterator<Group> groupIterator = validationOrder.getGroupIterator();
		while ( groupIterator.hasNext() ) {
			validateParametersForGroup( validationContext, executableMetaData, parameterValues, groupIterator.next() );
			if ( shouldFailFast( validationContext ) ) {
				return;
			}
		}

		ValueContext<Object[], Object> cascadingValueContext = ValueContext.getLocalExecutionContext(
				beanMetaDataManager,
				validatorScopedContext.getParameterNameProvider(),
				parameterValues,
				executableMetaData.getValidatableParametersMetaData(),
				PathImpl.createPathForExecutable( executableMetaData )
		);

		groupIterator = validationOrder.getGroupIterator();
		while ( groupIterator.hasNext() ) {
			Group group = groupIterator.next();
			cascadingValueContext.setCurrentGroup( group.getDefiningClass() );
			validateCascadedConstraints( validationContext, cascadingValueContext );
			if ( shouldFailFast( validationContext ) ) {
				return;
			}
		}

		// now process sequences, stop after the first erroneous group
		Iterator<Sequence> sequenceIterator = validationOrder.getSequenceIterator();
		while ( sequenceIterator.hasNext() ) {
			Sequence sequence = sequenceIterator.next();
			for ( GroupWithInheritance groupOfGroups : sequence ) {
				int numberOfViolations = validationContext.getFailingConstraints().size();

				for ( Group group : groupOfGroups ) {
					validateParametersForGroup( validationContext, executableMetaData, parameterValues, group );
					if ( shouldFailFast( validationContext ) ) {
						return;
					}

					cascadingValueContext.setCurrentGroup( group.getDefiningClass() );
					validateCascadedConstraints( validationContext, cascadingValueContext );

					if ( shouldFailFast( validationContext ) ) {
						return;
					}
				}

				if ( validationContext.getFailingConstraints().size() > numberOfViolations ) {
					break;
				}
			}
		}
	}

	private <T> void validateParametersForGroup(ValidationContext<T> validationContext, ExecutableMetaData executableMetaData, Object[] parameterValues,
			Group group) {
		Contracts.assertNotNull( executableMetaData, "executableMetaData may not be null" );

		// TODO GM: define behavior with respect to redefined default sequences. Should only the
		// sequence from the validated bean be honored or also default sequence definitions up in
		// the inheritance tree?
		// For now a redefined default sequence will only be considered if specified at the bean
		// hosting the validated itself, but no other default sequence from parent types
		if ( group.isDefaultGroup() ) {
			Iterator<Sequence> defaultGroupSequence = validationContext.getRootBeanMetaData().getDefaultValidationSequence( validationContext.getRootBean() );

			while ( defaultGroupSequence.hasNext() ) {
				Sequence sequence = defaultGroupSequence.next();
				int numberOfViolations = validationContext.getFailingConstraints().size();

				for ( GroupWithInheritance expandedGroup : sequence ) {
					for ( Group defaultGroupSequenceElement : expandedGroup ) {
						validateParametersForSingleGroup( validationContext, parameterValues, executableMetaData, defaultGroupSequenceElement.getDefiningClass() );

						if ( shouldFailFast( validationContext ) ) {
							return;
						}
					}

					//stop processing after first group with errors occurred
					if ( validationContext.getFailingConstraints().size() > numberOfViolations ) {
						return;
					}
				}
			}
		}
		else {
			validateParametersForSingleGroup( validationContext, parameterValues, executableMetaData, group.getDefiningClass() );
		}
	}

	private <T> void validateParametersForSingleGroup(ValidationContext<T> validationContext, Object[] parameterValues, ExecutableMetaData executableMetaData, Class<?> currentValidatedGroup) {
		if ( !executableMetaData.getCrossParameterConstraints().isEmpty() ) {
			ValueContext<T, Object> valueContext = getExecutableValueContext(
					validationContext.getRootBean(), executableMetaData, executableMetaData.getValidatableParametersMetaData(), currentValidatedGroup
			);

			// 1. validate cross-parameter constraints
			validateMetaConstraints( validationContext, valueContext, parameterValues, executableMetaData.getCrossParameterConstraints() );
			if ( shouldFailFast( validationContext ) ) {
				return;
			}
		}

		ValueContext<T, Object> valueContext = getExecutableValueContext(
				validationContext.getRootBean(), executableMetaData, executableMetaData.getValidatableParametersMetaData(), currentValidatedGroup
		);

		// 2. validate parameter constraints
		for ( int i = 0; i < parameterValues.length; i++ ) {
			ParameterMetaData parameterMetaData = executableMetaData.getParameterMetaData( i );
			Object value = parameterValues[i];

			if ( value != null ) {
				Class<?> valueType = value.getClass();
				if ( parameterMetaData.getType() instanceof Class && ( (Class<?>) parameterMetaData.getType() ).isPrimitive() ) {
					valueType = ReflectionHelper.unBoxedType( valueType );
				}
				if ( !TypeHelper.isAssignable(
						TypeHelper.getErasedType( parameterMetaData.getType() ),
						valueType
				) ) {
					throw LOG.getParameterTypesDoNotMatchException(
							valueType,
							parameterMetaData.getType(),
							i,
							validationContext.getExecutable()
					);
				}
			}

			validateMetaConstraints( validationContext, valueContext, parameterValues, parameterMetaData );
			if ( shouldFailFast( validationContext ) ) {
				return;
			}
		}
	}

	private <T> ValueContext<T, Object> getExecutableValueContext(T object, ExecutableMetaData executableMetaData, Validatable validatable, Class<?> group) {
		ValueContext<T, Object> valueContext;

		if ( object != null ) {
			valueContext = ValueContext.getLocalExecutionContext(
					beanMetaDataManager,
					validatorScopedContext.getParameterNameProvider(),
					object,
					validatable,
					PathImpl.createPathForExecutable( executableMetaData )
			);
		}
		else {
			valueContext = ValueContext.getLocalExecutionContext(
					beanMetaDataManager,
					validatorScopedContext.getParameterNameProvider(),
					(Class<T>) null, //the type is not required in this case (only for cascaded validation)
					validatable,
					PathImpl.createPathForExecutable( executableMetaData )
			);
		}

		valueContext.setCurrentGroup( group );

		return valueContext;
	}

	private <V, T> void validateReturnValueInContext(ValidationContext<T> validationContext, T bean, V value, ValidationOrder validationOrder) {
		BeanMetaData<T> beanMetaData = validationContext.getRootBeanMetaData();

		Optional<ExecutableMetaData> executableMetaDataOptional = beanMetaData.getMetaDataFor( validationContext.getExecutable() );

		if ( !executableMetaDataOptional.isPresent() ) {
			// the method is unconstrained
			return;
		}

		ExecutableMetaData executableMetaData = executableMetaDataOptional.get();

		if ( beanMetaData.defaultGroupSequenceIsRedefined() ) {
			validationOrder.assertDefaultGroupSequenceIsExpandable( beanMetaData.getDefaultGroupSequence( bean ) );
		}

		Iterator<Group> groupIterator = validationOrder.getGroupIterator();

		// process first single groups
		while ( groupIterator.hasNext() ) {
			validateReturnValueForGroup( validationContext, executableMetaData, bean, value, groupIterator.next() );
			if ( shouldFailFast( validationContext ) ) {
				return;
			}
		}

		ValueContext<V, Object> cascadingValueContext = null;

		if ( value != null ) {
			cascadingValueContext = ValueContext.getLocalExecutionContext(
					beanMetaDataManager,
					validatorScopedContext.getParameterNameProvider(),
					value,
					executableMetaData.getReturnValueMetaData(),
					PathImpl.createPathForExecutable( executableMetaData )
			);

			groupIterator = validationOrder.getGroupIterator();
			while ( groupIterator.hasNext() ) {
				Group group = groupIterator.next();
				cascadingValueContext.setCurrentGroup( group.getDefiningClass() );
				validateCascadedConstraints( validationContext, cascadingValueContext );
				if ( shouldFailFast( validationContext ) ) {
					return;
				}
			}
		}

		// now process sequences, stop after the first erroneous group
		Iterator<Sequence> sequenceIterator = validationOrder.getSequenceIterator();
		while ( sequenceIterator.hasNext() ) {
			Sequence sequence = sequenceIterator.next();
			for ( GroupWithInheritance groupOfGroups : sequence ) {
				int numberOfFailingConstraintsBeforeGroup = validationContext.getFailingConstraints().size();
				for ( Group group : groupOfGroups ) {
					validateReturnValueForGroup( validationContext, executableMetaData, bean, value, group );
					if ( shouldFailFast( validationContext ) ) {
						return;
					}

					if ( value != null ) {
						cascadingValueContext.setCurrentGroup( group.getDefiningClass() );
						validateCascadedConstraints( validationContext, cascadingValueContext );

						if ( shouldFailFast( validationContext ) ) {
							return;
						}
					}
				}

				if ( validationContext.getFailingConstraints().size() > numberOfFailingConstraintsBeforeGroup ) {
					break;
				}
			}
		}
	}

	//TODO GM: if possible integrate with validateParameterForGroup()
	private <T> void validateReturnValueForGroup(ValidationContext<T> validationContext, ExecutableMetaData executableMetaData, T bean, Object value,
			Group group) {
		Contracts.assertNotNull( executableMetaData, "executableMetaData may not be null" );

		// TODO GM: define behavior with respect to redefined default sequences. Should only the
		// sequence from the validated bean be honored or also default sequence definitions up in
		// the inheritance tree?
		// For now a redefined default sequence will only be considered if specified at the bean
		// hosting the validated itself, but no other default sequence from parent types

		if ( group.isDefaultGroup() ) {
			Iterator<Sequence> defaultGroupSequence = validationContext.getRootBeanMetaData().getDefaultValidationSequence( bean );

			while ( defaultGroupSequence.hasNext() ) {
				Sequence sequence = defaultGroupSequence.next();
				int numberOfViolations = validationContext.getFailingConstraints().size();

				for ( GroupWithInheritance expandedGroup : sequence ) {
					for ( Group defaultGroupSequenceElement : expandedGroup ) {
						validateReturnValueForSingleGroup( validationContext, executableMetaData, bean, value, defaultGroupSequenceElement.getDefiningClass() );

						if ( shouldFailFast( validationContext ) ) {
							return;
						}
					}

					//stop processing after first group with errors occurred
					if ( validationContext.getFailingConstraints().size() > numberOfViolations ) {
						return;
					}
				}
			}
		}
		else {
			validateReturnValueForSingleGroup( validationContext, executableMetaData, bean, value, group.getDefiningClass() );
		}
	}

	private <T> void validateReturnValueForSingleGroup(ValidationContext<T> validationContext, ExecutableMetaData executableMetaData, T bean, Object value, Class<?> oneGroup) {
		// validate constraints at return value itself
		ValueContext<?, Object> valueContext = getExecutableValueContext(
				executableMetaData.getKind() == ElementKind.CONSTRUCTOR ? value : bean,
				executableMetaData,
				executableMetaData.getReturnValueMetaData(),
				oneGroup
		);

		ReturnValueMetaData returnValueMetaData = executableMetaData.getReturnValueMetaData();

		validateMetaConstraints( validationContext, valueContext, value, returnValueMetaData );
	}

	/**
	 * Returns a value context pointing to the given property path relative to the specified root class for a given
	 * value.
	 *
	 * @param validationContext The validation context.
	 * @param propertyPath The property path for which constraints have to be collected.
	 * @return Returns an instance of {@code ValueContext} which describes the local validation context associated to
	 * the given property path.
	 */
	private <V> ValueContext<?, V> getValueContextForPropertyValidation(ValidationContext<?> validationContext, PathImpl propertyPath) {
		Class<?> clazz = validationContext.getRootBeanClass();
		BeanMetaData<?> beanMetaData = validationContext.getRootBeanMetaData();
		Object value = validationContext.getRootBean();
		PropertyMetaData propertyMetaData = null;

		Iterator<Path.Node> propertyPathIter = propertyPath.iterator();

		while ( propertyPathIter.hasNext() ) {
			// cast is ok, since we are dealing with engine internal classes
			NodeImpl propertyPathNode = (NodeImpl) propertyPathIter.next();
			propertyMetaData = getBeanPropertyMetaData( beanMetaData, propertyPathNode );

			// if the property is not the leaf property, we set up the context for the next iteration
			if ( propertyPathIter.hasNext() ) {
				if ( !propertyMetaData.isCascading() ) {
					throw LOG.getInvalidPropertyPathException( validationContext.getRootBeanClass(), propertyPath.asString() );
				}

				// TODO which cascadable???
				value = getCascadableValue( validationContext, value, propertyMetaData.getCascadables().iterator().next() );
				if ( value == null ) {
					throw LOG.getUnableToReachPropertyToValidateException( validationContext.getRootBean(), propertyPath );
				}
				clazz = value.getClass();

				// if we are in the case of an iterable and we want to validate an element of this iterable, we have to get the
				// element value
				if ( propertyPathNode.isIterable() ) {
					propertyPathNode = (NodeImpl) propertyPathIter.next();

					if ( propertyPathNode.getIndex() != null ) {
						value = ReflectionHelper.getIndexedValue( value, propertyPathNode.getIndex() );
					}
					else if ( propertyPathNode.getKey() != null ) {
						value = ReflectionHelper.getMappedValue( value, propertyPathNode.getKey() );
					}
					else {
						throw LOG.getPropertyPathMustProvideIndexOrMapKeyException();
					}

					if ( value == null ) {
						throw LOG.getUnableToReachPropertyToValidateException( validationContext.getRootBean(), propertyPath );
					}

					clazz = value.getClass();
					beanMetaData = beanMetaDataManager.getBeanMetaData( clazz );
					propertyMetaData = getBeanPropertyMetaData( beanMetaData, propertyPathNode );
				}
				else {
					beanMetaData = beanMetaDataManager.getBeanMetaData( clazz );
				}
			}
		}

		if ( propertyMetaData == null ) {
			// should only happen if the property path is empty, which should never happen
			throw LOG.getInvalidPropertyPathException( clazz, propertyPath.asString() );
		}

		validationContext.setValidatedProperty( propertyMetaData.getName() );
		propertyPath.removeLeafNode();

		return ValueContext.getLocalExecutionContext( validatorScopedContext.getParameterNameProvider(), value, beanMetaData, propertyPath );
	}

	/**
	 * Returns a value context pointing to the given property path relative to the specified root class without a value.
	 * <p>
	 * We are only able to use the static types as we don't have the value.
	 * </p>
	 *
	 * @param validationContext The validation context.
	 * @param propertyPath The property path for which constraints have to be collected.
	 * @return Returns an instance of {@code ValueContext} which describes the local validation context associated to
	 * the given property path.
	 */
	private <V> ValueContext<?, V> getValueContextForValueValidation(ValidationContext<?> validationContext,
			PathImpl propertyPath) {
		Class<?> clazz = validationContext.getRootBeanClass();
		BeanMetaData<?> beanMetaData = null;
		PropertyMetaData propertyMetaData = null;

		Iterator<Path.Node> propertyPathIter = propertyPath.iterator();

		while ( propertyPathIter.hasNext() ) {
			// cast is ok, since we are dealing with engine internal classes
			NodeImpl propertyPathNode = (NodeImpl) propertyPathIter.next();
			beanMetaData = beanMetaDataManager.getBeanMetaData( clazz );
			propertyMetaData = getBeanPropertyMetaData( beanMetaData, propertyPathNode );

			// if the property is not the leaf property, we set up the context for the next iteration
			if ( propertyPathIter.hasNext() ) {
				// if we are in the case of an iterable and we want to validate an element of this iterable, we have to get the
				// type from the parameterized type
				if ( propertyPathNode.isIterable() ) {
					propertyPathNode = (NodeImpl) propertyPathIter.next();

					clazz = ReflectionHelper.getClassFromType( ReflectionHelper.getCollectionElementType( propertyMetaData.getType() ) );
					beanMetaData = beanMetaDataManager.getBeanMetaData( clazz );
					propertyMetaData = getBeanPropertyMetaData( beanMetaData, propertyPathNode );
				}
				else {
					clazz = ReflectionHelper.getClassFromType( propertyMetaData.getType() );
				}
			}
		}

		if ( propertyMetaData == null ) {
			// should only happen if the property path is empty, which should never happen
			throw LOG.getInvalidPropertyPathException( clazz, propertyPath.asString() );
		}

		validationContext.setValidatedProperty( propertyMetaData.getName() );
		propertyPath.removeLeafNode();

		return ValueContext.getLocalExecutionContext( validatorScopedContext.getParameterNameProvider(), clazz, beanMetaData, propertyPath );
	}

	private boolean isValidationRequired(ValidationContext<?> validationContext,
			ValueContext<?, ?> valueContext,
			MetaConstraint<?> metaConstraint) {
		// validateProperty()/validateValue() call, but this constraint is for another property
		if ( validationContext.getValidatedProperty() != null &&
				!Objects.equals( validationContext.getValidatedProperty(), getPropertyName( metaConstraint.getLocation() ) ) ) {
				return false;
		}
		if ( validationContext.hasMetaConstraintBeenProcessed(
				valueContext.getCurrentBean(),
				valueContext.getPropertyPath(),
				metaConstraint
		) ) {
			return false;
		}

		if ( !metaConstraint.getGroupList().contains( valueContext.getCurrentGroup() ) ) {
			return false;
		}
		return isReachable(
				validationContext,
				valueContext.getCurrentBean(),
				valueContext.getPropertyPath(),
				metaConstraint.getElementType()
		);
	}

	private boolean isReachable(ValidationContext<?> validationContext, Object traversableObject, PathImpl path, ElementType type) {
		if ( needToCallTraversableResolver( path, type ) ) {
			return true;
		}

		Path pathToObject = path.getPathWithoutLeafNode();
		try {
			return validationContext.getTraversableResolver().isReachable(
					traversableObject,
					path.getLeafNode(),
					validationContext.getRootBeanClass(),
					pathToObject,
					type
			);
		}
		catch (RuntimeException e) {
			throw LOG.getErrorDuringCallOfTraversableResolverIsReachableException( e );
		}
	}

	private boolean needToCallTraversableResolver(PathImpl path, ElementType type) {
		// as the TraversableResolver interface is designed right now it does not make sense to call it when
		// there is no traversable object hosting the property to be accessed. For this reason we don't call the resolver
		// for class level constraints (ElementType.TYPE) or top level method parameters or return values.
		// see also BV expert group discussion - http://lists.jboss.org/pipermail/beanvalidation-dev/2013-January/000722.html
		return isClassLevelConstraint( type )
				|| isCrossParameterValidation( path )
				|| isParameterValidation( path )
				|| isReturnValueValidation( path );
	}

	private boolean isCascadeRequired(ValidationContext<?> validationContext, Object traversableObject, PathImpl path, ElementType type) {
		if ( needToCallTraversableResolver( path, type ) ) {
			return true;
		}

		boolean isReachable = isReachable( validationContext, traversableObject, path, type );
		if ( !isReachable ) {
			return false;
		}

		Path pathToObject = path.getPathWithoutLeafNode();
		try {
			return validationContext.getTraversableResolver().isCascadable(
					traversableObject,
					path.getLeafNode(),
					validationContext.getRootBeanClass(),
					pathToObject,
					type
			);
		}
		catch (RuntimeException e) {
			throw LOG.getErrorDuringCallOfTraversableResolverIsCascadableException( e );
		}
	}

	private boolean isClassLevelConstraint(ElementType type) {
		return ElementType.TYPE.equals( type );
	}

	private boolean isCrossParameterValidation(PathImpl path) {
		return path.getLeafNode().getKind() == ElementKind.CROSS_PARAMETER;
	}

	private boolean isParameterValidation(PathImpl path) {
		return path.getLeafNode().getKind() == ElementKind.PARAMETER;
	}

	private boolean isReturnValueValidation(PathImpl path) {
		return path.getLeafNode().getKind() == ElementKind.RETURN_VALUE;
	}

	private boolean shouldFailFast(ValidationContext<?> validationContext) {
		return validationContext.isFailFastModeEnabled() && !validationContext.getFailingConstraints().isEmpty();
	}

	private PropertyMetaData getBeanPropertyMetaData(BeanMetaData<?> beanMetaData, Path.Node propertyNode ) {
		if ( !ElementKind.PROPERTY.equals( propertyNode.getKind() ) ) {
			throw LOG.getInvalidPropertyPathException( beanMetaData.getBeanClass(), propertyNode.getName() );
		}

		return beanMetaData.getMetaDataFor( propertyNode.getName() );
	}

	private Object getCascadableValue(ValidationContext<?> validationContext, Object object, Cascadable cascadable) {
		return cascadable.getValue( object );
	}

	private String getPropertyName(ConstraintLocation location) {
		if ( location instanceof TypeArgumentConstraintLocation ) {
			location = ( (TypeArgumentConstraintLocation) location ).getOuterDelegate();
		}

		if ( location instanceof FieldConstraintLocation ) {
			return ( (FieldConstraintLocation) location ).getPropertyName();
		}
		else if ( location instanceof GetterConstraintLocation ) {
			return ( (GetterConstraintLocation) location ).getPropertyName();
		}

		return null;
	}
}
