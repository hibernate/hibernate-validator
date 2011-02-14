/*
* JBoss, Home of Professional Open Source
* Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual contributors
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.hibernate.validator.engine;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.ConstraintViolation;
import javax.validation.MessageInterpolator;
import javax.validation.Path;
import javax.validation.TraversableResolver;
import javax.validation.ValidationException;
import javax.validation.Validator;
import javax.validation.groups.Default;
import javax.validation.metadata.BeanDescriptor;

import com.googlecode.jtype.TypeUtils;

import org.hibernate.validator.engine.groups.Group;
import org.hibernate.validator.engine.groups.GroupChain;
import org.hibernate.validator.engine.groups.GroupChainGenerator;
import org.hibernate.validator.engine.resolver.SingleThreadCachedTraversableResolver;
import org.hibernate.validator.metadata.BeanMetaConstraint;
import org.hibernate.validator.metadata.BeanMetaData;
import org.hibernate.validator.metadata.BeanMetaDataCache;
import org.hibernate.validator.metadata.BeanMetaDataImpl;
import org.hibernate.validator.metadata.ConstraintHelper;
import org.hibernate.validator.metadata.MetaConstraint;
import org.hibernate.validator.metadata.MethodMetaData;
import org.hibernate.validator.metadata.ParameterMetaData;
import org.hibernate.validator.method.MethodConstraintViolation;
import org.hibernate.validator.method.MethodValidator;
import org.hibernate.validator.util.Contracts;
import org.hibernate.validator.util.ReflectionHelper;

/**
 * The main Bean Validation class. This is the core processing class of Hibernate Validator.
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Kevin Pollet - SERLI - (kevin.pollet@serli.com)
 */
public class ValidatorImpl implements Validator, MethodValidator {

	/**
	 * The default group array used in case any of the validate methods is called without a group.
	 */
	private static final Class<?>[] DEFAULT_GROUP_ARRAY = new Class<?>[] { Default.class };

	/**
	 * Used to resolve the group execution order for a validate call.
	 */
	private final transient GroupChainGenerator groupChainGenerator;

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
	 * Passed at creation time of this validator instance.
	 */
	private final ConstraintHelper constraintHelper;

	/**
	 * Used to get access to the bean meta data. Used to avoid to parsing the constraint configuration for each call
	 * of a given entity.
	 */
	private final BeanMetaDataCache beanMetaDataCache;

	/**
	 * Indicates if validation has to be stopped on first constraint violation.
	 */
	private final boolean failFast;

	public ValidatorImpl(ConstraintValidatorFactory constraintValidatorFactory, MessageInterpolator messageInterpolator, TraversableResolver traversableResolver, ConstraintHelper constraintHelper, BeanMetaDataCache beanMetaDataCache, boolean failFast) {
		this.constraintValidatorFactory = constraintValidatorFactory;
		this.messageInterpolator = messageInterpolator;
		this.traversableResolver = traversableResolver;
		this.constraintHelper = constraintHelper;
		this.beanMetaDataCache = beanMetaDataCache;
		this.failFast = failFast;

		groupChainGenerator = new GroupChainGenerator();
	}

	public final <T> Set<ConstraintViolation<T>> validate(T object, Class<?>... groups) {
		if ( object == null ) {
			throw new IllegalArgumentException( "Validation of a null object" );
		}

		GroupChain groupChain = determineGroupExecutionOrder( groups );

		ValidationContext<T, ConstraintViolation<T>> validationContext = ValidationContext.getContextForValidate(
				object, messageInterpolator, constraintValidatorFactory, getCachingTraversableResolver(), failFast
		);

		ValueContext<?, T> valueContext = ValueContext.getLocalExecutionContext( object, PathImpl.createRootPath() );

		return validateInContext( valueContext, validationContext, groupChain );
	}

	public final <T> Set<ConstraintViolation<T>> validateProperty(T object, String propertyName, Class<?>... groups) {
		if ( object == null ) {
			throw new IllegalArgumentException( "Validated object cannot be null." );
		}
		sanityCheckPropertyPath( propertyName );
		GroupChain groupChain = determineGroupExecutionOrder( groups );

		return validateProperty( object, PathImpl.createPathFromString( propertyName ), groupChain );
	}

	public final <T> Set<ConstraintViolation<T>> validateValue(Class<T> beanType, String propertyName, Object value, Class<?>... groups) {
		if ( beanType == null ) {
			throw new IllegalArgumentException( "The bean type cannot be null." );
		}

		sanityCheckPropertyPath( propertyName );
		GroupChain groupChain = determineGroupExecutionOrder( groups );

		Set<ConstraintViolation<T>> failingConstraintViolations = new HashSet<ConstraintViolation<T>>();
		validateValue(
				beanType, value, PathImpl.createPathFromString( propertyName ), failingConstraintViolations, groupChain
		);
		return failingConstraintViolations;
	}

	public final <T> Set<MethodConstraintViolation<T>> validateParameter(T object, Method method, Object parameterValue, int parameterIndex, Class<?>... groups) {

		Contracts.assertNotNull( object, "The object to be validated must not be null" );
		Contracts.assertNotNull( method, "The method to be validated must not be null" );

		GroupChain groupChain = determineGroupExecutionOrder( groups );

		MethodValidationContext<T> context = ValidationContext.getContextForValidateParameter(
				method,
				parameterIndex,
				object,
				messageInterpolator,
				constraintValidatorFactory,
				getCachingTraversableResolver(),
				failFast
		);

		Object[] parameterValues = new Object[method.getParameterTypes().length];
		parameterValues[parameterIndex] = parameterValue;

		validateParametersInContext( context, object, parameterValues, groupChain );

		return context.getFailingConstraints();
	}

	public final <T> Set<MethodConstraintViolation<T>> validateAllParameters(T object, Method method, Object[] parameterValues, Class<?>... groups) {

		Contracts.assertNotNull( object, "The object to be validated must not be null" );
		Contracts.assertNotNull( method, "The method to be validated must not be null" );

		//this might be the case for parameterless methods
		if ( parameterValues == null ) {
			return Collections.emptySet();
		}

		GroupChain groupChain = determineGroupExecutionOrder( groups );

		MethodValidationContext<T> context = ValidationContext.getContextForValidateParameters(
				method,
				object,
				messageInterpolator,
				constraintValidatorFactory,
				getCachingTraversableResolver(),
				failFast
		);

		validateParametersInContext( context, object, parameterValues, groupChain );

		return context.getFailingConstraints();
	}

	public <T> Set<MethodConstraintViolation<T>> validateReturnValue(T object, Method method, Object returnValue, Class<?>... groups) {

		Contracts.assertNotNull( method, "The method to be validated must not be null" );

		GroupChain groupChain = determineGroupExecutionOrder( groups );

		MethodValidationContext<T> context = ValidationContext.getContextForValidateParameters(
				method,
				object,
				messageInterpolator,
				constraintValidatorFactory,
				getCachingTraversableResolver(),
				failFast
		);

		validateReturnValueInContext( context, object, method, returnValue, groupChain );

		return context.getFailingConstraints();
	}

	public final BeanDescriptor getConstraintsForClass(Class<?> clazz) {
		return getBeanMetaData( clazz ).getBeanDescriptor();
	}

	public final <T> T unwrap(Class<T> type) {
		if ( type.isAssignableFrom( getClass() ) ) {
			return type.cast( this );
		}

		throw new ValidationException( "Type " + type + " not supported" );
	}

	private void sanityCheckPropertyPath(String propertyName) {
		if ( propertyName == null || propertyName.length() == 0 ) {
			throw new IllegalArgumentException( "Invalid property path." );
		}
	}

	private GroupChain determineGroupExecutionOrder(Class<?>[] groups) {
		if ( groups == null ) {
			throw new IllegalArgumentException( "null passed as group name" );
		}

		Class<?>[] tmpGroups = groups;
		// if no groups is specified use the default
		if ( tmpGroups.length == 0 ) {
			tmpGroups = DEFAULT_GROUP_ARRAY;
		}

		return groupChainGenerator.getGroupChainFor( Arrays.asList( tmpGroups ) );
	}

	/**
	 * Validates the given object using the available context information.
	 *
	 * @param valueContext the current validation context
	 * @param context the global validation context
	 * @param groupChain Contains the information which and in which order groups have to be executed
	 * @param <T> The root bean type
	 * @param <V> The type of the current object on the validation stack
	 *
	 * @return Set of constraint violations or the empty set if there were no violations.
	 */
	private <T, U, V, E extends ConstraintViolation<T>> Set<E> validateInContext(ValueContext<U, V> valueContext, ValidationContext<T, E> context, GroupChain groupChain) {
		if ( valueContext.getCurrentBean() == null ) {
			return Collections.emptySet();
		}

		BeanMetaData<U> beanMetaData = getBeanMetaData( valueContext.getCurrentBeanType() );
		if ( beanMetaData.defaultGroupSequenceIsRedefined() ) {
			groupChain.assertDefaultGroupSequenceIsExpandable( beanMetaData.getDefaultGroupSequence( valueContext.getCurrentBean() ) );
		}

		// process first single groups. For these we can optimise object traversal by first running all validations on the current bean
		// before traversing the object.
		Iterator<Group> groupIterator = groupChain.getGroupIterator();
		while ( groupIterator.hasNext() ) {
			Group group = groupIterator.next();
			valueContext.setCurrentGroup( group.getGroup() );
			validateConstraintsForCurrentGroup( context, valueContext );
			if ( context.shouldFailFast() ) {
				return context.getFailingConstraints();
			}
		}
		groupIterator = groupChain.getGroupIterator();
		while ( groupIterator.hasNext() ) {
			Group group = groupIterator.next();
			valueContext.setCurrentGroup( group.getGroup() );
			validateCascadedConstraints( context, valueContext );
			if ( context.shouldFailFast() ) {
				return context.getFailingConstraints();
			}
		}

		// now we process sequences. For sequences I have to traverse the object graph since I have to stop processing when an error occurs.
		Iterator<List<Group>> sequenceIterator = groupChain.getSequenceIterator();
		while ( sequenceIterator.hasNext() ) {
			List<Group> sequence = sequenceIterator.next();
			for ( Group group : sequence ) {
				int numberOfViolations = context.getFailingConstraints().size();
				valueContext.setCurrentGroup( group.getGroup() );

				validateConstraintsForCurrentGroup( context, valueContext );
				if ( context.shouldFailFast() ) {
					return context.getFailingConstraints();
				}

				validateCascadedConstraints( context, valueContext );
				if ( context.shouldFailFast() ) {
					return context.getFailingConstraints();
				}

				if ( context.getFailingConstraints().size() > numberOfViolations ) {
					break;
				}
			}
		}
		return context.getFailingConstraints();
	}

	private <T, U, V, E extends ConstraintViolation<T>> void validateConstraintsForCurrentGroup(ValidationContext<T, E> validationContext, ValueContext<U, V> valueContext) {
		BeanMetaData<U> beanMetaData = getBeanMetaData( valueContext.getCurrentBeanType() );
		boolean validatingDefault = valueContext.validatingDefault();
		boolean validatedBeanRedefinesDefault = beanMetaData.defaultGroupSequenceIsRedefined();

		// if we are not validating the default group there is nothing special to consider
		if ( !validatingDefault ) {
			validateConstraintsForNonDefaultGroup( validationContext, valueContext );
			return;
		}

		// if we are validating the default group we have to distinguish between the case where the main entity type
		// redefines the default group and where not.
		// When the default group is validated, but the current bean does not redefine the default group sequence
		// in this case we have to check whether any of the super-types (and the constraints hosted on it) re-defines
		// the default group sequence. In this case this sequence must be applied
		if ( validatedBeanRedefinesDefault ) {
			validateConstraintsForRedefinedDefaultGroup( validationContext, valueContext, beanMetaData );
		}
		else {
			validateConstraintsForDefaultGroup( validationContext, valueContext, beanMetaData );
		}
	}

	private <T, U, V, E extends ConstraintViolation<T>> void validateConstraintsForDefaultGroup(ValidationContext<T, E> validationContext, ValueContext<U, V> valueContext, BeanMetaData<U> beanMetaData) {
		for ( Map.Entry<Class<?>, List<BeanMetaConstraint<U, ? extends Annotation>>> entry : beanMetaData.getMetaConstraintsAsMap()
				.entrySet() ) {
			@SuppressWarnings("unchecked")
			Class<U> hostingBeanClass = (Class<U>) entry.getKey();
			List<BeanMetaConstraint<U, ? extends Annotation>> constraints = entry.getValue();

			BeanMetaData<U> hostingBeanMetaData = getBeanMetaData( hostingBeanClass );
			List<Class<?>> defaultGroupSequence = hostingBeanMetaData.getDefaultGroupSequence( valueContext.getCurrentBean() );

			PathImpl currentPath = valueContext.getPropertyPath();
			for ( Class<?> defaultSequenceMember : defaultGroupSequence ) {
				valueContext.setCurrentGroup( defaultSequenceMember );
				boolean validationSuccessful = true;
				for ( BeanMetaConstraint<U, ? extends Annotation> metaConstraint : constraints ) {
					boolean tmp = validateConstraint(
							validationContext, valueContext, metaConstraint
					);
					if ( validationContext.shouldFailFast() ) {
						return;
					}
					validationSuccessful = validationSuccessful && tmp;
					// reset the path
					valueContext.setPropertyPath( currentPath );
				}
				if ( !validationSuccessful ) {
					break;
				}
			}
			validationContext.markProcessed(
					valueContext.getCurrentBean(),
					valueContext.getCurrentGroup(),
					valueContext.getPropertyPath()
			);
		}
	}

	private <T, U, V, E extends ConstraintViolation<T>> void validateConstraintsForRedefinedDefaultGroup(ValidationContext<T, E> validationContext, ValueContext<U, V> valueContext, BeanMetaData<U> beanMetaData) {
		List<Class<?>> defaultGroupSequence = beanMetaData.getDefaultGroupSequence( valueContext.getCurrentBean() );

		PathImpl currentPath = valueContext.getPropertyPath();
		for ( Class<?> defaultSequenceMember : defaultGroupSequence ) {
			valueContext.setCurrentGroup( defaultSequenceMember );
			boolean validationSuccessful = true;
			for ( BeanMetaConstraint<U, ? extends Annotation> metaConstraint : beanMetaData.getMetaConstraintsAsList() ) {
				boolean tmp = validateConstraint( validationContext, valueContext, metaConstraint );
				if ( validationContext.shouldFailFast() ) {
					return;
				}
				validationSuccessful = validationSuccessful && tmp;
				// reset the path
				valueContext.setPropertyPath( currentPath );
			}
			if ( !validationSuccessful ) {
				break;
			}
		}
		validationContext.markProcessed(
				valueContext.getCurrentBean(),
				valueContext.getCurrentGroup(),
				valueContext.getPropertyPath()
		);
	}

	private <T, U, V> void validateConstraintsForNonDefaultGroup(ValidationContext<T, ?> validationContext, ValueContext<U, V> valueContext) {
		BeanMetaData<U> beanMetaData = getBeanMetaData( valueContext.getCurrentBeanType() );
		PathImpl currentPath = valueContext.getPropertyPath();
		for ( BeanMetaConstraint<U, ? extends Annotation> metaConstraint : beanMetaData.getMetaConstraintsAsList() ) {
			validateConstraint( validationContext, valueContext, metaConstraint );
			if ( validationContext.shouldFailFast() ) {
				return;
			}
			// reset the path to the state before this call
			valueContext.setPropertyPath( currentPath );
		}
		validationContext.markProcessed(
				valueContext.getCurrentBean(),
				valueContext.getCurrentGroup(),
				valueContext.getPropertyPath()
		);
	}

	private <T, U, V> boolean validateConstraint(ValidationContext<T, ?> validationContext, ValueContext<U, V> valueContext, BeanMetaConstraint<U, ?> metaConstraint) {
		boolean validationSuccessful = true;

		if ( metaConstraint.getElementType() != ElementType.TYPE ) {
			valueContext.appendNode( metaConstraint.getLocation().getPropertyName() );
		}

		if ( isValidationRequired( validationContext, valueContext, metaConstraint ) ) {
			@SuppressWarnings("unchecked")
			V valueToValidate = (V) metaConstraint.getValue( valueContext.getCurrentBean() );
			valueContext.setCurrentValidatedValue( valueToValidate );
			validationSuccessful = metaConstraint.validateConstraint( validationContext, valueContext );
		}

		return validationSuccessful;
	}

	/**
	 * Validates all cascaded constraints for the given bean using the current group set in the execution context.
	 * This method must always be called after validateConstraints for the same context.
	 *
	 * @param validationContext The execution context
	 * @param valueContext Collected information for single validation
	 */
	private <T, U, V> void validateCascadedConstraints(ValidationContext<T, ?> validationContext, ValueContext<U, V> valueContext) {
		List<Member> cascadedMembers = getBeanMetaData( valueContext.getCurrentBeanType() ).getCascadedMembers();
		PathImpl currentPath = valueContext.getPropertyPath();
		for ( Member member : cascadedMembers ) {
			Type type = ReflectionHelper.typeOf( member );
			String newNode = ReflectionHelper.getPropertyName( member );
			valueContext.appendNode( newNode );

			if ( isCascadeRequired( validationContext, valueContext, member ) ) {
				Object value = ReflectionHelper.getValue( member, valueContext.getCurrentBean() );
				if ( value != null ) {
					Iterator<?> iter = createIteratorForCascadedValue( type, value, valueContext );
					boolean isIndexable = isIndexable( type );
					validateCascadedConstraint(
							validationContext,
							iter,
							isIndexable,
							valueContext
					);
					if ( validationContext.shouldFailFast() ) {
						return;
					}
				}
			}
			// reset the path
			valueContext.setPropertyPath( currentPath );
		}
	}

	/**
	 * Validates the cascading parameter or return value specified with the
	 * given value context. Any further cascading references are followed if
	 * applicable.
	 *
	 * @param validationContext The global context for the current validateParameter(s) or
	 * validateReturnValue() call.
	 * @param valueContext The local context for validating the given parameter/return
	 * value.
	 */
	private <T, U, V> void validateCascadedMethodConstraints(MethodValidationContext<T> validationContext, ValueContext<U, V> valueContext) {

		Object value = valueContext.getCurrentBean();
		Type type = valueContext.getCurrentBeanType();
		Iterator<?> iter = createIteratorForCascadedValue( type, value, valueContext );
		boolean isIndexable = isIndexable( type );

		validateCascadedConstraint(
				validationContext,
				iter,
				isIndexable,
				valueContext
		);
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
		else if ( TypeUtils.isArray( type ) ) {
			List<?> arrayList = Arrays.asList( (Object[]) value );
			iter = arrayList.iterator();
			valueContext.markCurrentPropertyAsIterable();
		}
		else {
			List<Object> list = new ArrayList<Object>();
			list.add( value );
			iter = list.iterator();
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
		else if ( TypeUtils.isArray( type ) ) {
			isIndexable = true;
		}
		return isIndexable;
	}

	private <T> void validateCascadedConstraint(ValidationContext<T, ?> context, Iterator<?> iter, boolean isIndexable, ValueContext<?, ?> valueContext) {
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

			if ( !context.isAlreadyValidated(
					value, valueContext.getCurrentGroup(), valueContext.getPropertyPath()
			) ) {
				GroupChain groupChain = groupChainGenerator.getGroupChainFor(
						Arrays.<Class<?>>asList( valueContext.getCurrentGroup() )
				);

				ValueContext<?, T> newValueContext;
				if ( value != null ) {
					newValueContext = ValueContext.getLocalExecutionContext( value, valueContext.getPropertyPath() );
				}
				else {
					newValueContext = ValueContext.getLocalExecutionContext(
							valueContext.getCurrentBeanType(), valueContext.getPropertyPath()
					);
				}

				//propagate parameter index/name if required
				if ( valueContext.getParameterIndex() != null ) {
					newValueContext.setParameterIndex( valueContext.getParameterIndex() );
					newValueContext.setParameterName( valueContext.getParameterName() );
				}

				validateInContext( newValueContext, context, groupChain );
				if ( context.shouldFailFast() ) {
					return;
				}
			}
			i++;
		}
	}

	private <T, U, V> Set<ConstraintViolation<T>> validateProperty(T object, PathImpl propertyPath, GroupChain groupChain) {

		@SuppressWarnings("unchecked")
		final Class<T> beanType = (Class<T>) object.getClass();

		Set<BeanMetaConstraint<T, ?>> metaConstraints = new HashSet<BeanMetaConstraint<T, ?>>();
		Iterator<Path.Node> propertyIter = propertyPath.iterator();
		ValueContext<U, V> valueContext = collectMetaConstraintsForPath(
				beanType, object, propertyIter, propertyPath, metaConstraints
		);
		ValidationContext<T, ConstraintViolation<T>> validationContext = ValidationContext.getContextForValidateProperty(
				object,
				messageInterpolator,
				constraintValidatorFactory,
				getCachingTraversableResolver(),
				failFast
		);

		if ( valueContext.getCurrentBean() == null ) {
			throw new IllegalArgumentException( "Invalid property path." );
		}

		if ( metaConstraints.size() == 0 ) {
			return validationContext.getFailingConstraints();
		}

		Iterator<Group> groupIterator = groupChain.getGroupIterator();
		while ( groupIterator.hasNext() ) {
			Group group = groupIterator.next();
			validatePropertyForGroup( valueContext, validationContext, metaConstraints, group );
			if ( validationContext.shouldFailFast() ) {
				return validationContext.getFailingConstraints();
			}
		}

		Iterator<List<Group>> sequenceIterator = groupChain.getSequenceIterator();
		while ( sequenceIterator.hasNext() ) {
			List<Group> sequence = sequenceIterator.next();
			int numberOfConstraintViolationsBefore = validationContext.getFailingConstraints().size();
			for ( Group group : sequence ) {
				validatePropertyForGroup( valueContext, validationContext, metaConstraints, group );
				if ( validationContext.shouldFailFast() ) {
					return validationContext.getFailingConstraints();
				}
				if ( validationContext.getFailingConstraints().size() > numberOfConstraintViolationsBefore ) {
					break;
				}
			}
		}

		return validationContext.getFailingConstraints();
	}

	private <T, U, V> void validatePropertyForGroup(ValueContext<U, V> valueContext, ValidationContext<T, ConstraintViolation<T>> validationContext, Set<BeanMetaConstraint<T, ?>> metaConstraints, Group group) {

		int numberOfConstraintViolationsBefore = validationContext.getFailingConstraints().size();

		BeanMetaData<U> beanMetaData = getBeanMetaData( valueContext.getCurrentBeanType() );

		List<Class<?>> groupList;
		if ( group.isDefaultGroup() ) {
			groupList = beanMetaData.getDefaultGroupSequence( valueContext.getCurrentBean() );
		}
		else {
			groupList = new ArrayList<Class<?>>();
			groupList.add( group.getGroup() );
		}

		for ( Class<?> groupClass : groupList ) {
			for ( BeanMetaConstraint<T, ?> metaConstraint : metaConstraints ) {
				valueContext.setCurrentGroup( groupClass );
				if ( isValidationRequired( validationContext, valueContext, metaConstraint ) ) {
					@SuppressWarnings("unchecked")
					V valueToValidate = (V) metaConstraint.getValue( valueContext.getCurrentBean() );
					valueContext.setCurrentValidatedValue( valueToValidate );
					metaConstraint.validateConstraint( validationContext, valueContext );
					if ( validationContext.shouldFailFast() ) {
						return;
					}
				}
			}
			if ( validationContext.getFailingConstraints().size() > numberOfConstraintViolationsBefore ) {
				break;
			}
		}
	}

	private <T, U, V> void validateValue(Class<T> beanType, V value, PathImpl propertyPath, Set<ConstraintViolation<T>> failingConstraintViolations, GroupChain groupChain) {
		Set<BeanMetaConstraint<T, ?>> metaConstraints = new HashSet<BeanMetaConstraint<T, ?>>();
		ValueContext<U, V> valueContext = collectMetaConstraintsForPath(
				beanType, null, propertyPath.iterator(), propertyPath, metaConstraints
		);
		valueContext.setCurrentValidatedValue( value );

		if ( metaConstraints.size() == 0 ) {
			return;
		}

		//root of validateValue calls, share the same cached TraversableResolver
		TraversableResolver cachedTraversableResolver = getCachingTraversableResolver();

		// process groups
		Iterator<Group> groupIterator = groupChain.getGroupIterator();
		while ( groupIterator.hasNext() ) {
			Group group = groupIterator.next();
			validateValueForGroup(
					beanType,
					valueContext,
					failingConstraintViolations,
					metaConstraints,
					group,
					cachedTraversableResolver
			);
			if ( shouldFailFast( failingConstraintViolations ) ) {
				return;
			}
		}

		// process sequences
		Iterator<List<Group>> sequenceIterator = groupChain.getSequenceIterator();
		while ( sequenceIterator.hasNext() ) {
			List<Group> sequence = sequenceIterator.next();
			int numberOfConstraintViolations = failingConstraintViolations.size();
			for ( Group group : sequence ) {
				validateValueForGroup(
						beanType,
						valueContext,
						failingConstraintViolations,
						metaConstraints,
						group,
						cachedTraversableResolver
				);
				if ( shouldFailFast( failingConstraintViolations ) ) {
					return;
				}
				if ( failingConstraintViolations.size() > numberOfConstraintViolations ) {
					break;
				}
			}
		}
	}

	private <T, U, V> void validateValueForGroup(
			Class<T> beanType,
			ValueContext<U, V> valueContext,
			Set<ConstraintViolation<T>> failingConstraintViolations,
			Set<BeanMetaConstraint<T, ?>> metaConstraints,
			Group group,
			TraversableResolver cachedTraversableResolver) {
		int numberOfConstraintViolations = failingConstraintViolations.size();

		BeanMetaData<U> beanMetaData = getBeanMetaData( valueContext.getCurrentBeanType() );

		List<Class<?>> groupList;
		if ( group.isDefaultGroup() ) {
			groupList = beanMetaData.getDefaultGroupSequence( null );
		}
		else {
			groupList = new ArrayList<Class<?>>();
			groupList.add( group.getGroup() );
		}

		for ( Class<?> groupClass : groupList ) {
			for ( MetaConstraint<T, ?> metaConstraint : metaConstraints ) {
				ValidationContext<T, ConstraintViolation<T>> context = ValidationContext.getContextForValidateValue(
						beanType, messageInterpolator, constraintValidatorFactory, cachedTraversableResolver, failFast
				);
				valueContext.setCurrentGroup( groupClass );
				if ( isValidationRequired( context, valueContext, metaConstraint ) ) {
					metaConstraint.validateConstraint( context, valueContext );
					failingConstraintViolations.addAll( context.getFailingConstraints() );
					if ( shouldFailFast( failingConstraintViolations ) ) {
						return;
					}
				}
			}
			if ( failingConstraintViolations.size() > numberOfConstraintViolations ) {
				break;
			}
		}
	}

	private <T> void validateParametersInContext(MethodValidationContext<T> validationContext, T object, Object[] parameterValues, GroupChain groupChain) {

		BeanMetaData<T> beanMetaData = getBeanMetaData( validationContext.getRootBeanClass() );

		//assert that there are no illegal method parameter constraints
		beanMetaData.assertMethodParameterConstraintsCorrectness();

		if ( beanMetaData.defaultGroupSequenceIsRedefined() ) {
			groupChain.assertDefaultGroupSequenceIsExpandable( beanMetaData.getDefaultGroupSequence( object ) );
		}

		// process first single groups
		Iterator<Group> groupIterator = groupChain.getGroupIterator();
		while ( groupIterator.hasNext() ) {
			validateParametersForGroup( validationContext, object, parameterValues, groupIterator.next() );
			if ( validationContext.shouldFailFast() ) {
				return;
			}
		}

		// now process sequences, stop after the first erroneous group
		Iterator<List<Group>> sequenceIterator = groupChain.getSequenceIterator();
		while ( sequenceIterator.hasNext() ) {
			List<Group> sequence = sequenceIterator.next();
			for ( Group group : sequence ) {
				int numberOfFailingConstraint = validateParametersForGroup(
						validationContext, object, parameterValues, group
				);
				if ( validationContext.shouldFailFast() ) {
					return;
				}
				if ( numberOfFailingConstraint > 0 ) {
					break;
				}
			}
		}
	}

	private <T> int validateParametersForGroup(MethodValidationContext<T> validationContext, T object, Object[] parameterValues, Group group) {

		int numberOfViolationsBefore = validationContext.getFailingConstraints().size();

		Method method = validationContext.getMethod();

		BeanMetaData<T> beanMetaData = getBeanMetaData( validationContext.getRootBeanClass() );
		Map<Class<?>, MethodMetaData> methodMetaDataByType = beanMetaData.getMetaDataForMethod( method );

		//used for retrieval of parameter names; we'll take the names from the lowest method in the hierarchy
		MethodMetaData methodMetaDataOfDeclaringType = methodMetaDataByType.get( method.getDeclaringClass() );

		// TODO GM: define behavior with respect to redefined default sequences. Should only the
		// sequence from the validated bean be honored or also default sequence definitions up in
		// the inheritance tree?
		// For now a redefined default sequence will only be considered if specified at the bean
		// hosting the validated itself, but no other default sequence from parent types

		List<Class<?>> groupList;
		if ( group.isDefaultGroup() ) {
			groupList = beanMetaData.getDefaultGroupSequence( object );
		}
		else {
			groupList = Arrays.<Class<?>>asList( group.getGroup() );
		}

		//the only case where we can have multiple groups here is a redefined default group sequence
		for ( Class<?> oneGroup : groupList ) {

			int numberOfViolationsOfCurrentGroup = 0;

			for ( Entry<Class<?>, MethodMetaData> constraintsOfOneClass : methodMetaDataByType.entrySet() ) {

				for ( int i = 0; i < parameterValues.length; i++ ) {

					//ignore this parameter if this validation is for a single parameter and this is not the right one
					if ( validationContext.getParameterIndex() != null && !validationContext.getParameterIndex()
							.equals( i ) ) {
						continue;
					}

					Object value = parameterValues[i];
					String parameterName = methodMetaDataOfDeclaringType.getParameterMetaData( i ).getParameterName();

					// validate constraints at parameter itself
					ValueContext<T, Object> valueContext = ValueContext.getLocalExecutionContext(
							object, PathImpl.createPathForMethodParameter( method, parameterName ), i, parameterName
					);
					valueContext.setCurrentValidatedValue( value );
					valueContext.setCurrentGroup( oneGroup );

					ParameterMetaData parameterMetaData = constraintsOfOneClass.getValue()
							.getParameterMetaData( valueContext.getParameterIndex() );

					numberOfViolationsOfCurrentGroup += validateParameterForGroup(
							validationContext, valueContext, parameterMetaData
					);
					if ( validationContext.shouldFailFast() ) {
						return validationContext.getFailingConstraints().size() - numberOfViolationsBefore;
					}
				}
			}

			//stop processing after first group with errors occurred
			if ( numberOfViolationsOfCurrentGroup > 0 ) {
				break;
			}
		}

		// validate parameter beans annotated with @Valid if required
		for ( int i = 0; i < parameterValues.length; i++ ) {

			//ignore this parameter if this validation is for a single parameter and this is not the right one
			if ( validationContext.getParameterIndex() != null && !validationContext.getParameterIndex().equals( i ) ) {
				continue;
			}

			Object value = parameterValues[i];
			String parameterName = methodMetaDataOfDeclaringType.getParameterMetaData( i ).getParameterName();

			if ( isCascadeRequired( method, i ) && value != null ) {

				ValueContext<Object, ?> cascadingvalueContext = ValueContext.getLocalExecutionContext(
						value, PathImpl.createPathForMethodParameter( method, parameterName ), i, parameterName
				);
				cascadingvalueContext.setCurrentGroup( group.getGroup() );

				//TODO GM: consider violations from cascaded validation
				validateCascadedMethodConstraints( validationContext, cascadingvalueContext );
				if ( validationContext.shouldFailFast() ) {
					break;
				}
			}
		}

		return validationContext.getFailingConstraints().size() - numberOfViolationsBefore;
	}

	/**
	 * Validates the constraints at the specified parameter which are part of
	 * the given value context's current group. Any occurred constraint
	 * violations are stored in the given validation context.
	 *
	 * @param validationContext The validation context for storing constraint violations.
	 * @param valueContext The value context specifying the group and value to validate.
	 * @param parameterMetaData Meta data on the constraints to evaluate.
	 *
	 * @return The number of constraint violations occurred during validation of
	 *         the specified constraints.
	 */
	private <T, U, V> int validateParameterForGroup(MethodValidationContext<T> validationContext, ValueContext<U, V> valueContext, ParameterMetaData parameterMetaData) {

		int numberOfViolationsBefore = validationContext.getFailingConstraints().size();

		for ( MetaConstraint<?, ? extends Annotation> metaConstraint : parameterMetaData ) {

			//ignore constraints not part of the evaluated group
			if ( !metaConstraint.getGroupList().contains( valueContext.getCurrentGroup() ) ) {
				continue;
			}

			metaConstraint.validateConstraint( validationContext, valueContext );
			if ( validationContext.shouldFailFast() ) {
				break;
			}
		}

		return validationContext.getFailingConstraints().size() - numberOfViolationsBefore;
	}

	private <V, T> void validateReturnValueInContext(MethodValidationContext<T> context, T bean, Method method, V value, GroupChain groupChain) {

		BeanMetaData<T> beanMetaData = getBeanMetaData( context.getRootBeanClass() );

		if ( beanMetaData.defaultGroupSequenceIsRedefined() ) {
			groupChain.assertDefaultGroupSequenceIsExpandable( beanMetaData.getDefaultGroupSequence( bean ) );
		}

		Iterator<Group> groupIterator = groupChain.getGroupIterator();

		// process first single groups
		while ( groupIterator.hasNext() ) {
			validateReturnValueForGroup( context, bean, value, groupIterator.next() );
			if ( context.shouldFailFast() ) {
				return;
			}
		}

		// now process sequences, stop after the first erroneous group
		Iterator<List<Group>> sequenceIterator = groupChain.getSequenceIterator();
		while ( sequenceIterator.hasNext() ) {
			List<Group> sequence = sequenceIterator.next();
			for ( Group group : sequence ) {
				int numberOfFailingConstraint = validateReturnValueForGroup(
						context, bean, value, group
				);
				if ( context.shouldFailFast() ) {
					return;
				}
				if ( numberOfFailingConstraint > 0 ) {
					break;
				}
			}
		}
	}

	//TODO GM: if possible integrate with validateParameterForGroup()
	private <T, V> int validateReturnValueForGroup(MethodValidationContext<T> validationContext, T bean, V value, Group group) {

		int numberOfViolationsBefore = validationContext.getFailingConstraints().size();

		Method method = validationContext.getMethod();

		BeanMetaData<T> beanMetaData = getBeanMetaData( validationContext.getRootBeanClass() );
		Map<Class<?>, MethodMetaData> methodMetaDataByType = beanMetaData.getMetaDataForMethod( method );

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
			groupList = Arrays.<Class<?>>asList( group.getGroup() );
		}

		//the only case where we can have multiple groups here is a redefined default group sequence
		for ( Class<?> oneGroup : groupList ) {

			int numberOfViolationsOfCurrentGroup = 0;

			// validate constraints at return value itself
			ValueContext<T, V> valueContext = ValueContext.getLocalExecutionContext(
					bean, PathImpl.createPathForMethodReturnValue( method )
			);
			valueContext.setCurrentValidatedValue( value );
			valueContext.setCurrentGroup( oneGroup );

			for ( Entry<Class<?>, MethodMetaData> constraintsOfOneClass : methodMetaDataByType.entrySet() ) {

				numberOfViolationsOfCurrentGroup +=
						validateReturnValueForGroup(
								validationContext, valueContext, constraintsOfOneClass.getValue()
						);
				if ( validationContext.shouldFailFast() ) {
					return validationContext.getFailingConstraints().size() - numberOfViolationsBefore;
				}
			}

			//stop processing after first group with errors occurred
			if ( numberOfViolationsOfCurrentGroup > 0 ) {
				break;
			}
		}

		// cascaded validation if required
		if ( isCascadeRequired( method ) && value != null ) {

			ValueContext<V, Object> cascadingvalueContext = ValueContext.getLocalExecutionContext(
					value, PathImpl.createPathForMethodReturnValue( method )
			);
			cascadingvalueContext.setCurrentGroup( group.getGroup() );

			validateCascadedMethodConstraints( validationContext, cascadingvalueContext );
		}

		return validationContext.getFailingConstraints().size() - numberOfViolationsBefore;
	}

	private <T, V> int validateReturnValueForGroup(MethodValidationContext<T> validationContext,
												   ValueContext<T, V> valueContext, MethodMetaData constraintsOfOneClass) {

		int numberOfViolationsBefore = validationContext.getFailingConstraints().size();

		for ( MetaConstraint<?, ? extends Annotation> metaConstraint : constraintsOfOneClass ) {

			if ( !metaConstraint.getGroupList().contains( valueContext.getCurrentGroup() ) ) {
				continue;
			}
			metaConstraint.validateConstraint( validationContext, valueContext );
			if ( validationContext.shouldFailFast() ) {
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
	 * @param clazz The class type to check for constraints.
	 * @param value While resolving the property path this instance points to the current object. Might be {@code null}.
	 * @param propertyIter An instance of {@code PropertyIterator} in order to iterate the items of the original property path.
	 * @param propertyPath The property path for which constraints have to be collected.
	 * @param metaConstraints Set of {@code MetaConstraint}s to collect all matching constraints.
	 *
	 * @return Returns an instance of {@code ValueContext} which describes the context associated to the given property path.
	 */
	private <T, U, V> ValueContext<U, V> collectMetaConstraintsForPath(Class<T> clazz, T value, Iterator<Path.Node> propertyIter, PathImpl propertyPath, Set<BeanMetaConstraint<T, ?>> metaConstraints) {
		Path.Node elem = propertyIter.next();
		T newValue = value;

		final BeanMetaData<T> metaData = getBeanMetaData( clazz );
		//use precomputed method list as ReflectionHelper#containsMember is slow
		if ( !metaData.isPropertyPresent( elem.getName() ) ) {
			throw new IllegalArgumentException(
					"Invalid property path. There is no property " + elem.getName() + " in entity " + metaData.getBeanClass()
							.getName()
			);
		}

		if ( !propertyIter.hasNext() ) {
			List<BeanMetaConstraint<T, ? extends Annotation>> metaConstraintList = metaData.getMetaConstraintsAsList();
			for ( BeanMetaConstraint<T, ?> metaConstraint : metaConstraintList ) {
				if ( elem.getName() != null && elem.getName()
						.equals( metaConstraint.getLocation().getPropertyName() ) ) {
					metaConstraints.add( metaConstraint );
				}
			}
		}
		else {
			List<Member> cascadedMembers = metaData.getCascadedMembers();
			for ( Member m : cascadedMembers ) {
				if ( ReflectionHelper.getPropertyName( m ).equals( elem.getName() ) ) {
					Type type = ReflectionHelper.typeOf( m );
					newValue = newValue == null ? null : (T) ReflectionHelper.getValue( m, newValue );
					if ( elem.isInIterable() ) {
						if ( newValue != null && elem.getIndex() != null ) {
							newValue = (T) ReflectionHelper.getIndexedValue( newValue, elem.getIndex() );
						}
						else if ( newValue != null && elem.getKey() != null ) {
							newValue = (T) ReflectionHelper.getMappedValue( newValue, elem.getKey() );
						}
						else if ( newValue != null ) {
							throw new IllegalArgumentException( "Property path must provide index or map key" );
						}
						type = ReflectionHelper.getIndexedType( type );
					}

					@SuppressWarnings("unchecked")
					Class<T> valueClass = (Class<T>) ( newValue == null ? type : newValue.getClass() );

					return collectMetaConstraintsForPath(
							valueClass,
							newValue,
							propertyIter,
							propertyPath,
							metaConstraints
					);
				}
			}
		}

		if ( newValue == null ) {
			return ValueContext.getLocalExecutionContext( (Class<U>) clazz, propertyPath );
		}
		return ValueContext.getLocalExecutionContext( (U) value, propertyPath );
	}

	private <U> BeanMetaData<U> getBeanMetaData(Class<U> beanClass) {
		BeanMetaDataImpl<U> beanMetaData = beanMetaDataCache.getBeanMetaData( beanClass );
		if ( beanMetaData == null ) {
			beanMetaData = new BeanMetaDataImpl<U>(
					beanClass, constraintHelper, beanMetaDataCache
			);
			beanMetaDataCache.addBeanMetaData( beanClass, beanMetaData );
		}
		return beanMetaData;
	}

	/**
	 * Must be called and stored for the duration of the stack call
	 * A new instance is returned each time
	 *
	 * @return The resolver for the duration of a full validation.
	 */
	private TraversableResolver getCachingTraversableResolver() {
		return new SingleThreadCachedTraversableResolver( traversableResolver );
	}

	private boolean isValidationRequired(ValidationContext<?, ?> validationContext, ValueContext<?, ?> valueContext, MetaConstraint<?, ?> metaConstraint) {
		if ( !metaConstraint.getGroupList().contains( valueContext.getCurrentGroup() ) ) {
			return false;
		}

		boolean isReachable;
		PathImpl path = valueContext.getPropertyPath();
		Path pathToObject = path.getPathWithoutLeafNode();

		try {
			isReachable = validationContext.getTraversableResolver().isReachable(
					valueContext.getCurrentBean(),
					path.getLeafNode(),
					validationContext.getRootBeanClass(),
					pathToObject,
					metaConstraint.getElementType()
			);
		}
		catch ( RuntimeException e ) {
			throw new ValidationException( "Call to TraversableResolver.isReachable() threw an exception", e );
		}

		return isReachable;
	}

	private boolean isCascadeRequired(ValidationContext<?, ?> validationContext, ValueContext<?, ?> valueContext, Member member) {
		final ElementType type = member instanceof Field ? ElementType.FIELD : ElementType.METHOD;
		boolean isReachable;
		boolean isCascadable;

		PathImpl path = valueContext.getPropertyPath();
		Path pathToObject = path.getPathWithoutLeafNode();

		try {
			isReachable = validationContext.getTraversableResolver().isReachable(
					valueContext.getCurrentBean(),
					path.getLeafNode(),
					validationContext.getRootBeanClass(),
					pathToObject,
					type
			);
		}
		catch ( RuntimeException e ) {
			throw new ValidationException( "Call to TraversableResolver.isReachable() threw an exception", e );
		}

		try {
			isCascadable = validationContext.getTraversableResolver().isCascadable(
					valueContext.getCurrentBean(),
					path.getLeafNode(),
					validationContext.getRootBeanClass(),
					pathToObject,
					type
			);
		}
		catch ( RuntimeException e ) {
			throw new ValidationException( "Call to TraversableResolver.isCascadable() threw an exception", e );
		}

		return isReachable && isCascadable;
	}

	/**
	 * Checks whether a cascaded validation is required for the given parameter
	 * of the given method or not.
	 *
	 * @param method The method of interest.
	 * @param parameterIndex The parameter of interest's index within the method's
	 * parameter list.
	 *
	 * @return True, if a cascaded validation is required, false otherwise.
	 */
	private boolean isCascadeRequired(Method method, int parameterIndex) {

		BeanMetaData<?> beanMetaData = getBeanMetaData( method.getDeclaringClass() );
		Map<Class<?>, MethodMetaData> methodMetaData = beanMetaData.getMetaDataForMethod( method );

		for ( MethodMetaData oneMethodMetaData : methodMetaData.values() ) {
			if ( oneMethodMetaData.getParameterMetaData( parameterIndex ).isCascading() ) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Checks whether a cascaded validation is required when validating the
	 * return value of of the given method or not.
	 *
	 * @param method The method of interest. parameter list.
	 *
	 * @return True, if a cascaded validation is required, false otherwise.
	 */
	private boolean isCascadeRequired(Method method) {

		BeanMetaData<?> beanMetaData = getBeanMetaData( method.getDeclaringClass() );
		Map<Class<?>, MethodMetaData> methodMetaData = beanMetaData.getMetaDataForMethod( method );

		for ( MethodMetaData oneMethodMetaData : methodMetaData.values() ) {
			if ( oneMethodMetaData.isCascading() ) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Whether or not the validation should fail on the first constraint violation.
	 *
	 * @param failingConstraintViolations the set of failing constraint violations so far
	 * @param <T> the type of validated bean
	 *
	 * @return true if the validation process should fail fast
	 */
	private <T> boolean shouldFailFast(Set<ConstraintViolation<T>> failingConstraintViolations) {
		return failFast && failingConstraintViolations.size() > 0;
	}
}

