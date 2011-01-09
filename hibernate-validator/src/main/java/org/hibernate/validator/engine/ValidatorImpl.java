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
import java.util.Collection;
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

import org.hibernate.validator.MethodConstraintViolation;
import org.hibernate.validator.MethodValidator;
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
import org.hibernate.validator.util.Contracts;
import org.hibernate.validator.util.ReflectionHelper;

import com.googlecode.jtype.TypeUtils;

/**
 * The main Bean Validation class. This is the core processing class of Hibernate Validator.
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 * @author Gunnar Morling
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

	public ValidatorImpl(ConstraintValidatorFactory constraintValidatorFactory, MessageInterpolator messageInterpolator, TraversableResolver traversableResolver, ConstraintHelper constraintHelper, BeanMetaDataCache beanMetaDataCache) {
		this.constraintValidatorFactory = constraintValidatorFactory;
		this.messageInterpolator = messageInterpolator;
		this.traversableResolver = traversableResolver;
		this.constraintHelper = constraintHelper;
		this.beanMetaDataCache = beanMetaDataCache;

		groupChainGenerator = new GroupChainGenerator();
	}

	public final <T> Set<ConstraintViolation<T>> validate(T object, Class<?>... groups) {
		if ( object == null ) {
			throw new IllegalArgumentException( "Validation of a null object" );
		}

		GroupChain groupChain = determineGroupExecutionOrder( groups );

		ValidationContext<T, ConstraintViolation<T>> validationContext = ValidationContext.getContextForValidate(
				object, messageInterpolator, constraintValidatorFactory, getCachingTraversableResolver()
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

		Set<ConstraintViolation<T>> failingConstraintViolations = new HashSet<ConstraintViolation<T>>();
		validateProperty(
				object, PathImpl.createPathFromString( propertyName ), failingConstraintViolations, groupChain
		);
		return failingConstraintViolations;
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
				method, object, messageInterpolator, constraintValidatorFactory, getCachingTraversableResolver()
		);

		validateParametersInContext( context, object, new Object[] { parameterValue }, groupChain );

		return context.getFailingConstraints();
	}

	public final <T> Set<MethodConstraintViolation<T>> validateParameters(T object, Method method, Object[] parameterValues, Class<?>... groups) {

		Contracts.assertNotNull( object, "The object to be validated must not be null" );
		Contracts.assertNotNull( method, "The method to be validated must not be null" );

		//this might be the case for parameterless methods
		if ( parameterValues == null ) {
			return Collections.emptySet();
		}

		GroupChain groupChain = determineGroupExecutionOrder( groups );

		MethodValidationContext<T> context = ValidationContext.getContextForValidateParameter(
				method, object, messageInterpolator, constraintValidatorFactory, getCachingTraversableResolver()
		);

		validateParametersInContext( context, object, parameterValues, groupChain );

		return context.getFailingConstraints();
	}

	public <T> Set<MethodConstraintViolation<T>> validateReturnValue(T object, Method method, Object returnValue, Class<?>... groups) {

		Contracts.assertNotNull( method, "The method to be validated must not be null" );

		GroupChain groupChain = determineGroupExecutionOrder( groups );

		MethodValidationContext<T> context = ValidationContext.getContextForValidateParameter(
				method, object, messageInterpolator, constraintValidatorFactory, getCachingTraversableResolver()
		);

		return validateReturnValueInContext( context, object, method, returnValue, groupChain );
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
			groupChain.assertDefaultGroupSequenceIsExpandable( beanMetaData.getDefaultGroupSequence() );
		}

		// process first single groups. For these we can optimise object traversal by first running all validations on the current bean
		// before traversing the object.
		Iterator<Group> groupIterator = groupChain.getGroupIterator();
		while ( groupIterator.hasNext() ) {
			Group group = groupIterator.next();
			valueContext.setCurrentGroup( group.getGroup() );
			validateConstraintsForCurrentGroup( context, valueContext );
		}
		groupIterator = groupChain.getGroupIterator();
		while ( groupIterator.hasNext() ) {
			Group group = groupIterator.next();
			valueContext.setCurrentGroup( group.getGroup() );
			validateCascadedConstraints( context, valueContext );
		}

		// now we process sequences. For sequences I have to traverse the object graph since I have to stop processing when an error occurs.
		Iterator<List<Group>> sequenceIterator = groupChain.getSequenceIterator();
		while ( sequenceIterator.hasNext() ) {
			List<Group> sequence = sequenceIterator.next();
			for ( Group group : sequence ) {
				int numberOfViolations = context.getFailingConstraints().size();
				valueContext.setCurrentGroup( group.getGroup() );

				validateConstraintsForCurrentGroup( context, valueContext );
				validateCascadedConstraints( context, valueContext );

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

		// if we are validating the default group we have to distinguish between the case where the main entity type redefines the default group and where not
		if ( validatedBeanRedefinesDefault ) {
			validateConstraintsForRedefinedDefaultGroupOnMainEntity( validationContext, valueContext, beanMetaData );
		}
		else {
			validateConstraintsForRedefinedDefaultGroup( validationContext, valueContext, beanMetaData );
		}
	}

	private <T, U, V, E extends ConstraintViolation<T>> void validateConstraintsForRedefinedDefaultGroup(ValidationContext<T, E> validationContext, ValueContext<U, V> valueContext, BeanMetaData<U> beanMetaData) {
		for ( Map.Entry<Class<?>, List<BeanMetaConstraint<U, ? extends Annotation>>> entry : beanMetaData.getMetaConstraintsAsMap()
				.entrySet() ) {
			Class<?> hostingBeanClass = entry.getKey();
			List<BeanMetaConstraint<U, ? extends Annotation>> constraints = entry.getValue();

			List<Class<?>> defaultGroupSequence = getBeanMetaData( hostingBeanClass ).getDefaultGroupSequence();
			PathImpl currentPath = valueContext.getPropertyPath();
			for ( Class<?> defaultSequenceMember : defaultGroupSequence ) {
				valueContext.setCurrentGroup( defaultSequenceMember );
				boolean validationSuccessful = true;
				for ( BeanMetaConstraint<U, ? extends Annotation> metaConstraint : constraints ) {
					boolean tmp = validateConstraint(
							validationContext, valueContext, metaConstraint
					);
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

	private <T, U, V, E extends ConstraintViolation<T>> void validateConstraintsForRedefinedDefaultGroupOnMainEntity(ValidationContext<T, E> validationContext, ValueContext<U, V> valueContext, BeanMetaData<U> beanMetaData) {
		List<Class<?>> defaultGroupSequence = beanMetaData.getDefaultGroupSequence();
		PathImpl currentPath = valueContext.getPropertyPath();
		for ( Class<?> defaultSequenceMember : defaultGroupSequence ) {
			valueContext.setCurrentGroup( defaultSequenceMember );
			boolean validationSuccessful = true;
			for ( BeanMetaConstraint<U, ? extends Annotation> metaConstraint : beanMetaData.getMetaConstraintsAsList() ) {
				boolean tmp = validateConstraint( validationContext, valueContext, metaConstraint );
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
			Object valueToValidate = metaConstraint.getValue( valueContext.getCurrentBean() );
			valueContext.setCurrentValidatedValue( ( V ) valueToValidate );
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
				}
			}
			// reset the path
			valueContext.setPropertyPath( currentPath );
		}
	}

	/**
	 * Validates the cascading parameter specified with the given value context.
	 * Any further cascading references are followed if applicable.
	 *
	 * @param validationContext The global context for the current validateParameter(s) call.
	 * @param valueContext The local context for validating the given parameter.
	 */
	private <T, U, V> void validateCascadedParameter(MethodValidationContext<T> validationContext, ValueContext<U, V> valueContext) {

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
	 *
	 * @return An iterator over the value of a cascaded property.
	 */
	private Iterator<?> createIteratorForCascadedValue(Type type, Object value, ValueContext<?, ?> valueContext) {
		Iterator<?> iter;
		if ( ReflectionHelper.isIterable( type ) ) {
			iter = ( ( Iterable<?> ) value ).iterator();
			valueContext.markCurrentPropertyAsIterable();
		}
		else if ( ReflectionHelper.isMap( type ) ) {
			Map<?, ?> map = ( Map<?, ?> ) value;
			iter = map.entrySet().iterator();
			valueContext.markCurrentPropertyAsIterable();
		}
		else if ( TypeUtils.isArray( type ) ) {
			List<?> arrayList = Arrays.asList( ( Object[] ) value );
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
				mapKey = ( ( Map.Entry<?, ?> ) value ).getKey();
				valueContext.setKey( mapKey );
				value = ( ( Map.Entry<?, ?> ) value ).getValue();
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
			}
			i++;
		}
	}

	private <T> void validateProperty(T object, PathImpl propertyPath, Set<ConstraintViolation<T>> failingConstraintViolations, GroupChain groupChain) {

		@SuppressWarnings("unchecked")
		final Class<T> beanType = ( Class<T> ) object.getClass();

		Set<BeanMetaConstraint<T, ?>> metaConstraints = new HashSet<BeanMetaConstraint<T, ?>>();
		Iterator<Path.Node> propertyIter = propertyPath.iterator();
		Object hostingBeanInstance = collectMetaConstraintsForPath(
				beanType, object, propertyIter, metaConstraints
		);

		if ( hostingBeanInstance == null ) {
			throw new IllegalArgumentException( "Invalid property path." );
		}

		if ( metaConstraints.size() == 0 ) {
			return;
		}

		//this method is at the root of validateProperty calls, share the same cachedTR
		TraversableResolver cachedResolver = getCachingTraversableResolver();

		Iterator<Group> groupIterator = groupChain.getGroupIterator();
		while ( groupIterator.hasNext() ) {
			Group group = groupIterator.next();
			validatePropertyForGroup(
					object,
					propertyPath,
					failingConstraintViolations,
					metaConstraints,
					hostingBeanInstance,
					group,
					cachedResolver
			);
		}

		Iterator<List<Group>> sequenceIterator = groupChain.getSequenceIterator();
		while ( sequenceIterator.hasNext() ) {
			List<Group> sequence = sequenceIterator.next();
			int numberOfConstraintViolationsBefore = failingConstraintViolations.size();
			for ( Group group : sequence ) {
				validatePropertyForGroup(
						object,
						propertyPath,
						failingConstraintViolations,
						metaConstraints,
						hostingBeanInstance,
						group,
						cachedResolver
				);

				if ( failingConstraintViolations.size() > numberOfConstraintViolationsBefore ) {
					break;
				}
			}
		}
	}

	private <T, U, V> void validatePropertyForGroup(
			T object,
			PathImpl path,
			Set<ConstraintViolation<T>> failingConstraintViolations,
			Set<BeanMetaConstraint<T, ?>> metaConstraints,
			U hostingBeanInstance,
			Group group,
			TraversableResolver cachedTraversableResolver) {
		int numberOfConstraintViolationsBefore = failingConstraintViolations.size();

		//TODO GM: is that right?
		BeanMetaData<T> beanMetaData = getBeanMetaData( ( Class<T> ) object.getClass() );

		List<Class<?>> groupList;
		if ( group.isDefaultGroup() ) {
			groupList = beanMetaData.getDefaultGroupSequence();
		}
		else {
			groupList = new ArrayList<Class<?>>();
			groupList.add( group.getGroup() );
		}

		for ( Class<?> groupClass : groupList ) {
			for ( BeanMetaConstraint<T, ?> metaConstraint : metaConstraints ) {
				ValidationContext<T, ConstraintViolation<T>> context = ValidationContext.getContextForValidateProperty(
						object,
						messageInterpolator,
						constraintValidatorFactory,
						cachedTraversableResolver
				);
				ValueContext<U, V> valueContext = ValueContext.getLocalExecutionContext(
						hostingBeanInstance, path
				);
				valueContext.setCurrentGroup( groupClass );
				if ( isValidationRequired( context, valueContext, metaConstraint ) ) {
					Object valueToValidate = metaConstraint.getValue( valueContext.getCurrentBean() );
					valueContext.setCurrentValidatedValue( ( V ) valueToValidate );
					metaConstraint.validateConstraint( context, valueContext );
					failingConstraintViolations.addAll( context.getFailingConstraints() );
				}
			}
			if ( failingConstraintViolations.size() > numberOfConstraintViolationsBefore ) {
				break;
			}
		}
	}

	private <T> void validateValue(Class<T> beanType, Object value, PathImpl propertyPath, Set<ConstraintViolation<T>> failingConstraintViolations, GroupChain groupChain) {
		Set<BeanMetaConstraint<T, ?>> metaConstraints = new HashSet<BeanMetaConstraint<T, ?>>();
		collectMetaConstraintsForPath( beanType, null, propertyPath.iterator(), metaConstraints );

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
					value,
					propertyPath,
					failingConstraintViolations,
					metaConstraints,
					group,
					cachedTraversableResolver
			);
		}

		// process sequences
		Iterator<List<Group>> sequenceIterator = groupChain.getSequenceIterator();
		while ( sequenceIterator.hasNext() ) {
			List<Group> sequence = sequenceIterator.next();
			int numberOfConstraintViolations = failingConstraintViolations.size();
			for ( Group group : sequence ) {
				validateValueForGroup(
						beanType,
						value,
						propertyPath,
						failingConstraintViolations,
						metaConstraints,
						group,
						cachedTraversableResolver
				);

				if ( failingConstraintViolations.size() > numberOfConstraintViolations ) {
					break;
				}
			}
		}
	}

	private <U, V> void validateValueForGroup(
			Class<U> beanType,
			V value,
			PathImpl path,
			Set<ConstraintViolation<U>> failingConstraintViolations,
			Set<BeanMetaConstraint<U, ?>> metaConstraints,
			Group group,
			TraversableResolver cachedTraversableResolver) {
		int numberOfConstraintViolations = failingConstraintViolations.size();

		//TODO GM: is that right?
		BeanMetaData<U> beanMetaData = getBeanMetaData( beanType );

		List<Class<?>> groupList;
		if ( group.isDefaultGroup() ) {
			groupList = beanMetaData.getDefaultGroupSequence();
		}
		else {
			groupList = new ArrayList<Class<?>>();
			groupList.add( group.getGroup() );
		}

		for ( Class<?> groupClass : groupList ) {
			for ( MetaConstraint<U, ?> metaConstraint : metaConstraints ) {
				ValidationContext<U, ConstraintViolation<U>> context = ValidationContext.getContextForValidateValue(
						beanType, messageInterpolator, constraintValidatorFactory, cachedTraversableResolver
				);
				ValueContext<U, V> valueContext = ValueContext.getLocalExecutionContext( beanType, path );
				valueContext.setCurrentGroup( groupClass );
				valueContext.setCurrentValidatedValue( value );
				if ( isValidationRequired( context, valueContext, metaConstraint ) ) {
					metaConstraint.validateConstraint( context, valueContext );
					failingConstraintViolations.addAll( context.getFailingConstraints() );
				}
			}
			if ( failingConstraintViolations.size() > numberOfConstraintViolations ) {
				break;
			}
		}
	}

	private <T> void validateParametersInContext(MethodValidationContext<T> validationContext, T object, Object[] parameterValues, GroupChain groupChain) {

		Method method = validationContext.getMethod();
		BeanMetaData<?> beanMetaData = getBeanMetaData( method.getDeclaringClass() );
		if ( beanMetaData.defaultGroupSequenceIsRedefined() ) {
			groupChain.assertDefaultGroupSequenceIsExpandable( beanMetaData.getDefaultGroupSequence() );
		}

		// process first single groups
		Iterator<Group> groupIterator = groupChain.getGroupIterator();
		while ( groupIterator.hasNext() ) {
			validateParametersForGroup( validationContext, object, parameterValues, groupIterator.next() );
		}

		// now process sequences, stop after the first erroneous group
		Iterator<List<Group>> sequenceIterator = groupChain.getSequenceIterator();
		while ( sequenceIterator.hasNext() ) {
			List<Group> sequence = sequenceIterator.next();
			for ( Group group : sequence ) {
				int numberOfFailingConstraint = validateParametersForGroup(
						validationContext, object, parameterValues, group
				);
				if ( numberOfFailingConstraint > 0 ) {
					break;
				}
			}
		}
	}

	private <T> int validateParametersForGroup(MethodValidationContext<T> validationContext, T object, Object[] parameterValues, Group group) {

		int numberOfViolationsBefore = validationContext.getFailingConstraints().size();

		Method method = validationContext.getMethod();

		BeanMetaData<?> beanMetaData = getBeanMetaData( method.getDeclaringClass() );
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
			groupList = beanMetaData.getDefaultGroupSequence();
		}
		else {
			groupList = Arrays.<Class<?>>asList( group.getGroup() );
		}

		//the only case where we can have multiple groups here is a redefined default group sequence
		for ( Class<?> oneGroup : groupList ) {

			int numberOfViolationsOfCurrentGroup = 0;

			for ( Entry<Class<?>, MethodMetaData> constraintsOfOneClass : methodMetaDataByType.entrySet() ) {

				for ( int i = 0; i < parameterValues.length; i++ ) {

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
				}
			}

			//stop processing after first group with errors occurred
			if ( numberOfViolationsOfCurrentGroup > 0 ) {
				break;
			}
		}

		// validate parameter beans annotated with @Valid if required
		for ( int i = 0; i < parameterValues.length; i++ ) {

			Object value = parameterValues[i];
			String parameterName = methodMetaDataOfDeclaringType.getParameterMetaData( i ).getParameterName();

			if ( isCascadeRequired( method, i ) && value != null ) {

				ValueContext<Object, ?> cascadingvalueContext = ValueContext.getLocalExecutionContext(
						value, PathImpl.createPathForMethodParameter( method, parameterName ), i, parameterName
				);
				cascadingvalueContext.setCurrentGroup( group.getGroup() );

				//TODO GM: consider violations from cascaded validation
				validateCascadedParameter( validationContext, cascadingvalueContext );
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
		}

		return validationContext.getFailingConstraints().size() - numberOfViolationsBefore;
	}

	private <V, T> Set<MethodConstraintViolation<T>> validateReturnValueInContext(MethodValidationContext<T> context, T bean, Method method, V value, GroupChain groupChain) {

		Set<MethodConstraintViolation<T>> constraintViolations = new HashSet<MethodConstraintViolation<T>>();

		BeanMetaData<?> beanMetaData = getBeanMetaData( method.getDeclaringClass() );

		if ( beanMetaData.defaultGroupSequenceIsRedefined() ) {
			groupChain.assertDefaultGroupSequenceIsExpandable( beanMetaData.getDefaultGroupSequence() );
		}

		ValueContext<T, V> valueContext = ValueContext.getLocalExecutionContext(
				bean, PathImpl.createPathForMethodReturnValue( method )
		);
		valueContext.setCurrentValidatedValue( value );
//		valueContext.setParameterIndex(parameterIndex);

		// process first single groups. For these we can optimise object traversal by first running all validations on the current bean
		// before traversing the object.
		Iterator<Group> groupIterator = groupChain.getGroupIterator();

		// validate constraints at the parameters themselves
		while ( groupIterator.hasNext() ) {

			Group group = groupIterator.next();
			valueContext.setCurrentGroup( group.getGroup() );
			validateReturnValueForGroup( context, valueContext );
		}
		constraintViolations.addAll( ( Collection<? extends MethodConstraintViolation<T>> ) context.getFailingConstraints() );

//		// validate parameter beans annotated with @Valid
//		if ( isCascadeRequired( method, parameterIndex ) && value != null ) {
//
//			ValueContext<V, ?> cascadingvalueContext = ValueContext.getLocalExecutionContext( value, PathImpl.createPathForMethodParameter(method, parameterIndex) );
//
//			groupIterator = groupChain.getGroupIterator();
//			while ( groupIterator.hasNext() ) {
//
//				Group group = groupIterator.next();
//				cascadingvalueContext.setCurrentGroup( group.getGroup() );
//				cascadingvalueContext.setParameterIndex(parameterIndex);
//				validateConstraintsForCurrentGroup( context, cascadingvalueContext );
//			}
//
//			// validate cascaded constraints of parameter bean
//			groupIterator = groupChain.getGroupIterator();
//			while ( groupIterator.hasNext() ) {
//				Group group = groupIterator.next();
//				cascadingvalueContext.setCurrentGroup( group.getGroup() );
//				cascadingvalueContext.setParameterIndex(parameterIndex);
//				validateCascadedConstraints( context, cascadingvalueContext );
//			}
//			constraintViolations.addAll( (Collection<? extends MethodConstraintViolation<T>>) context.getFailingConstraints() );
//		}

		//TODO GM: evaluate group sequences

		return constraintViolations;
	}

	private <T, U, V> Set<MethodConstraintViolation<T>> validateReturnValueForGroup(MethodValidationContext<T> validationContext, ValueContext<U, V> valueContext) {

		Set<MethodConstraintViolation<T>> constraintViolations = new HashSet<MethodConstraintViolation<T>>();

		BeanMetaData<?> beanMetaData = getBeanMetaData( validationContext.getMethod().getDeclaringClass() );
		Map<Class<?>, MethodMetaData> methodMetaData = beanMetaData.getMetaDataForMethod( validationContext.getMethod() );

		for ( Entry<Class<?>, MethodMetaData> constraintsOfOneClass : methodMetaData.entrySet() ) {

			MethodMetaData metaData = constraintsOfOneClass.getValue();

			for ( MetaConstraint<?, ? extends Annotation> metaConstraint : metaData ) {

				if ( !metaConstraint.getGroupList().contains( valueContext.getCurrentGroup() ) ) {
					continue;
				}
				metaConstraint.validateConstraint( validationContext, valueContext );
				constraintViolations.addAll( validationContext.getFailingConstraints() );
			}
		}

		return constraintViolations;
	}

	/**
	 * Collects all <code>MetaConstraint</code>s which match the given path relative to the specified root class.
	 * <p>
	 * This method is called recursively.
	 * </p>
	 *
	 * @param clazz the class type to check for constraints.
	 * @param value While resolving the property path this instance points to the current object. Might be <code>null</code>.
	 * @param propertyIter an instance of <code>PropertyIterator</code> in order to iterate the items of the original property path.
	 * @param metaConstraints Set of <code>MetaConstraint</code>s to collect all matching constraints.
	 *
	 * @return Returns the bean hosting the constraints which match the specified property path.
	 */
	private <T> Object collectMetaConstraintsForPath(Class<T> clazz, Object value, Iterator<Path.Node> propertyIter, Set<BeanMetaConstraint<T, ?>> metaConstraints) {
		Path.Node elem = propertyIter.next();
		Object newValue = value;

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
					newValue = newValue == null ? null : ReflectionHelper.getValue( m, newValue );
					if ( elem.isInIterable() ) {
						if ( newValue != null && elem.getIndex() != null ) {
							newValue = ReflectionHelper.getIndexedValue( newValue, elem.getIndex() );
						}
						else if ( newValue != null && elem.getKey() != null ) {
							newValue = ReflectionHelper.getMappedValue( newValue, elem.getKey() );
						}
						else if ( newValue != null ) {
							throw new IllegalArgumentException( "Property path must provide index or map key" );
						}
						type = ReflectionHelper.getIndexedType( type );
					}

					@SuppressWarnings("unchecked")
					Class<T> valueClass = ( Class<T> ) ( newValue == null ? type : newValue.getClass() );

					return collectMetaConstraintsForPath(
							valueClass,
							newValue,
							propertyIter,
							metaConstraints
					);
				}
			}
		}
		return newValue;
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
}

