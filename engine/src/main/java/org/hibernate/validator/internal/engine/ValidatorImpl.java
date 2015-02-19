/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine;

import java.lang.annotation.ElementType;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import javax.validation.ConstraintValidatorFactory;
import javax.validation.ConstraintViolation;
import javax.validation.ElementKind;
import javax.validation.MessageInterpolator;
import javax.validation.ParameterNameProvider;
import javax.validation.Path;
import javax.validation.TraversableResolver;
import javax.validation.Validator;
import javax.validation.executable.ExecutableValidator;
import javax.validation.groups.Default;
import javax.validation.metadata.BeanDescriptor;

import org.hibernate.validator.internal.engine.ValidationContext.ValidationContextBuilder;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorManager;
import org.hibernate.validator.internal.engine.groups.Group;
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
import org.hibernate.validator.internal.metadata.raw.ExecutableElement;
import org.hibernate.validator.internal.util.ConcurrentReferenceHashMap;
import org.hibernate.validator.internal.util.ConcurrentReferenceHashMap.ReferenceType;
import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.ReflectionHelper;
import org.hibernate.validator.internal.util.TypeHelper;
import org.hibernate.validator.internal.util.TypeResolutionHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.util.privilegedactions.GetDeclaredField;
import org.hibernate.validator.internal.util.privilegedactions.GetDeclaredMethod;
import org.hibernate.validator.internal.util.privilegedactions.SetAccessibility;
import org.hibernate.validator.spi.time.TimeProvider;
import org.hibernate.validator.spi.valuehandling.ValidatedValueUnwrapper;

import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashMap;
import static org.hibernate.validator.internal.util.logging.Messages.MESSAGES;

/**
 * The main Bean Validation class. This is the core processing class of Hibernate Validator.
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
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
	private final ParameterNameProvider parameterNameProvider;

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

	/**
	 * Keeps an accessible version for each non-accessible member whose value needs to be accessed during validation.
	 */
	private final ConcurrentMap<Member, Member> accessibleMembers;

	public ValidatorImpl(ConstraintValidatorFactory constraintValidatorFactory,
						 MessageInterpolator messageInterpolator,
						 TraversableResolver traversableResolver,
						 BeanMetaDataManager beanMetaDataManager,
						 ParameterNameProvider parameterNameProvider,
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

		this.accessibleMembers = new ConcurrentReferenceHashMap<Member, Member>(
				100,
				ReferenceType.SOFT,
				ReferenceType.SOFT
		);
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
				object,
				beanMetaDataManager.getBeanMetaData( object.getClass() ),
				PathImpl.createRootPath()
		);

		return validateInContext( valueContext, validationContext, validationOrder );
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

		return validatePropertyInContext(
				context,
				PathImpl.createPathFromString( propertyName ),
				validationOrder
		);
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

		return validateParameters( object, ExecutableElement.forMethod( method ), parameterValues, groups );
	}

	@Override
	public <T> Set<ConstraintViolation<T>> validateConstructorParameters(Constructor<? extends T> constructor, Object[] parameterValues, Class<?>... groups) {
		Contracts.assertNotNull( constructor, MESSAGES.validatedConstructorMustNotBeNull() );
		Contracts.assertNotNull( parameterValues, MESSAGES.validatedParameterArrayMustNotBeNull() );

		return validateParameters( null, ExecutableElement.forConstructor( constructor ), parameterValues, groups );
	}

	@Override
	public <T> Set<ConstraintViolation<T>> validateConstructorReturnValue(Constructor<? extends T> constructor, T createdObject, Class<?>... groups) {
		Contracts.assertNotNull( constructor, MESSAGES.validatedConstructorMustNotBeNull() );
		Contracts.assertNotNull( createdObject, MESSAGES.validatedConstructorCreatedInstanceMustNotBeNull() );

		return validateReturnValue( null, ExecutableElement.forConstructor( constructor ), createdObject, groups );
	}

	@Override
	public <T> Set<ConstraintViolation<T>> validateReturnValue(T object, Method method, Object returnValue, Class<?>... groups) {
		Contracts.assertNotNull( object, MESSAGES.validatedObjectMustNotBeNull() );
		Contracts.assertNotNull( method, MESSAGES.validatedMethodMustNotBeNull() );

		return validateReturnValue( object, ExecutableElement.forMethod( method ), returnValue, groups );
	}

	private <T> Set<ConstraintViolation<T>> validateParameters(T object, ExecutableElement executable, Object[] parameterValues, Class<?>... groups) {
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

	private <T> Set<ConstraintViolation<T>> validateReturnValue(T object, ExecutableElement executable, Object returnValue, Class<?>... groups) {
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
	 *
	 * @param valueContext the current validation context
	 * @param context the global validation context
	 * @param validationOrder Contains the information which and in which order groups have to be executed
	 * @param <T> The root bean type
	 *
	 * @return Set of constraint violations or the empty set if there were no violations.
	 */
	private <T, U> Set<ConstraintViolation<T>> validateInContext(ValueContext<U, Object> valueContext, ValidationContext<T> context, ValidationOrder validationOrder) {
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
			for ( Group group : sequence.getComposingGroups() ) {
				int numberOfViolations = context.getFailingConstraints().size();
				valueContext.setCurrentGroup( group.getDefiningClass() );

				validateConstraintsForCurrentGroup( context, valueContext );
				if ( shouldFailFast( context ) ) {
					return context.getFailingConstraints();
				}

				validateCascadedConstraints( context, valueContext );
				if ( shouldFailFast( context ) ) {
					return context.getFailingConstraints();
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
			List<Class<?>> defaultGroupSequence = hostingBeanMetaData.getDefaultGroupSequence( valueContext.getCurrentBean() );
			Set<MetaConstraint<?>> metaConstraints = hostingBeanMetaData.getDirectMetaConstraints();

			// if the current class redefined the default group sequence, this sequence has to be applied to all the class hierarchy.
			if ( defaultGroupSequenceIsRedefined ) {
				metaConstraints = hostingBeanMetaData.getMetaConstraints();
			}

			PathImpl currentPath = valueContext.getPropertyPath();
			for ( Class<?> defaultSequenceMember : defaultGroupSequence ) {
				valueContext.setCurrentGroup( defaultSequenceMember );
				boolean validationSuccessful = true;
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

					boolean tmp = validateConstraint( validationContext, valueContext, false, metaConstraint );
					if ( shouldFailFast( validationContext ) ) {
						return;
					}
					validationSuccessful = validationSuccessful && tmp;
					// reset property path
					valueContext.setPropertyPath( currentPath );
				}
				if ( !validationSuccessful ) {
					break;
				}
			}
			validationContext.markCurrentBeanAsProcessed( valueContext );

			// all constraints in the hierarchy has been validated, stop validation.
			if ( defaultGroupSequenceIsRedefined ) {
				break;
			}
		}
	}

	private void validateConstraintsForNonDefaultGroup(ValidationContext<?> validationContext, ValueContext<?, Object> valueContext) {
		BeanMetaData<?> beanMetaData = beanMetaDataManager.getBeanMetaData( valueContext.getCurrentBeanType() );
		PathImpl currentPath = valueContext.getPropertyPath();
		for ( MetaConstraint<?> metaConstraint : beanMetaData.getMetaConstraints() ) {
			validateConstraint( validationContext, valueContext, false, metaConstraint );
			if ( shouldFailFast( validationContext ) ) {
				return;
			}
			// reset the path to the state before this call
			valueContext.setPropertyPath( currentPath );
		}
		validationContext.markCurrentBeanAsProcessed( valueContext );
	}

	private boolean validateConstraint(ValidationContext<?> validationContext,
									   ValueContext<?, Object> valueContext,
									   boolean propertyPathComplete,
									   MetaConstraint<?> metaConstraint) {

		boolean validationSuccessful = true;

		if ( metaConstraint.getElementType() != ElementType.TYPE ) {
			PropertyMetaData propertyMetaData = beanMetaDataManager.getBeanMetaData( valueContext.getCurrentBeanType() ).getMetaDataFor(
				ReflectionHelper.getPropertyName( metaConstraint.getLocation().getMember() )
			);

			if ( !propertyPathComplete ) {
				valueContext.appendNode( propertyMetaData );
			}
			// set the unwrapping mode for this validation
			valueContext.setUnwrapMode( propertyMetaData.unwrapMode() );
		}
		else {
			valueContext.appendBeanNode();
		}

		if ( isValidationRequired( validationContext, valueContext, metaConstraint ) ) {
			if ( valueContext.getCurrentBean() != null ) {
				Object valueToValidate = getValue(
						metaConstraint.getLocation().getMember(),
						valueContext.getCurrentBean()
				);
				valueContext.setCurrentValidatedValue( valueToValidate );
			}
			validationSuccessful = metaConstraint.validateConstraint( validationContext, valueContext );
		}

		// reset the unwrapping mode
		valueContext.setUnwrapMode( UnwrapMode.AUTOMATIC );
		return validationSuccessful;
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

				Object value = getValue( valueContext.getCurrentBean(), validationContext, cascadable );

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
								valueIter,
								false,
								valueContext,
								validationOrder,
								Collections.<MetaConstraint<?>>emptySet()
						);
						if ( shouldFailFast( validationContext ) ) {
							return;
						}
					}

					// Second, validate the content of the value
					Iterator<?> elementsIter = createIteratorForCascadedValue( type, value, valueContext );
					boolean isIndexable = isIndexable( type );

					validateCascadedConstraint(
							validationContext,
							elementsIter,
							isIndexable,
							valueContext,
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
	 *
	 * @param type the type of the cascaded field or property.
	 * @param value the actual value.
	 * @param valueContext context object containing state about the currently validated instance
	 *
	 * @return An iterator over the value of a cascaded property.
	 */
	private Iterator<?> createIteratorForCascadedValue(Type type, Object value, ValueContext<?, ?> valueContext) {
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

	/**
	 * Called when processing cascaded constraints. This methods inspects the type of the cascaded constraints and in case
	 * of a list or array creates an iterator in order to validate each element.
	 *
	 * @param type the type of the cascaded field or property.
	 *
	 * @return An iterator over the value of a cascaded property.
	 */
	private boolean isIndexable(Type type) {
		boolean isIndexable = false;
		if ( ReflectionHelper.isList( type ) ) {
			isIndexable = true;
		}
		else if ( ReflectionHelper.isMap( type ) ) {
			isIndexable = true;
		}
		else if ( TypeHelper.isArray( type ) ) {
			isIndexable = true;
		}
		return isIndexable;
	}

	private void validateCascadedConstraint(ValidationContext<?> context, Iterator<?> iter, boolean isIndexable, ValueContext<?,
			Object> valueContext, ValidationOrder validationOrder, Set<MetaConstraint<?>> typeArgumentsConstraint) {
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
				validateTypeArgumentConstraints( context, valueContext, value, typeArgumentsConstraint );

				// Cascade validation
				ValueContext<?, Object> newValueContext;
				if ( value != null ) {
					newValueContext = ValueContext.getLocalExecutionContext(
							value,
							beanMetaDataManager.getBeanMetaData( value.getClass() ),
							valueContext.getPropertyPath()
					);
				}
				else {
					newValueContext = ValueContext.getLocalExecutionContext(
							valueContext.getCurrentBeanType(),
							beanMetaDataManager.getBeanMetaData( valueContext.getCurrentBeanType() ),
							valueContext.getPropertyPath()
					);
				}

				validateInContext( newValueContext, context, validationOrder );
				if ( shouldFailFast( context ) ) {
					return;
				}
			}
			i++;
		}
	}

	private void validateTypeArgumentConstraints(ValidationContext<?> context,
												 ValueContext<?, Object> valueContext,
												 Object value,
												 Set<MetaConstraint<?>> typeArgumentsConstraints) {
		valueContext.setCurrentValidatedValue( value );
		for ( MetaConstraint<?> metaConstraint : typeArgumentsConstraints ) {
			metaConstraint.validateConstraint( context, valueContext );
			if ( shouldFailFast( context ) ) {
				return;
			}
		}
	}

	private <T> Set<ConstraintViolation<T>> validatePropertyInContext(ValidationContext<T> context, PathImpl propertyPath, ValidationOrder validationOrder) {
		List<MetaConstraint<?>> metaConstraints = newArrayList();
		Iterator<Path.Node> propertyIter = propertyPath.iterator();
		ValueContext<?, Object> valueContext = collectMetaConstraintsForPath(
				context,
				propertyIter,
				propertyPath,
				metaConstraints
		);

		if ( valueContext.getCurrentBean() == null ) {
			throw log.getInvalidPropertyPathException();
		}

		if ( metaConstraints.size() == 0 ) {
			return context.getFailingConstraints();
		}

		assertDefaultGroupSequenceIsExpandable( valueContext, validationOrder );

		// process first single groups
		Iterator<Group> groupIterator = validationOrder.getGroupIterator();
		while ( groupIterator.hasNext() ) {
			Group group = groupIterator.next();
			valueContext.setCurrentGroup( group.getDefiningClass() );
			validatePropertyForCurrentGroup( valueContext, context, metaConstraints );
			if ( shouldFailFast( context ) ) {
				return context.getFailingConstraints();
			}
		}

		// now process sequences, stop after the first erroneous group
		Iterator<Sequence> sequenceIterator = validationOrder.getSequenceIterator();
		while ( sequenceIterator.hasNext() ) {
			Sequence sequence = sequenceIterator.next();
			for ( Group group : sequence.getComposingGroups() ) {
				valueContext.setCurrentGroup( group.getDefiningClass() );
				int numberOfConstraintViolations = validatePropertyForCurrentGroup(
						valueContext, context, metaConstraints
				);
				if ( shouldFailFast( context ) ) {
					return context.getFailingConstraints();
				}
				if ( numberOfConstraintViolations > 0 ) {
					break;
				}
			}
		}

		return context.getFailingConstraints();
	}

	private <T> void assertDefaultGroupSequenceIsExpandable(ValueContext<T, ?> valueContext, ValidationOrder validationOrder) {
		BeanMetaData<T> beanMetaData = beanMetaDataManager.getBeanMetaData( valueContext.getCurrentBeanType() );
		if ( beanMetaData.defaultGroupSequenceIsRedefined() ) {
			validationOrder.assertDefaultGroupSequenceIsExpandable( beanMetaData.getDefaultGroupSequence( valueContext.getCurrentBean() ) );
		}
	}

	private <T> Set<ConstraintViolation<T>> validateValueInContext(ValidationContext<T> context, Object value, PathImpl propertyPath, ValidationOrder validationOrder) {
		List<MetaConstraint<?>> metaConstraints = newArrayList();
		ValueContext<?, Object> valueContext = collectMetaConstraintsForPath(
				context, propertyPath.iterator(), propertyPath, metaConstraints
		);
		valueContext.setCurrentValidatedValue( value );

		if ( metaConstraints.size() == 0 ) {
			return context.getFailingConstraints();
		}

		BeanMetaData<?> beanMetaData = beanMetaDataManager.getBeanMetaData( valueContext.getCurrentBeanType() );
		if ( beanMetaData.defaultGroupSequenceIsRedefined() ) {
			validationOrder.assertDefaultGroupSequenceIsExpandable( beanMetaData.getDefaultGroupSequence( null ) );
		}

		// process first single groups
		Iterator<Group> groupIterator = validationOrder.getGroupIterator();
		while ( groupIterator.hasNext() ) {
			Group group = groupIterator.next();
			valueContext.setCurrentGroup( group.getDefiningClass() );
			validatePropertyForCurrentGroup( valueContext, context, metaConstraints );
			if ( shouldFailFast( context ) ) {
				return context.getFailingConstraints();
			}
		}

		// now process sequences, stop after the first erroneous group
		Iterator<Sequence> sequenceIterator = validationOrder.getSequenceIterator();
		while ( sequenceIterator.hasNext() ) {
			Sequence sequence = sequenceIterator.next();
			for ( Group group : sequence.getComposingGroups() ) {
				valueContext.setCurrentGroup( group.getDefiningClass() );
				int numberOfConstraintViolations = validatePropertyForCurrentGroup(
						valueContext, context, metaConstraints
				);
				if ( shouldFailFast( context ) ) {
					return context.getFailingConstraints();
				}
				if ( numberOfConstraintViolations > 0 ) {
					break;
				}
			}
		}

		return context.getFailingConstraints();
	}

	/**
	 * Validates the property constraints associated to the current {@code ValueContext} group.
	 *
	 * @param valueContext The current validation context.
	 * @param validationContext The global validation context.
	 * @param metaConstraints All constraints associated to the property.
	 *
	 * @return The number of constraint violations raised when validating the {@code ValueContext} current group.
	 */
	private int validatePropertyForCurrentGroup(ValueContext<?, Object> valueContext, ValidationContext<?> validationContext, List<MetaConstraint<?>> metaConstraints) {
		// we do not validate the default group, nothing special to do
		if ( !valueContext.validatingDefault() ) {
			return validatePropertyForNonDefaultGroup( valueContext, validationContext, metaConstraints );
		}

		// we are validating the default group, we have to consider that a class in the hierarchy could redefine the default group sequence
		return validatePropertyForDefaultGroup( valueContext, validationContext, metaConstraints );
	}

	/**
	 * Validates the property constraints for the current {@code ValueContext} group.
	 * <p>
	 * The current {@code ValueContext} group is not the default group.
	 * </p>
	 *
	 * @param valueContext The current validation context.
	 * @param validationContext The global validation context.
	 * @param metaConstraints All constraints associated to the property.
	 *
	 * @return The number of constraint violations raised when validating the {@code ValueContext} current group.
	 */
	private int validatePropertyForNonDefaultGroup(ValueContext<?, Object> valueContext, ValidationContext<?> validationContext, List<MetaConstraint<?>> metaConstraints) {
		int numberOfConstraintViolationsBefore = validationContext.getFailingConstraints().size();
		for ( MetaConstraint<?> metaConstraint : metaConstraints ) {
			validateConstraint( validationContext, valueContext, true, metaConstraint );
			if ( shouldFailFast( validationContext ) ) {
				return validationContext.getFailingConstraints()
						.size() - numberOfConstraintViolationsBefore;
			}
		}
		return validationContext.getFailingConstraints().size() - numberOfConstraintViolationsBefore;
	}

	/**
	 * Validates the property for the default group.
	 * <p>
	 * This method checks that the default group sequence is not redefined in the class hierarchy for a superclass
	 * hosting constraints for the property to validate.
	 * </p>
	 *
	 * @param valueContext The current validation context.
	 * @param validationContext The global validation context.
	 * @param constraintList All constraints associated to the property to check.
	 *
	 * @return The number of constraint violations raised when validating the default group.
	 */
	private <U> int validatePropertyForDefaultGroup(ValueContext<U, Object> valueContext,
													ValidationContext<?> validationContext,
													List<MetaConstraint<?>> constraintList) {
		final int numberOfConstraintViolationsBefore = validationContext.getFailingConstraints().size();
		final BeanMetaData<U> beanMetaData = beanMetaDataManager.getBeanMetaData( valueContext.getCurrentBeanType() );
		final Map<Class<?>, Class<?>> validatedInterfaces = newHashMap();

		// evaluating the constraints of a bean per class in hierarchy. this is necessary to detect potential default group re-definitions
		for ( Class<? super U> clazz : beanMetaData.getClassHierarchy() ) {
			BeanMetaData<? super U> hostingBeanMetaData = beanMetaDataManager.getBeanMetaData( clazz );
			boolean defaultGroupSequenceIsRedefined = hostingBeanMetaData.defaultGroupSequenceIsRedefined();
			Set<MetaConstraint<?>> metaConstraints = hostingBeanMetaData.getDirectMetaConstraints();
			List<Class<?>> defaultGroupSequence = hostingBeanMetaData.getDefaultGroupSequence( valueContext.getCurrentBean() );

			if ( defaultGroupSequenceIsRedefined ) {
				metaConstraints = hostingBeanMetaData.getMetaConstraints();
			}

			for ( Class<?> groupClass : defaultGroupSequence ) {
				boolean validationSuccessful = true;
				valueContext.setCurrentGroup( groupClass );
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

					if ( constraintList.contains( metaConstraint ) ) {
						boolean tmp = validateConstraint( validationContext, valueContext, true, metaConstraint );

						validationSuccessful = validationSuccessful && tmp;
						if ( shouldFailFast( validationContext ) ) {
							return validationContext.getFailingConstraints()
									.size() - numberOfConstraintViolationsBefore;
						}
					}
				}
				if ( !validationSuccessful ) {
					break;
				}
			}
			// all the hierarchy has been validated, stop validation.
			if ( defaultGroupSequenceIsRedefined ) {
				break;
			}
		}
		return validationContext.getFailingConstraints().size() - numberOfConstraintViolationsBefore;
	}

	private <T> void validateParametersInContext(ValidationContext<T> validationContext,
												 Object[] parameterValues,
												 ValidationOrder validationOrder) {
		BeanMetaData<T> beanMetaData = beanMetaDataManager.getBeanMetaData( validationContext.getRootBeanClass() );
		ExecutableMetaData executableMetaData = beanMetaData.getMetaDataFor( validationContext.getExecutable() );

		if ( executableMetaData == null ) {
			// there is no executable metadata - specified object and method do not match
			throw log.getMethodOrConstructorNotDefinedByValidatedTypeException(
					beanMetaData.getBeanClass().getName(),
					validationContext.getExecutable().getMember()
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
				parameterValues,
				executableMetaData.getValidatableParametersMetaData(),
				PathImpl.createPathForExecutable( executableMetaData )
		);
		cascadingValueContext.setUnwrapMode( executableMetaData.unwrapMode() );

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
			for ( Group group : sequence.getComposingGroups() ) {
				int numberOfFailingConstraint = validateParametersForGroup(
						validationContext, parameterValues, group
				);
				if ( shouldFailFast( validationContext ) ) {
					return;
				}

				cascadingValueContext.setCurrentGroup( group.getDefiningClass() );
				validateCascadedConstraints( validationContext, cascadingValueContext );

				if ( shouldFailFast( validationContext ) ) {
					return;
				}

				if ( numberOfFailingConstraint > 0 ) {
					break;
				}
			}
		}
	}

	private <T> int validateParametersForGroup(ValidationContext<T> validationContext, Object[] parameterValues, Group group) {
		int numberOfViolationsBefore = validationContext.getFailingConstraints().size();

		BeanMetaData<T> beanMetaData = beanMetaDataManager.getBeanMetaData( validationContext.getRootBeanClass() );
		ExecutableMetaData executableMetaData = beanMetaData.getMetaDataFor( validationContext.getExecutable() );

		if ( parameterValues.length != executableMetaData.getParameterTypes().length ) {
			throw log.getInvalidParameterCountForExecutableException(
					ExecutableElement.getExecutableAsString(
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
		List<Class<?>> groupList;
		if ( group.isDefaultGroup() ) {
			groupList = beanMetaData.getDefaultGroupSequence( validationContext.getRootBean() );
		}
		else {
			groupList = Arrays.<Class<?>>asList( group.getDefiningClass() );
		}

		//the only case where we can have multiple groups here is a redefined default group sequence
		for ( Class<?> currentValidatedGroup : groupList ) {
			int numberOfViolationsOfCurrentGroup = 0;

			ValueContext<T, Object> valueContext = getExecutableValueContext(
					validationContext.getRootBean(), executableMetaData, currentValidatedGroup
			);
			valueContext.appendCrossParameterNode();
			valueContext.setCurrentValidatedValue( parameterValues );

			// 1. validate cross-parameter constraints
			numberOfViolationsOfCurrentGroup += validateConstraintsForGroup(
					validationContext, valueContext, executableMetaData.getCrossParameterConstraints()
			);
			if ( shouldFailFast( validationContext ) ) {
				return validationContext.getFailingConstraints().size() - numberOfViolationsBefore;
			}

			valueContext = getExecutableValueContext(
					validationContext.getRootBean(), executableMetaData, currentValidatedGroup
			);
			valueContext.setCurrentValidatedValue( parameterValues );

			// 2. validate parameter constraints
			for ( int i = 0; i < parameterValues.length; i++ ) {
				PathImpl originalPath = valueContext.getPropertyPath();

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
								valueType.getName(),
								parameterMetaData.getType().toString(),
								i,
								validationContext.getExecutable().getMember()
						);
					}
				}

				valueContext.appendNode( parameterMetaData );
				valueContext.setUnwrapMode( parameterMetaData.unwrapMode() );
				valueContext.setCurrentValidatedValue( value );

				numberOfViolationsOfCurrentGroup += validateConstraintsForGroup(
						validationContext, valueContext, parameterMetaData
				);
				if ( shouldFailFast( validationContext ) ) {
					return validationContext.getFailingConstraints().size() - numberOfViolationsBefore;
				}

				if ( !parameterMetaData.isCascading() ) {
					numberOfViolationsOfCurrentGroup += validateConstraintsForGroup(
							validationContext, valueContext, parameterMetaData.getTypeArgumentsConstraints()
					);
					if ( shouldFailFast( validationContext ) ) {
						return validationContext.getFailingConstraints().size() - numberOfViolationsBefore;
					}
				}

				valueContext.setPropertyPath( originalPath );
			}

			//stop processing after first group with errors occurred
			if ( numberOfViolationsOfCurrentGroup > 0 ) {
				break;
			}
		}

		return validationContext.getFailingConstraints().size() - numberOfViolationsBefore;
	}

	private <T> ValueContext<T, Object> getExecutableValueContext(T object, ExecutableMetaData executableMetaData, Class<?> group) {
		ValueContext<T, Object> valueContext;

		if ( object != null ) {
			valueContext = ValueContext.getLocalExecutionContext(
					object,
					null,
					PathImpl.createPathForExecutable( executableMetaData )
			);
		}
		else {
			valueContext = ValueContext.getLocalExecutionContext(
					(Class<T>) null, //the type is not required in this case (only for cascaded validation)
					null,
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
			for ( Group group : sequence.getComposingGroups() ) {
				int numberOfFailingConstraint = validateReturnValueForGroup(
						context, bean, value, group
				);
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

				if ( numberOfFailingConstraint > 0 ) {
					break;
				}
			}
		}
	}

	//TODO GM: if possible integrate with validateParameterForGroup()
	private <T> int validateReturnValueForGroup(ValidationContext<T> validationContext, T bean, Object value, Group group) {
		int numberOfViolationsBefore = validationContext.getFailingConstraints().size();

		BeanMetaData<T> beanMetaData = beanMetaDataManager.getBeanMetaData( validationContext.getRootBeanClass() );
		ExecutableMetaData executableMetaData = beanMetaData.getMetaDataFor( validationContext.getExecutable() );

		if ( executableMetaData == null ) {
			// nothing to validate
			return 0;
		}

		// TODO GM: define behavior with respect to redefined default sequences. Should only the
		// sequence from the validated bean be honored or also default sequence definitions up in
		// the inheritance tree?
		// For now a redefined default sequence will only be considered if specified at the bean
		// hosting the validated itself, but no other default sequence from parent types

		List<Class<?>> groupList;
		if ( group.isDefaultGroup() ) {
			groupList = beanMetaData.getDefaultGroupSequence( bean );
		}
		else {
			groupList = Arrays.<Class<?>>asList( group.getDefiningClass() );
		}

		//the only case where we can have multiple groups here is a redefined default group sequence
		for ( Class<?> oneGroup : groupList ) {

			int numberOfViolationsOfCurrentGroup = 0;

			// validate constraints at return value itself
			ValueContext<?, Object> valueContext = getExecutableValueContext(
					executableMetaData.getKind() == ElementKind.CONSTRUCTOR ? value : bean,
					executableMetaData,
					oneGroup
			);

			valueContext.setCurrentValidatedValue( value );
			ReturnValueMetaData returnValueMetaData = executableMetaData.getReturnValueMetaData();
			valueContext.appendNode( returnValueMetaData );
			valueContext.setUnwrapMode( returnValueMetaData.unwrapMode() );

			numberOfViolationsOfCurrentGroup +=
					validateConstraintsForGroup(
							validationContext, valueContext, executableMetaData
					);
			if ( shouldFailFast( validationContext ) ) {
				return validationContext.getFailingConstraints().size() - numberOfViolationsBefore;
			}

			//stop processing after first group with errors occurred
			if ( numberOfViolationsOfCurrentGroup > 0 ) {
				break;
			}
		}

		return validationContext.getFailingConstraints().size() - numberOfViolationsBefore;
	}

	private int validateConstraintsForGroup(ValidationContext<?> validationContext,
											ValueContext<?, ?> valueContext,
											Iterable<MetaConstraint<?>> constraints) {
		int numberOfViolationsBefore = validationContext.getFailingConstraints().size();

		for ( MetaConstraint<?> metaConstraint : constraints ) {
			if ( !isValidationRequired( validationContext, valueContext, metaConstraint ) ) {
				continue;
			}

			metaConstraint.validateConstraint( validationContext, valueContext );
			if ( shouldFailFast( validationContext ) ) {
				break;
			}
		}

		return validationContext.getFailingConstraints().size() - numberOfViolationsBefore;
	}

	/**
	 * Collects all {@code MetaConstraint}s which match the given path relative to the specified root class.
	 * <p>
	 * This method is called recursively.
	 * </p>
	 *
	 * @param validationContext The validation context.
	 * @param propertyIter An instance of {@code PropertyIterator} in order to iterate the items of the original property path.
	 * @param propertyPath The property path for which constraints have to be collected.
	 * @param metaConstraintsList An instance of {@code Map} where {@code MetaConstraint}s which match the given path are saved for each class in the hosting class hierarchy.
	 *
	 * @return Returns an instance of {@code ValueContext} which describes the local validation context associated to the given property path.
	 */
	private <V> ValueContext<?, V> collectMetaConstraintsForPath(ValidationContext validationContext,
			Iterator<Path.Node> propertyIter,
			PathImpl propertyPath,
			List<MetaConstraint<?>> metaConstraintsList) {
		Class<?> clazz = validationContext.getRootBeanClass();
		Object value = validationContext.getRootBean();

		// cast is ok, since we are dealing with engine internal classes
		NodeImpl elem = (NodeImpl) propertyIter.next();
		Object newValue = value;

		BeanMetaData<?> metaData = beanMetaDataManager.getBeanMetaData( clazz );
		PropertyMetaData property = metaData.getMetaDataFor( elem.getName() );

		// use precomputed method list as ReflectionHelper#containsMember is slow
		if ( property == null ) {
			throw log.getInvalidPropertyPathException( elem.getName(), metaData.getBeanClass().getName() );
		}
		else if ( !propertyIter.hasNext() ) {
			metaConstraintsList.addAll( property.getConstraints() );
		}
		else {
			if ( property.isCascading() ) {
				Type type = property.getType();
				newValue = newValue == null ? null : getValue( newValue, validationContext, property );
				if ( elem.isIterable() ) {
					if ( newValue != null && elem.getIndex() != null ) {
						newValue = ReflectionHelper.getIndexedValue( newValue, elem.getIndex() );
					}
					else if ( newValue != null && elem.getKey() != null ) {
						newValue = ReflectionHelper.getMappedValue( newValue, elem.getKey() );
					}
					else if ( newValue != null ) {
						throw log.getPropertyPathMustProvideIndexOrMapKeyException();
					}
					type = ReflectionHelper.getIndexedType( type );
				}

				ValidationContext newValidationContext;
				if ( newValue != null ) {
					newValidationContext = getValidationContext().forValidateProperty( newValue );
				}
				else {
					newValidationContext = getValidationContext().forValidateValue( (Class<?>) type );
				}
				return collectMetaConstraintsForPath(
						newValidationContext,
						propertyIter,
						propertyPath,
						metaConstraintsList
				);
			}
		}

		if ( newValue == null ) {
			return ValueContext.getLocalExecutionContext( clazz, null, propertyPath );
		}
		return ValueContext.getLocalExecutionContext( value, null, propertyPath );
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
		catch ( RuntimeException e ) {
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
		catch ( RuntimeException e ) {
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

	private Object getValue(Object object, ValidationContext validationContext, Cascadable cascadable) {
		Object value;
		if ( cascadable instanceof PropertyMetaData ) {
			Member member = getAccessible( ( (PropertyMetaData) cascadable ).getCascadingMember() );
			value = getValue( member, object );
		}
		else if ( cascadable instanceof ParameterMetaData ) {
			value = ( (Object[]) object )[( (ParameterMetaData) cascadable ).getIndex()];
		}
		else {
			value = object;
		}

		// Value can be wrapped (e.g. Optional<Address>). Try to unwrap it
		UnwrapMode unwrapMode = cascadable.unwrapMode();
		if ( UnwrapMode.UNWRAP.equals( unwrapMode )
				|| UnwrapMode.AUTOMATIC.equals( unwrapMode ) ) {
			ValidatedValueUnwrapper valueHandler = validationContext.getValidatedValueUnwrapper( cascadable.getType() );
			if ( valueHandler != null ) {
				value = valueHandler.handleValidatedValue( value );
			}
		}

		return value;
	}

	private Object getValue(Member member, Object object) {
		if ( member == null ) {
			return object;
		}

		member = getAccessible( member );

		if ( member instanceof Method ) {
			return ReflectionHelper.getValue( (Method) member, object );
		}
		else if ( member instanceof Field ) {
			return ReflectionHelper.getValue( (Field) member, object );
		}
		return null;
	}

	/**
	 * Returns an accessible version of the given member. Will be the given member itself in case it is accessible,
	 * otherwise a copy which is set accessible. These copies are maintained in the
	 * {@link ValidatorImpl#accessibleMembers} cache.
	 */
	private Member getAccessible(Member original) {
		if ( ( (AccessibleObject) original ).isAccessible() ) {
			return original;
		}

		Member member = accessibleMembers.get( original );

		if ( member != null ) {
			return member;
		}

		Class<?> clazz = original.getDeclaringClass();

		if ( original instanceof Field ) {
			member = run( GetDeclaredField.action( clazz, original.getName() ) );
		}
		else {
			member = run( GetDeclaredMethod.action( clazz, original.getName() ) );
		}

		run( SetAccessibility.action( member ) );

		Member cached = accessibleMembers.putIfAbsent( original, member );
		if ( cached != null ) {
			member = cached;
		}

		return member;
	}

	/**
	 * Runs the given privileged action, using a privileged block if required.
	 * <p>
	 * <b>NOTE:</b> This must never be changed into a publicly available method to avoid execution of arbitrary
	 * privileged actions within HV's protection domain.
	 */
	private <T> T run(PrivilegedAction<T> action) {
		return System.getSecurityManager() != null ? AccessController.doPrivileged( action ) : action.run();
	}
}
