/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashMap;
import static org.hibernate.validator.internal.util.logging.Messages.MESSAGES;

import java.lang.annotation.ElementType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintValidatorFactory;
import javax.validation.ConstraintViolation;
import javax.validation.ElementKind;
import javax.validation.MessageInterpolator;
import javax.validation.Path;
import javax.validation.TraversableResolver;
import javax.validation.Validator;
import javax.validation.executable.ExecutableValidator;
import javax.validation.groups.Default;
import javax.validation.metadata.BeanDescriptor;

import org.hibernate.validator.internal.engine.ValidationContext.ValidationContextBuilder;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorManager;
import org.hibernate.validator.internal.engine.groups.Group;
import org.hibernate.validator.internal.engine.groups.GroupWithInheritance;
import org.hibernate.validator.internal.engine.groups.Sequence;
import org.hibernate.validator.internal.engine.groups.ValidationOrder;
import org.hibernate.validator.internal.engine.groups.ValidationOrderGenerator;
import org.hibernate.validator.internal.engine.path.NodeImpl;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.hibernate.validator.internal.engine.resolver.CachingTraversableResolverForSingleValidation;
import org.hibernate.validator.internal.engine.valuehandling.UnwrapMode;
import org.hibernate.validator.internal.metadata.BeanMetaDataManager;
import org.hibernate.validator.internal.metadata.aggregated.BeanMetaData;
import org.hibernate.validator.internal.metadata.aggregated.ExecutableMetaData;
import org.hibernate.validator.internal.metadata.aggregated.ParameterMetaData;
import org.hibernate.validator.internal.metadata.aggregated.PropertyMetaData;
import org.hibernate.validator.internal.metadata.aggregated.ReturnValueMetaData;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.facets.Cascadable;
import org.hibernate.validator.internal.metadata.facets.Validatable;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.internal.metadata.location.PropertyConstraintLocation;
import org.hibernate.validator.internal.metadata.location.TypeArgumentConstraintLocation;
import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.ExecutableHelper;
import org.hibernate.validator.internal.util.ExecutableParameterNameProvider;
import org.hibernate.validator.internal.util.ReflectionHelper;
import org.hibernate.validator.internal.util.TypeHelper;
import org.hibernate.validator.internal.util.TypeResolutionHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.spi.time.TimeProvider;
import org.hibernate.validator.spi.valuehandling.ValidatedValueUnwrapper;

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

	private static final Log log = LoggerFactory.make();

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
	 * {@link MessageInterpolator} as passed to the constructor of this instance.
	 */
	private final MessageInterpolator messageInterpolator;

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

	/**
	 * Used for retrieving parameter names to be used in constraint violations or node names.
	 */
	private final ExecutableParameterNameProvider parameterNameProvider;

	private final TimeProvider timeProvider;

	/**
	 * Indicates if validation has to be stopped on first constraint violation.
	 */
	private final boolean failFast;

	/**
	 * Used for resolving generic type information.
	 */
	private final TypeResolutionHelper typeResolutionHelper;

	/**
	 * Contains handlers to be applied prior to validation when validating elements.
	 */
	private final List<ValidatedValueUnwrapper<?>> validatedValueHandlers;

	public ValidatorImpl(ConstraintValidatorFactory constraintValidatorFactory,
			MessageInterpolator messageInterpolator,
			TraversableResolver traversableResolver,
			BeanMetaDataManager beanMetaDataManager,
			ExecutableParameterNameProvider parameterNameProvider,
			TimeProvider timeProvider,
			TypeResolutionHelper typeResolutionHelper,
			List<ValidatedValueUnwrapper<?>> validatedValueHandlers,
			ConstraintValidatorManager constraintValidatorManager,
			boolean failFast) {
		this.constraintValidatorFactory = constraintValidatorFactory;
		this.messageInterpolator = messageInterpolator;
		this.traversableResolver = traversableResolver;
		this.beanMetaDataManager = beanMetaDataManager;
		this.parameterNameProvider = parameterNameProvider;
		this.timeProvider = timeProvider;
		this.typeResolutionHelper = typeResolutionHelper;
		this.validatedValueHandlers = validatedValueHandlers;
		this.constraintValidatorManager = constraintValidatorManager;
		this.failFast = failFast;

		validationOrderGenerator = new ValidationOrderGenerator();
	}

	@Override
	public final <T> Set<ConstraintViolation<T>> validate(T object, Class<?>... groups) {
		Contracts.assertNotNull( object, MESSAGES.validatedObjectMustNotBeNull() );

		if ( !beanMetaDataManager.isConstrained( object.getClass() ) ) {
			return Collections.emptySet();
		}

		ValidationOrder validationOrder = determineGroupValidationOrder( groups );
		ValidationContext<T> validationContext = getValidationContext().forValidate( object );

		ValueContext<?, Object> valueContext = ValueContext.getLocalExecutionContext(
				parameterNameProvider,
				object,
				beanMetaDataManager.getBeanMetaData( object.getClass() ),
				PathImpl.createRootPath()
		);

		return validateInContext( validationContext, valueContext, validationOrder );
	}

	@Override
	public final <T> Set<ConstraintViolation<T>> validateProperty(T object, String propertyName, Class<?>... groups) {
		Contracts.assertNotNull( object, MESSAGES.validatedObjectMustNotBeNull() );

		sanityCheckPropertyPath( propertyName );
		ValidationOrder validationOrder = determineGroupValidationOrder( groups );
		ValidationContext<T> context = getValidationContext().forValidateProperty( object );

		if ( !beanMetaDataManager.isConstrained( context.getRootBeanClass() ) ) {
			return Collections.emptySet();
		}

		PathImpl propertyPath = PathImpl.createPathFromString( propertyName );
		ValueContext<?, Object> valueContext = getValueContextForPropertyValidation( context, propertyPath );

		if ( valueContext.getCurrentBean() == null ) {
			throw log.getUnableToReachPropertyToValidateException( context.getRootBean(), propertyPath );
		}

		return validateInContext( context, valueContext, validationOrder );
	}

	@Override
	public final <T> Set<ConstraintViolation<T>> validateValue(Class<T> beanType, String propertyName, Object value, Class<?>... groups) {
		Contracts.assertNotNull( beanType, MESSAGES.beanTypeCannotBeNull() );

		if ( !beanMetaDataManager.isConstrained( beanType ) ) {
			return Collections.emptySet();
		}

		sanityCheckPropertyPath( propertyName );
		ValidationOrder validationOrder = determineGroupValidationOrder( groups );
		ValidationContext<T> context = getValidationContext().forValidateValue( beanType );

		return validateValueInContext(
				context,
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
		//this might be the case for parameterless methods
		if ( parameterValues == null ) {
			return Collections.emptySet();
		}

		ValidationOrder validationOrder = determineGroupValidationOrder( groups );

		ValidationContext<T> context = getValidationContext().forValidateParameters(
				parameterNameProvider,
				object,
				executable,
				parameterValues
		);

		if ( !beanMetaDataManager.isConstrained( context.getRootBeanClass() ) ) {
			return Collections.emptySet();
		}

		validateParametersInContext( context, parameterValues, validationOrder );

		return context.getFailingConstraints();
	}

	private <T> Set<ConstraintViolation<T>> validateReturnValue(T object, Executable executable, Object returnValue, Class<?>... groups) {
		ValidationOrder validationOrder = determineGroupValidationOrder( groups );

		ValidationContext<T> context = getValidationContext().forValidateReturnValue(
				object,
				executable,
				returnValue
		);

		if ( !beanMetaDataManager.isConstrained( context.getRootBeanClass() ) ) {
			return Collections.emptySet();
		}

		validateReturnValueInContext( context, object, returnValue, validationOrder );

		return context.getFailingConstraints();
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

		throw log.getTypeNotSupportedForUnwrappingException( type );
	}

	@Override
	public ExecutableValidator forExecutables() {
		return this;
	}

	private ValidationContextBuilder getValidationContext() {
		return ValidationContext.getValidationContext(
				constraintValidatorManager,
				messageInterpolator,
				constraintValidatorFactory,
				getCachingTraversableResolver(),
				timeProvider,
				validatedValueHandlers,
				typeResolutionHelper,
				failFast
		);
	}

	private void sanityCheckPropertyPath(String propertyName) {
		if ( propertyName == null || propertyName.length() == 0 ) {
			throw log.getInvalidPropertyPathException();
		}
	}

	private ValidationOrder determineGroupValidationOrder(Class<?>[] groups) {
		Contracts.assertNotNull( groups, MESSAGES.groupMustNotBeNull() );
		for ( Class<?> clazz : groups ) {
			if ( clazz == null ) {
				throw new IllegalArgumentException( MESSAGES.groupMustNotBeNull() );
			}
		}

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
	 * @param context the global validation context
	 * @param valueContext the current validation context
	 * @param validationOrder Contains the information which and in which order groups have to be executed
	 *
	 * @param <T> The root bean type
	 *
	 * @return Set of constraint violations or the empty set if there were no violations.
	 */
	private <T, U> Set<ConstraintViolation<T>> validateInContext(ValidationContext<T> context, ValueContext<U, Object> valueContext, ValidationOrder validationOrder) {
		if ( valueContext.getCurrentBean() == null ) {
			return Collections.emptySet();
		}

		BeanMetaData<U> beanMetaData = beanMetaDataManager.getBeanMetaData( valueContext.getCurrentBeanType() );
		if ( beanMetaData.defaultGroupSequenceIsRedefined() ) {
			validationOrder.assertDefaultGroupSequenceIsExpandable( beanMetaData.getDefaultGroupSequence( valueContext.getCurrentBean() ) );
		}

		// process first single groups. For these we can optimise object traversal by first running all validations on the current bean
		// before traversing the object.
		Iterator<Group> groupIterator = validationOrder.getGroupIterator();
		while ( groupIterator.hasNext() ) {
			Group group = groupIterator.next();
			valueContext.setCurrentGroup( group.getDefiningClass() );
			validateConstraintsForCurrentGroup( context, valueContext );
			if ( shouldFailFast( context ) ) {
				return context.getFailingConstraints();
			}
		}
		groupIterator = validationOrder.getGroupIterator();
		while ( groupIterator.hasNext() ) {
			Group group = groupIterator.next();
			valueContext.setCurrentGroup( group.getDefiningClass() );
			validateCascadedConstraints( context, valueContext );
			if ( shouldFailFast( context ) ) {
				return context.getFailingConstraints();
			}
		}

		// now we process sequences. For sequences I have to traverse the object graph since I have to stop processing when an error occurs.
		Iterator<Sequence> sequenceIterator = validationOrder.getSequenceIterator();
		while ( sequenceIterator.hasNext() ) {
			Sequence sequence = sequenceIterator.next();
			for ( GroupWithInheritance groupOfGroups : sequence ) {
				int numberOfViolations = context.getFailingConstraints().size();

				for ( Group group : groupOfGroups ) {
					valueContext.setCurrentGroup( group.getDefiningClass() );

					validateConstraintsForCurrentGroup( context, valueContext );
					if ( shouldFailFast( context ) ) {
						return context.getFailingConstraints();
					}

					validateCascadedConstraints( context, valueContext );
					if ( shouldFailFast( context ) ) {
						return context.getFailingConstraints();
					}
				}
				if ( context.getFailingConstraints().size() > numberOfViolations ) {
					break;
				}
			}
		}
		return context.getFailingConstraints();
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
		final BeanMetaData<U> beanMetaData = beanMetaDataManager.getBeanMetaData( valueContext.getCurrentBeanType() );
		final Map<Class<?>, Class<?>> validatedInterfaces = newHashMap();

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
		BeanMetaData<?> beanMetaData = beanMetaDataManager.getBeanMetaData( valueContext.getCurrentBeanType() );
		validateMetaConstraints( validationContext, valueContext, valueContext.getCurrentBean(), beanMetaData.getMetaConstraints() );
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
		PathImpl currentPath = valueContext.getPropertyPath();
		UnwrapMode currentUnwrapMode = valueContext.getUnwrapMode();

		boolean success = true;

		valueContext.appendNode( metaConstraint.getLocation() );

		if ( isValidationRequired( validationContext, valueContext, metaConstraint ) ) {
			valueContext.setUnwrapMode( valueContext.getCurrentValidatable().getUnwrapMode( metaConstraint.getLocation() ) );

			if ( parent != null ) {
				Object valueToValidate = valueContext.getValue( parent, metaConstraint.getLocation() );
				valueContext.setCurrentValidatedValue( valueToValidate );
			}

			success = metaConstraint.validateConstraint( validationContext, valueContext );
		}

		// reset the path to the state before this call
		valueContext.setPropertyPath( currentPath );
		valueContext.setUnwrapMode( currentUnwrapMode );

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
		PathImpl originalPath = valueContext.getPropertyPath();
		Class<?> originalGroup = valueContext.getCurrentGroup();

		for ( Cascadable cascadable : validatable.getCascadables() ) {
			valueContext.appendNode( cascadable );
			Class<?> group = cascadable.convertGroup( originalGroup );
			valueContext.setCurrentGroup( group );

			ElementType elementType = cascadable.getElementType();
			if ( isCascadeRequired(
					validationContext,
					valueContext.getCurrentBean(),
					valueContext.getPropertyPath(),
					elementType
			) ) {

				Object value = getCascadableValue( validationContext, valueContext.getCurrentBean(), cascadable );

				if ( value != null ) {

					// expand the group only if was created by group conversion;
					// otherwise we're looping through the right validation order
					// already and need only to pass the current element
					ValidationOrder validationOrder = validationOrderGenerator.getValidationOrder(
							group,
							group != originalGroup
					);

					Type type = value.getClass();

					// HV-902: First, validate the properties for beans that implements Iterable and Map
					if ( ReflectionHelper.isIterable( type ) || ReflectionHelper.isMap( type ) ) {
						Iterator<?> valueIter = Collections.singletonList( value ).iterator();
						validateCascadedConstraint(
								validationContext,
								valueContext,
								valueIter,
								false,
								validationOrder,
								Collections.<MetaConstraint<?>>emptySet()
						);
						if ( shouldFailFast( validationContext ) ) {
							return;
						}
					}

					// Second, validate the content of the value
					Iterator<?> elementsIter = createIteratorForCascadedValue( valueContext, type, value );
					boolean isIndexable = ReflectionHelper.isIndexable( type );

					validateCascadedConstraint(
							validationContext,
							valueContext,
							elementsIter,
							isIndexable,
							validationOrder,
							cascadable.getTypeArgumentsConstraints()
					);
					if ( shouldFailFast( validationContext ) ) {
						return;
					}
				}
			}

			// reset the path
			valueContext.setPropertyPath( originalPath );
			valueContext.setCurrentGroup( originalGroup );
		}
	}

	/**
	 * Called when processing cascaded constraints. This methods inspects the type of the cascaded constraints and in case
	 * of a list or array creates an iterator in order to validate each element.
	 * @param valueContext context object containing state about the currently validated instance
	 * @param type the type of the cascaded field or property.
	 * @param value the actual value.
	 *
	 * @return An iterator over the value of a cascaded property.
	 */
	private Iterator<?> createIteratorForCascadedValue(ValueContext<?, ?> valueContext, Type type, Object value) {
		Iterator<?> iter;
		if ( ReflectionHelper.isIterable( type ) ) {
			iter = ( (Iterable<?>) value ).iterator();
			valueContext.markCurrentPropertyAsIterable();
		}
		else if ( ReflectionHelper.isMap( type ) ) {
			Map<?, ?> map = (Map<?, ?>) value;
			iter = map.entrySet().iterator();
			valueContext.markCurrentPropertyAsIterable();
		}
		else if ( TypeHelper.isArray( type ) ) {
			List<?> arrayList = Arrays.asList( (Object[]) value );
			iter = arrayList.iterator();
			valueContext.markCurrentPropertyAsIterable();
		}
		else {
			iter = Collections.singletonList( value ).iterator();
		}
		return iter;
	}

	private void validateCascadedConstraint(ValidationContext<?> context, ValueContext<?, Object> valueContext, Iterator<?> iter, boolean isIndexable,
			ValidationOrder validationOrder, Set<MetaConstraint<?>> typeArgumentsConstraint) {
		Object value;
		Object mapKey;
		int i = 0;
		while ( iter.hasNext() ) {
			value = iter.next();
			if ( value instanceof Map.Entry ) {
				mapKey = ( (Map.Entry<?, ?>) value ).getKey();
				valueContext.setKey( mapKey );
				value = ( (Map.Entry<?, ?>) value ).getValue();
			}
			else if ( isIndexable ) {
				valueContext.setIndex( i );
			}

			if ( !context.isBeanAlreadyValidated(
					value,
					valueContext.getCurrentGroup(),
					valueContext.getPropertyPath()
			) ) {
				// Type arguments
				validateTypeArgumentConstraints( context, buildNewLocalExecutionContext( valueContext, value ), value, typeArgumentsConstraint );

				// Cascade validation
				validateInContext( context, buildNewLocalExecutionContext( valueContext, value ), validationOrder );
				if ( shouldFailFast( context ) ) {
					return;
				}
			}
			i++;
		}
	}

	private ValueContext<?, Object> buildNewLocalExecutionContext(ValueContext<?, Object> valueContext, Object value) {
		ValueContext<?, Object> newValueContext;
		if ( value != null ) {
			newValueContext = ValueContext.getLocalExecutionContext(
					parameterNameProvider,
					value,
					beanMetaDataManager.getBeanMetaData( value.getClass() ),
					valueContext.getPropertyPath()
			);
		}
		else {
			newValueContext = ValueContext.getLocalExecutionContext(
					parameterNameProvider,
					valueContext.getCurrentBeanType(),
					beanMetaDataManager.getBeanMetaData( valueContext.getCurrentBeanType() ),
					valueContext.getPropertyPath()
			);
		}
		return newValueContext;
	}

	private void validateTypeArgumentConstraints(ValidationContext<?> context,
			ValueContext<?, Object> valueContext,
			Object value,
			Set<MetaConstraint<?>> typeArgumentsConstraints) {
		PathImpl previousPath = valueContext.getPropertyPath();
		valueContext.setCurrentValidatedValue( value );
		for ( MetaConstraint<?> metaConstraint : typeArgumentsConstraints ) {
			valueContext.appendNode( metaConstraint.getLocation() );
			metaConstraint.validateConstraint( context, valueContext );
			valueContext.setPropertyPath( previousPath );
			if ( shouldFailFast( context ) ) {
				return;
			}
		}
	}

	private <T> Set<ConstraintViolation<T>> validateValueInContext(ValidationContext<T> context, Object value, PathImpl propertyPath, ValidationOrder validationOrder) {
		ValueContext<?, Object> valueContext = getValueContextForValueValidation( context, propertyPath );
		valueContext.setCurrentValidatedValue( value );

		BeanMetaData<?> beanMetaData = beanMetaDataManager.getBeanMetaData( valueContext.getCurrentBeanType() );
		if ( beanMetaData.defaultGroupSequenceIsRedefined() ) {
			validationOrder.assertDefaultGroupSequenceIsExpandable( beanMetaData.getDefaultGroupSequence( null ) );
		}

		// process first single groups
		Iterator<Group> groupIterator = validationOrder.getGroupIterator();
		while ( groupIterator.hasNext() ) {
			Group group = groupIterator.next();
			valueContext.setCurrentGroup( group.getDefiningClass() );
			validateConstraintsForCurrentGroup( context, valueContext );
			if ( shouldFailFast( context ) ) {
				return context.getFailingConstraints();
			}
		}

		// now process sequences, stop after the first erroneous group
		Iterator<Sequence> sequenceIterator = validationOrder.getSequenceIterator();
		while ( sequenceIterator.hasNext() ) {
			Sequence sequence = sequenceIterator.next();
			for ( GroupWithInheritance groupOfGroups : sequence ) {
				int numberOfConstraintViolationsBefore = context.getFailingConstraints().size();
				for ( Group group : groupOfGroups ) {
					valueContext.setCurrentGroup( group.getDefiningClass() );
					validateConstraintsForCurrentGroup( context, valueContext );
					if ( shouldFailFast( context ) ) {
						return context.getFailingConstraints();
					}
				}
				if ( context.getFailingConstraints().size() > numberOfConstraintViolationsBefore ) {
					break;
				}
			}
		}

		return context.getFailingConstraints();
	}

	private <T> void validateParametersInContext(ValidationContext<T> validationContext,
			Object[] parameterValues,
			ValidationOrder validationOrder) {
		BeanMetaData<T> beanMetaData = beanMetaDataManager.getBeanMetaData( validationContext.getRootBeanClass() );
		ExecutableMetaData executableMetaData = beanMetaData.getMetaDataFor( validationContext.getExecutable() );

		if ( executableMetaData == null ) {
			// there is no executable metadata - specified object and method do not match
			throw log.getMethodOrConstructorNotDefinedByValidatedTypeException(
					beanMetaData.getBeanClass(),
					validationContext.getExecutable()
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
			validateParametersForGroup( validationContext, parameterValues, groupIterator.next() );
			if ( shouldFailFast( validationContext ) ) {
				return;
			}
		}

		ValueContext<Object[], Object> cascadingValueContext = ValueContext.getLocalExecutionContext(
				parameterNameProvider,
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
					validateParametersForGroup( validationContext, parameterValues, group );
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

	private <T> void validateParametersForGroup(ValidationContext<T> validationContext, Object[] parameterValues, Group group) {
		BeanMetaData<T> beanMetaData = beanMetaDataManager.getBeanMetaData( validationContext.getRootBeanClass() );
		ExecutableMetaData executableMetaData = beanMetaData.getMetaDataFor( validationContext.getExecutable() );

		if ( parameterValues.length != executableMetaData.getParameterTypes().length ) {
			throw log.getInvalidParameterCountForExecutableException(
					ExecutableHelper.getExecutableAsString(
							executableMetaData.getType().toString() + "#" + executableMetaData.getName(),
							executableMetaData.getParameterTypes()
					), parameterValues.length, executableMetaData.getParameterTypes().length
			);
		}

		// TODO GM: define behavior with respect to redefined default sequences. Should only the
		// sequence from the validated bean be honored or also default sequence definitions up in
		// the inheritance tree?
		// For now a redefined default sequence will only be considered if specified at the bean
		// hosting the validated itself, but no other default sequence from parent types
		if ( group.isDefaultGroup() ) {
			Iterator<Sequence> defaultGroupSequence = beanMetaData.getDefaultValidationSequence( validationContext.getRootBean() );

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
					throw log.getParameterTypesDoNotMatchException(
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

			if ( !parameterMetaData.isCascading() ) {
				validateMetaConstraints( validationContext, valueContext, parameterValues, parameterMetaData.getTypeArgumentsConstraints() );
				if ( shouldFailFast( validationContext ) ) {
					return;
				}
			}
		}
	}

	private <T> ValueContext<T, Object> getExecutableValueContext(T object, ExecutableMetaData executableMetaData, Validatable validatable, Class<?> group) {
		ValueContext<T, Object> valueContext;

		if ( object != null ) {
			valueContext = ValueContext.getLocalExecutionContext(
					parameterNameProvider,
					object,
					validatable,
					PathImpl.createPathForExecutable( executableMetaData )
			);
		}
		else {
			valueContext = ValueContext.getLocalExecutionContext(
					parameterNameProvider,
					(Class<T>) null, //the type is not required in this case (only for cascaded validation)
					validatable,
					PathImpl.createPathForExecutable( executableMetaData )
			);
		}

		valueContext.setCurrentGroup( group );

		return valueContext;
	}

	private <V, T> void validateReturnValueInContext(ValidationContext<T> context, T bean, V value, ValidationOrder validationOrder) {
		BeanMetaData<T> beanMetaData = beanMetaDataManager.getBeanMetaData( context.getRootBeanClass() );
		ExecutableMetaData executableMetaData = beanMetaData.getMetaDataFor( context.getExecutable() );

		if ( executableMetaData == null ) {
			return;
		}

		if ( beanMetaData.defaultGroupSequenceIsRedefined() ) {
			validationOrder.assertDefaultGroupSequenceIsExpandable( beanMetaData.getDefaultGroupSequence( bean ) );
		}

		Iterator<Group> groupIterator = validationOrder.getGroupIterator();

		// process first single groups
		while ( groupIterator.hasNext() ) {
			validateReturnValueForGroup( context, bean, value, groupIterator.next() );
			if ( shouldFailFast( context ) ) {
				return;
			}
		}

		ValueContext<V, Object> cascadingValueContext = null;

		if ( value != null ) {
			cascadingValueContext = ValueContext.getLocalExecutionContext(
					parameterNameProvider,
					value,
					executableMetaData.getReturnValueMetaData(),
					PathImpl.createPathForExecutable( executableMetaData )
			);

			groupIterator = validationOrder.getGroupIterator();
			while ( groupIterator.hasNext() ) {
				Group group = groupIterator.next();
				cascadingValueContext.setCurrentGroup( group.getDefiningClass() );
				validateCascadedConstraints( context, cascadingValueContext );
				if ( shouldFailFast( context ) ) {
					return;
				}
			}
		}

		// now process sequences, stop after the first erroneous group
		Iterator<Sequence> sequenceIterator = validationOrder.getSequenceIterator();
		while ( sequenceIterator.hasNext() ) {
			Sequence sequence = sequenceIterator.next();
			for ( GroupWithInheritance groupOfGroups : sequence ) {
				int numberOfFailingConstraintsBeforeGroup = context.getFailingConstraints().size();
				for ( Group group : groupOfGroups ) {
					validateReturnValueForGroup( context, bean, value, group );
					if ( shouldFailFast( context ) ) {
						return;
					}

					if ( value != null ) {
						cascadingValueContext.setCurrentGroup( group.getDefiningClass() );
						validateCascadedConstraints( context, cascadingValueContext );

						if ( shouldFailFast( context ) ) {
							return;
						}
					}
				}

				if ( context.getFailingConstraints().size() > numberOfFailingConstraintsBeforeGroup ) {
					break;
				}
			}
		}
	}

	//TODO GM: if possible integrate with validateParameterForGroup()
	private <T> void validateReturnValueForGroup(ValidationContext<T> validationContext, T bean, Object value, Group group) {
		BeanMetaData<T> beanMetaData = beanMetaDataManager.getBeanMetaData( validationContext.getRootBeanClass() );
		ExecutableMetaData executableMetaData = beanMetaData.getMetaDataFor( validationContext.getExecutable() );

		if ( executableMetaData == null ) {
			// nothing to validate
			return;
		}

		// TODO GM: define behavior with respect to redefined default sequences. Should only the
		// sequence from the validated bean be honored or also default sequence definitions up in
		// the inheritance tree?
		// For now a redefined default sequence will only be considered if specified at the bean
		// hosting the validated itself, but no other default sequence from parent types

		if ( group.isDefaultGroup() ) {
			Iterator<Sequence> defaultGroupSequence = beanMetaData.getDefaultValidationSequence( bean );

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
		if ( shouldFailFast( validationContext ) ) {
			return;
		}

		if ( !returnValueMetaData.isCascading() ) {
			validateMetaConstraints( validationContext, valueContext, value, returnValueMetaData.getTypeArgumentsConstraints() );
			if ( shouldFailFast( validationContext ) ) {
				return;
			}
		}
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
		BeanMetaData<?> beanMetaData = null;
		Object value = validationContext.getRootBean();
		PropertyMetaData propertyMetaData = null;

		Iterator<Path.Node> propertyPathIter = propertyPath.iterator();

		while ( propertyPathIter.hasNext() ) {
			// cast is ok, since we are dealing with engine internal classes
			NodeImpl propertyPathNode = (NodeImpl) propertyPathIter.next();
			beanMetaData = beanMetaDataManager.getBeanMetaData( clazz );
			propertyMetaData = getBeanPropertyMetaData( beanMetaData, propertyPathNode );

			// if the property is not the leaf property, we set up the context for the next iteration
			if ( propertyPathIter.hasNext() ) {
				if ( !propertyMetaData.isCascading() ) {
					throw log.getInvalidPropertyPathException( validationContext.getRootBeanClass(), propertyPath.asString() );
				}

				value = getCascadableValue( validationContext, value, propertyMetaData );
				if ( value == null ) {
					throw log.getUnableToReachPropertyToValidateException( validationContext.getRootBean(), propertyPath );
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
						throw log.getPropertyPathMustProvideIndexOrMapKeyException();
					}

					if ( value == null ) {
						throw log.getUnableToReachPropertyToValidateException( validationContext.getRootBean(), propertyPath );
					}

					clazz = value.getClass();
					beanMetaData = beanMetaDataManager.getBeanMetaData( clazz );
					propertyMetaData = getBeanPropertyMetaData( beanMetaData, propertyPathNode );
				}
			}
		}

		if ( propertyMetaData == null ) {
			// should only happen if the property path is empty, which should never happen
			throw log.getInvalidPropertyPathException( clazz, propertyPath.asString() );
		}

		validationContext.setValidatedProperty( propertyMetaData.getName() );
		propertyPath.removeLeafNode();

		return ValueContext.getLocalExecutionContext( parameterNameProvider, value, beanMetaData, propertyPath );
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

					clazz = ReflectionHelper.getClassFromType( ReflectionHelper.getIndexedType( propertyMetaData.getType() ) );
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
			throw log.getInvalidPropertyPathException( clazz, propertyPath.asString() );
		}

		validationContext.setValidatedProperty( propertyMetaData.getName() );
		propertyPath.removeLeafNode();

		return ValueContext.getLocalExecutionContext( parameterNameProvider, clazz, beanMetaData, propertyPath );
	}

	/**
	 * Must be called and stored for the duration of the stack call
	 * A new instance is returned each time
	 *
	 * @return The resolver for the duration of a full validation.
	 */
	private TraversableResolver getCachingTraversableResolver() {
		return new CachingTraversableResolverForSingleValidation( traversableResolver );
	}

	private boolean isValidationRequired(ValidationContext<?> validationContext,
			ValueContext<?, ?> valueContext,
			MetaConstraint<?> metaConstraint) {
		// validateProperty()/validateValue() call, but this constraint is for another property
		if ( validationContext.getValidatedProperty() != null ) {
			String propertyName = getPropertyName( metaConstraint.getLocation() );
			if ( propertyName != null && !propertyName.equals( validationContext.getValidatedProperty() ) ) {
				return false;
			}
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
			throw log.getErrorDuringCallOfTraversableResolverIsReachableException( e );
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
			throw log.getErrorDuringCallOfTraversableResolverIsCascadableException( e );
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

	private boolean shouldFailFast(ValidationContext<?> context) {
		return context.isFailFastModeEnabled() && !context.getFailingConstraints().isEmpty();
	}

	private PropertyMetaData getBeanPropertyMetaData(BeanMetaData<?> beanMetaData, Path.Node propertyNode ) {
		if ( !ElementKind.PROPERTY.equals( propertyNode.getKind() ) ) {
			throw log.getInvalidPropertyPathException( beanMetaData.getBeanClass(), propertyNode.getName() );
		}

		PropertyMetaData propertyMetaData = beanMetaData.getMetaDataFor( propertyNode.getName() );

		if ( propertyMetaData == null ) {
			throw log.getInvalidPropertyPathException( beanMetaData.getBeanClass(), propertyNode.getName() );
		}
		return propertyMetaData;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Object getCascadableValue(ValidationContext<?> validationContext, Object object, Cascadable cascadable) {
		Object value = cascadable.getValue( object );

		// Value can be wrapped (e.g. Optional<Address>). Try to unwrap it
		UnwrapMode unwrapMode = cascadable.unwrapMode();
		if ( UnwrapMode.UNWRAP.equals( unwrapMode ) || UnwrapMode.AUTOMATIC.equals( unwrapMode ) ) {
			ValidatedValueUnwrapper valueHandler = validationContext.getValidatedValueUnwrapper( cascadable.getCascadableType() );
			if ( valueHandler != null ) {
				value = valueHandler.handleValidatedValue( value );
			}
		}

		return value;
	}

	private String getPropertyName(ConstraintLocation location) {
		if ( location instanceof PropertyConstraintLocation ) {
			return ( (PropertyConstraintLocation) location ).getPropertyName();
		}
		else if ( location instanceof TypeArgumentConstraintLocation ) {
			ConstraintLocation delegate = ( (TypeArgumentConstraintLocation) location ).getDelegate();
			if ( delegate instanceof PropertyConstraintLocation ) {
				return ( (PropertyConstraintLocation) delegate ).getPropertyName();
			}
		}

		return null;
	}
}
