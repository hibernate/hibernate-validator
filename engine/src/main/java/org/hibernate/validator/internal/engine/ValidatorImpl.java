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
package org.hibernate.validator.internal.engine;

import java.lang.annotation.ElementType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.ConstraintViolation;
import javax.validation.MessageInterpolator;
import javax.validation.MethodValidator;
import javax.validation.Path;
import javax.validation.TraversableResolver;
import javax.validation.Validator;
import javax.validation.groups.Default;
import javax.validation.metadata.BeanDescriptor;

import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorManager;
import org.hibernate.validator.internal.engine.groups.Group;
import org.hibernate.validator.internal.engine.groups.Sequence;
import org.hibernate.validator.internal.engine.groups.ValidationOrder;
import org.hibernate.validator.internal.engine.groups.ValidationOrderGenerator;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.hibernate.validator.internal.engine.resolver.SingleThreadCachedTraversableResolver;
import org.hibernate.validator.internal.metadata.BeanMetaDataManager;
import org.hibernate.validator.internal.metadata.aggregated.BeanMetaData;
import org.hibernate.validator.internal.metadata.aggregated.ExecutableMetaData;
import org.hibernate.validator.internal.metadata.aggregated.ParameterMetaData;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.raw.ExecutableElement;
import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.ReflectionHelper;
import org.hibernate.validator.internal.util.TypeHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashMap;
import static org.hibernate.validator.internal.util.logging.Messages.MESSAGES;

/**
 * The main Bean Validation class. This is the core processing class of Hibernate Validator.
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Kevin Pollet <kevin.pollet@serli.com> (C) 2011 SERLI
 */
public class ValidatorImpl implements Validator, MethodValidator {

	private static final Log log = LoggerFactory.make();

	/**
	 * The default group array used in case any of the validate methods is called without a group.
	 */
	private static final Class<?>[] DEFAULT_GROUP_ARRAY = new Class<?>[] { Default.class };

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
	 * Indicates if validation has to be stopped on first constraint violation.
	 */
	private final boolean failFast;

	public ValidatorImpl(ConstraintValidatorFactory constraintValidatorFactory,
						 MessageInterpolator messageInterpolator,
						 TraversableResolver traversableResolver,
						 BeanMetaDataManager beanMetaDataManager,
						 ConstraintValidatorManager constraintValidatorManager,
						 boolean failFast) {
		this.constraintValidatorFactory = constraintValidatorFactory;
		this.messageInterpolator = messageInterpolator;
		this.traversableResolver = traversableResolver;
		this.beanMetaDataManager = beanMetaDataManager;
		this.constraintValidatorManager = constraintValidatorManager;
		this.failFast = failFast;

		validationOrderGenerator = new ValidationOrderGenerator();
	}

	@Override
	public final <T> Set<ConstraintViolation<T>> validate(T object, Class<?>... groups) {
		Contracts.assertNotNull( object, MESSAGES.validatedObjectMustNotBeNull() );

		ValidationOrder validationOrder = determineGroupValidationOrder( groups );

		ValidationContext<T, ConstraintViolation<T>> validationContext = ValidationContext.getContextForValidate(
				beanMetaDataManager,
				constraintValidatorManager,
				object,
				messageInterpolator,
				constraintValidatorFactory,
				getCachingTraversableResolver(),
				failFast
		);

		ValueContext<?, T> valueContext = ValueContext.getLocalExecutionContext( object, PathImpl.createRootPath() );

		return validateInContext( valueContext, validationContext, validationOrder );
	}

	@Override
	public final <T> Set<ConstraintViolation<T>> validateProperty(T object, String propertyName, Class<?>... groups) {
		Contracts.assertNotNull( object, MESSAGES.validatedObjectMustNotBeNull() );

		sanityCheckPropertyPath( propertyName );
		ValidationOrder validationOrder = determineGroupValidationOrder( groups );

		ValidationContext<T, ConstraintViolation<T>> context = ValidationContext.getContextForValidateProperty(
				beanMetaDataManager,
				constraintValidatorManager,
				object,
				messageInterpolator,
				constraintValidatorFactory,
				getCachingTraversableResolver(),
				failFast
		);

		return validatePropertyInContext( context, PathImpl.createPathFromString( propertyName ), validationOrder );
	}

	@Override
	public final <T> Set<ConstraintViolation<T>> validateValue(Class<T> beanType, String propertyName, Object value, Class<?>... groups) {

		Contracts.assertNotNull( beanType, MESSAGES.beanTypeCannotBeNull() );

		sanityCheckPropertyPath( propertyName );
		ValidationOrder validationOrder = determineGroupValidationOrder( groups );

		ValidationContext<T, ConstraintViolation<T>> context = ValidationContext.getContextForValidateValue(
				beanMetaDataManager,
				constraintValidatorManager,
				beanType,
				messageInterpolator,
				constraintValidatorFactory,
				getCachingTraversableResolver(),
				failFast
		);

		return validateValueInContext( context, value, PathImpl.createPathFromString( propertyName ), validationOrder );
	}

	@Override
	public <T> Set<ConstraintViolation<T>> validateParameters(T object, Method method, Object[] parameterValues, Class<?>... groups) {

		Contracts.assertNotNull( object, MESSAGES.validatedObjectMustNotBeNull() );
		Contracts.assertNotNull( method, MESSAGES.validatedMethodMustNotBeNull() );

		return validateParameters( object, ExecutableElement.forMethod( method ), parameterValues, groups );
	}

	@Override
	public <T> Set<ConstraintViolation<T>> validateConstructorParameters(Constructor<T> constructor, Object[] parameterValues, Class<?>... groups) {

		Contracts.assertNotNull( constructor, MESSAGES.validatedConstructorMustNotBeNull() );

		return validateParameters( null, ExecutableElement.forConstructor( constructor ), parameterValues, groups );
	}

	private <T> Set<ConstraintViolation<T>> validateParameters(T object, ExecutableElement executable, Object[] parameterValues, Class<?>... groups) {
		//this might be the case for parameterless methods
		if ( parameterValues == null ) {
			return Collections.emptySet();
		}

		ValidationOrder validationOrder = determineGroupValidationOrder( groups );

		MethodValidationContext<T> context = ValidationContext.getContextForValidateParameters(
				beanMetaDataManager,
				constraintValidatorManager,
				executable,
				parameterValues,
				object,
				messageInterpolator,
				constraintValidatorFactory,
				getCachingTraversableResolver(),
				failFast
		);

		validateParametersInContext( context, object, parameterValues, validationOrder );

		return context.getFailingConstraints();
	}

	@Override
	public <T> Set<ConstraintViolation<T>> validateConstructorReturnValue(Constructor<T> constructor, T createdObject, Class<?>... groups) {

		Contracts.assertNotNull( constructor, MESSAGES.validatedConstructorMustNotBeNull() );

		return validateReturnValue( null, ExecutableElement.forConstructor( constructor ), createdObject, groups );
	}

	@Override
	public <T> Set<ConstraintViolation<T>> validateReturnValue(T object, Method method, Object returnValue, Class<?>... groups) {

		Contracts.assertNotNull( method, MESSAGES.validatedMethodMustNotBeNull() );

		return validateReturnValue( object, ExecutableElement.forMethod( method ), returnValue, groups );
	}

	private <T> Set<ConstraintViolation<T>> validateReturnValue(T object, ExecutableElement executable, Object returnValue, Class<?>... groups) {

		ValidationOrder validationOrder = determineGroupValidationOrder( groups );

		MethodValidationContext<T> context = ValidationContext.getContextForValidateParameters(
				beanMetaDataManager,
				constraintValidatorManager,
				executable,
				null,
				object,
				messageInterpolator,
				constraintValidatorFactory,
				getCachingTraversableResolver(),
				failFast
		);

		validateReturnValueInContext( context, object, returnValue, validationOrder );

		return context.getFailingConstraints();
	}

	@Override
	public final BeanDescriptor getConstraintsForClass(Class<?> clazz) {
		return beanMetaDataManager.getBeanMetaData( clazz ).getBeanDescriptor();
	}

	@Override
	public final <T> T unwrap(Class<T> type) {
		if ( type.isAssignableFrom( getClass() ) ) {
			return type.cast( this );
		}
		throw log.getTypeNotSupportedException( type );
	}

	@Override
	public MethodValidator forMethods() {
		return this;
	}

	private void sanityCheckPropertyPath(String propertyName) {
		if ( propertyName == null || propertyName.length() == 0 ) {
			throw log.getInvalidPropertyPathException();
		}
	}

	private ValidationOrder determineGroupValidationOrder(Class<?>[] groups) {
		Contracts.assertNotNull( groups, MESSAGES.groupMustNotBeNull() );

		Class<?>[] tmpGroups = groups;
		// if no groups is specified use the default
		if ( tmpGroups.length == 0 ) {
			tmpGroups = DEFAULT_GROUP_ARRAY;
		}

		return validationOrderGenerator.getValidationOrder( Arrays.asList( tmpGroups ) );
	}

	/**
	 * Validates the given object using the available context information.
	 *
	 * @param valueContext the current validation context
	 * @param context the global validation context
	 * @param validationOrder Contains the information which and in which order groups have to be executed
	 * @param <T> The root bean type
	 * @param <V> The type of the current object on the validation stack
	 *
	 * @return Set of constraint violations or the empty set if there were no violations.
	 */
	private <T, U, V, E extends ConstraintViolation<T>> Set<E> validateInContext(ValueContext<U, V> valueContext, ValidationContext<T, E> context, ValidationOrder validationOrder) {
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

	private <T, U, V, E extends ConstraintViolation<T>> void validateConstraintsForCurrentGroup(ValidationContext<T, E> validationContext, ValueContext<U, V> valueContext) {
		// we are not validating the default group there is nothing special to consider. If we are validating the default
		// group sequence we have to consider that a class in the hierarchy could redefine the default group sequence.
		if ( !valueContext.validatingDefault() ) {
			validateConstraintsForNonDefaultGroup( validationContext, valueContext );
		}
		else {
			validateConstraintsForDefaultGroup( validationContext, valueContext );
		}
	}

	private <T, U, V, E extends ConstraintViolation<T>> void validateConstraintsForDefaultGroup(ValidationContext<T, E> validationContext, ValueContext<U, V> valueContext) {
		final BeanMetaData<U> beanMetaData = beanMetaDataManager.getBeanMetaData( valueContext.getCurrentBeanType() );
		final Map<Class<?>, Class<?>> validatedInterfaces = newHashMap();

		// evaluating the constraints of a bean per class in hierarchy, this is necessary to detect potential default group re-definitions
		for ( Class<?> clazz : beanMetaData.getClassHierarchy() ) {
			@SuppressWarnings("unchecked")
			BeanMetaData<U> hostingBeanMetaData = ( BeanMetaData<U> ) beanMetaDataManager.getBeanMetaData( clazz );
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
					final Class<?> declaringClass = metaConstraint.getLocation().getBeanClass();
					if ( declaringClass.isInterface() ) {
						Class<?> validatedForClass = validatedInterfaces.get( declaringClass );
						if ( validatedForClass != null && !validatedForClass.equals( clazz ) ) {
							continue;
						}
						validatedInterfaces.put( declaringClass, clazz );
					}

					boolean tmp = validateConstraint( validationContext, valueContext, metaConstraint );
					if ( shouldFailFast( validationContext ) ) {
						return;
					}
					validationSuccessful = validationSuccessful && tmp;
					valueContext.setPropertyPath( currentPath );
				}
				if ( !validationSuccessful ) {
					break;
				}
			}
			validationContext.markProcessed( valueContext );

			// all constraints in the hierarchy has been validated, stop validation.
			if ( defaultGroupSequenceIsRedefined ) {
				break;
			}
		}
	}

	private <T, U, V> void validateConstraintsForNonDefaultGroup(ValidationContext<T, ?> validationContext, ValueContext<U, V> valueContext) {
		BeanMetaData<U> beanMetaData = beanMetaDataManager.getBeanMetaData( valueContext.getCurrentBeanType() );
		PathImpl currentPath = valueContext.getPropertyPath();
		for ( MetaConstraint<?> metaConstraint : beanMetaData.getMetaConstraints() ) {
			validateConstraint( validationContext, valueContext, metaConstraint );
			if ( shouldFailFast( validationContext ) ) {
				return;
			}
			// reset the path to the state before this call
			valueContext.setPropertyPath( currentPath );
		}
		validationContext.markProcessed( valueContext );
	}

	private <T, U, V> boolean validateConstraint(ValidationContext<T, ?> validationContext,
												 ValueContext<U, V> valueContext,
												 MetaConstraint<?> metaConstraint) {
		boolean validationSuccessful = true;

		if ( metaConstraint.getElementType() != ElementType.TYPE ) {
			valueContext.appendNode( ReflectionHelper.getPropertyName( metaConstraint.getLocation().getMember() ) );
		}
		else {
			valueContext.appendNode( null );
		}

		if ( isValidationRequired( validationContext, valueContext, metaConstraint ) ) {
			@SuppressWarnings("unchecked")
			V valueToValidate = ( V ) metaConstraint.getValue( valueContext.getCurrentBean() );
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
		BeanMetaData<U> beanMetaData = beanMetaDataManager.getBeanMetaData( valueContext.getCurrentBeanType() );
		Set<Member> cascadedMembers = beanMetaData.getCascadedMembers();
		PathImpl currentPath = valueContext.getPropertyPath();
		for ( Member member : cascadedMembers ) {
			String newNode = ReflectionHelper.getPropertyName( member );
			valueContext.appendNode( newNode );

			if ( isCascadeRequired( validationContext, valueContext, member ) ) {
				Object value = ReflectionHelper.getValue( member, valueContext.getCurrentBean() );
				if ( value != null ) {
					Type type = value.getClass();
					Iterator<?> iter = createIteratorForCascadedValue( type, value, valueContext );
					boolean isIndexable = isIndexable( type );
					validateCascadedConstraint(
							validationContext,
							iter,
							isIndexable,
							valueContext
					);
					if ( shouldFailFast( validationContext ) ) {
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
			iter = ( ( Iterable<?> ) value ).iterator();
			valueContext.markCurrentPropertyAsIterable();
		}
		else if ( ReflectionHelper.isMap( type ) ) {
			Map<?, ?> map = ( Map<?, ?> ) value;
			iter = map.entrySet().iterator();
			valueContext.markCurrentPropertyAsIterable();
		}
		else if ( TypeHelper.isArray( type ) ) {
			List<?> arrayList = Arrays.asList( ( Object[] ) value );
			iter = arrayList.iterator();
			valueContext.markCurrentPropertyAsIterable();
		}
		else {
			List<Object> list = newArrayList();
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
		else if ( TypeHelper.isArray( type ) ) {
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
				ValidationOrder validationOrder = validationOrderGenerator.getValidationOrder(
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

				validateInContext( newValueContext, context, validationOrder );
				if ( shouldFailFast( context ) ) {
					return;
				}
			}
			i++;
		}
	}

	private <T, U, V> Set<ConstraintViolation<T>> validatePropertyInContext(ValidationContext<T, ConstraintViolation<T>> context, PathImpl propertyPath, ValidationOrder validationOrder) {
		List<MetaConstraint<?>> metaConstraints = newArrayList();
		Iterator<Path.Node> propertyIter = propertyPath.iterator();
		ValueContext<U, V> valueContext = collectMetaConstraintsForPath(
				context.getRootBeanClass(),
				context.getRootBean(),
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

		BeanMetaData<U> beanMetaData = beanMetaDataManager.getBeanMetaData( valueContext.getCurrentBeanType() );
		if ( beanMetaData.defaultGroupSequenceIsRedefined() ) {
			validationOrder.assertDefaultGroupSequenceIsExpandable( beanMetaData.getDefaultGroupSequence( valueContext.getCurrentBean() ) );
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

	private <T, U, V> Set<ConstraintViolation<T>> validateValueInContext(ValidationContext<T, ConstraintViolation<T>> context, V value, PathImpl propertyPath, ValidationOrder validationOrder) {
		List<MetaConstraint<?>> metaConstraints = newArrayList();
		ValueContext<U, V> valueContext = collectMetaConstraintsForPath(
				context.getRootBeanClass(), null, propertyPath.iterator(), propertyPath, metaConstraints
		);
		valueContext.setCurrentValidatedValue( value );

		if ( metaConstraints.size() == 0 ) {
			return context.getFailingConstraints();
		}

		BeanMetaData<U> beanMetaData = beanMetaDataManager.getBeanMetaData( valueContext.getCurrentBeanType() );
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
	private <T, U, V> int validatePropertyForCurrentGroup(ValueContext<U, V> valueContext, ValidationContext<T, ConstraintViolation<T>> validationContext, List<MetaConstraint<?>> metaConstraints) {
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
	private <T, U, V> int validatePropertyForNonDefaultGroup(ValueContext<U, V> valueContext, ValidationContext<T, ConstraintViolation<T>> validationContext, List<MetaConstraint<?>> metaConstraints) {
		int numberOfConstraintViolationsBefore = validationContext.getFailingConstraints().size();

		for ( MetaConstraint<?> metaConstraint : metaConstraints ) {
			if ( isValidationRequired( validationContext, valueContext, metaConstraint ) ) {
				if ( valueContext.getCurrentBean() != null ) {
					@SuppressWarnings("unchecked")
					V valueToValidate = ( V ) metaConstraint.getValue( valueContext.getCurrentBean() );
					valueContext.setCurrentValidatedValue( valueToValidate );
				}
				metaConstraint.validateConstraint( validationContext, valueContext );
				if ( shouldFailFast( validationContext ) ) {
					return validationContext.getFailingConstraints()
							.size() - numberOfConstraintViolationsBefore;
				}
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
	private <T, U, V> int validatePropertyForDefaultGroup(ValueContext<U, V> valueContext,
														  ValidationContext<T, ConstraintViolation<T>> validationContext,
														  List<MetaConstraint<?>> constraintList) {
		final int numberOfConstraintViolationsBefore = validationContext.getFailingConstraints().size();
		final BeanMetaData<U> beanMetaData = beanMetaDataManager.getBeanMetaData( valueContext.getCurrentBeanType() );
		final Map<Class<?>, Class<?>> validatedInterfaces = newHashMap();

		// evaluating the constraints of a bean per class in hierarchy. this is necessary to detect potential default group re-definitions
		for ( Class<?> clazz : beanMetaData.getClassHierarchy() ) {
			BeanMetaData<U> hostingBeanMetaData = ( BeanMetaData<U> ) beanMetaDataManager.getBeanMetaData( clazz );
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
					final Class<?> declaringClass = metaConstraint.getLocation().getBeanClass();
					if ( declaringClass.isInterface() ) {
						Class<?> validatedForClass = validatedInterfaces.get( declaringClass );
						if ( validatedForClass != null && !validatedForClass.equals( clazz ) ) {
							continue;
						}
						validatedInterfaces.put( declaringClass, clazz );
					}

					if ( constraintList.contains( metaConstraint )
							&& isValidationRequired( validationContext, valueContext, metaConstraint ) ) {

						if ( valueContext.getCurrentBean() != null ) {
							@SuppressWarnings("unchecked")
							V valueToValidate = ( V ) metaConstraint.getValue( valueContext.getCurrentBean() );
							valueContext.setCurrentValidatedValue( valueToValidate );
						}
						boolean tmp = metaConstraint.validateConstraint( validationContext, valueContext );
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

	private <T> void validateParametersInContext(MethodValidationContext<T> validationContext,
												 T object,
												 Object[] parameterValues,
												 ValidationOrder validationOrder) {

		BeanMetaData<T> beanMetaData = beanMetaDataManager.getBeanMetaData( validationContext.getRootBeanClass() );

		//assert that there are no illegal method parameter constraints
		ExecutableMetaData methodMetaData = beanMetaData.getMetaDataFor( validationContext.getExecutable() );
		methodMetaData.assertCorrectnessOfMethodParameterConstraints();

		if ( beanMetaData.defaultGroupSequenceIsRedefined() ) {
			validationOrder.assertDefaultGroupSequenceIsExpandable( beanMetaData.getDefaultGroupSequence( object ) );
		}

		// process first single groups
		Iterator<Group> groupIterator = validationOrder.getGroupIterator();
		while ( groupIterator.hasNext() ) {
			validateParametersForGroup( validationContext, object, parameterValues, groupIterator.next() );
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
						validationContext, object, parameterValues, group
				);
				if ( shouldFailFast( validationContext ) ) {
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

		ExecutableElement executable = validationContext.getExecutable();

		BeanMetaData<T> beanMetaData = beanMetaDataManager.getBeanMetaData( validationContext.getRootBeanClass() );
		ExecutableMetaData methodMetaData = beanMetaData.getMetaDataFor( executable );

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
			groupList = Arrays.<Class<?>>asList( group.getDefiningClass() );
		}

		//the only case where we can have multiple groups here is a redefined default group sequence
		for ( Class<?> oneGroup : groupList ) {

			int numberOfViolationsOfCurrentGroup = 0;

			for ( int i = 0; i < parameterValues.length; i++ ) {
				Object value = parameterValues[i];
				String parameterName = methodMetaData.getParameterMetaData( i ).getName();

				// validate constraints at parameter itself
				ValueContext<T, Object> valueContext;

				if ( object != null ) {
					valueContext = ValueContext.getLocalExecutionContext(
							object, PathImpl.createPathForParameter( executable, parameterName ), i, parameterName
					);
				}
				else {
					valueContext = ValueContext.getLocalExecutionContext(
							object,
							( Class<T> ) executable.getMember().getDeclaringClass(),
							PathImpl.createPathForParameter( executable, parameterName ),
							i,
							parameterName
					);
				}
				valueContext.setCurrentValidatedValue( value );
				valueContext.setCurrentGroup( oneGroup );

				numberOfViolationsOfCurrentGroup += validateParameterForGroup(
						validationContext, valueContext, methodMetaData.getParameterMetaData( i )
				);
				if ( shouldFailFast( validationContext ) ) {
					return validationContext.getFailingConstraints().size() - numberOfViolationsBefore;
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
			ParameterMetaData parameterMetaData = methodMetaData.getParameterMetaData( i );
			String parameterName = parameterMetaData.getName();

			if ( parameterMetaData.isCascading() && value != null ) {

				ValueContext<Object, ?> cascadingValueContext = ValueContext.getLocalExecutionContext(
						value, PathImpl.createPathForParameter( executable, parameterName ), i, parameterName
				);
				cascadingValueContext.setCurrentGroup( group.getDefiningClass() );

				//TODO GM: consider violations from cascaded validation
				validateCascadedMethodConstraints( validationContext, cascadingValueContext );
				if ( shouldFailFast( validationContext ) ) {
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

		for ( MetaConstraint<?> metaConstraint : parameterMetaData ) {

			//ignore constraints not part of the evaluated group
			if ( !metaConstraint.getGroupList().contains( valueContext.getCurrentGroup() ) ) {
				continue;
			}

			metaConstraint.validateConstraint( validationContext, valueContext );
			if ( shouldFailFast( validationContext ) ) {
				break;
			}
		}

		return validationContext.getFailingConstraints().size() - numberOfViolationsBefore;
	}

	private <V, T> void validateReturnValueInContext(MethodValidationContext<T> context, T bean, V value, ValidationOrder validationOrder) {

		BeanMetaData<T> beanMetaData = beanMetaDataManager.getBeanMetaData( context.getRootBeanClass() );

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
				if ( numberOfFailingConstraint > 0 ) {
					break;
				}
			}
		}
	}

	//TODO GM: if possible integrate with validateParameterForGroup()
	private <T, V> int validateReturnValueForGroup(MethodValidationContext<T> validationContext, T bean, V value, Group group) {

		int numberOfViolationsBefore = validationContext.getFailingConstraints().size();

		ExecutableElement executable = validationContext.getExecutable();

		BeanMetaData<T> beanMetaData = beanMetaDataManager.getBeanMetaData( validationContext.getRootBeanClass() );
		ExecutableMetaData methodMetaData = beanMetaData.getMetaDataFor( executable );

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
			ValueContext<T, V> valueContext;
			if ( bean != null ) {
				valueContext = ValueContext.getLocalExecutionContext(
						bean, PathImpl.createPathForMethodReturnValue( executable )
				);
			}
			else {
				//constructor validation
				valueContext = ValueContext.getLocalExecutionContext(
						( Class<T> ) executable.getMember().getDeclaringClass(),
						PathImpl.createPathForMethodReturnValue( executable )
				);
			}

			valueContext.setCurrentValidatedValue( value );
			valueContext.setCurrentGroup( oneGroup );

			numberOfViolationsOfCurrentGroup +=
					validateReturnValueForGroup(
							validationContext, valueContext, methodMetaData
					);
			if ( shouldFailFast( validationContext ) ) {
				return validationContext.getFailingConstraints().size() - numberOfViolationsBefore;
			}

			//stop processing after first group with errors occurred
			if ( numberOfViolationsOfCurrentGroup > 0 ) {
				break;
			}
		}

		// cascaded validation if required
		if ( methodMetaData.isCascading() && value != null ) {

			ValueContext<V, Object> cascadingvalueContext = ValueContext.getLocalExecutionContext(
					value, PathImpl.createPathForMethodReturnValue( executable )
			);
			cascadingvalueContext.setCurrentGroup( group.getDefiningClass() );

			validateCascadedMethodConstraints( validationContext, cascadingvalueContext );
		}

		return validationContext.getFailingConstraints().size() - numberOfViolationsBefore;
	}

	private <T, V> int validateReturnValueForGroup(MethodValidationContext<T> validationContext,
												   ValueContext<T, V> valueContext, ExecutableMetaData methodMetaData) {

		int numberOfViolationsBefore = validationContext.getFailingConstraints().size();

		for ( MetaConstraint<?> metaConstraint : methodMetaData ) {

			if ( !metaConstraint.getGroupList().contains( valueContext.getCurrentGroup() ) ) {
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
	 * @param clazz The class type to check for constraints.
	 * @param value While resolving the property path this instance points to the current object. Might be {@code null}.
	 * @param propertyIter An instance of {@code PropertyIterator} in order to iterate the items of the original property path.
	 * @param propertyPath The property path for which constraints have to be collected.
	 * @param metaConstraintsList An instance of {@code Map} where {@code MetaConstraint}s which match the given path are saved for each class in the hosting class hierarchy.
	 *
	 * @return Returns an instance of {@code ValueContext} which describes the local validation context associated to the given property path.
	 */
	private <T, U, V> ValueContext<U, V> collectMetaConstraintsForPath(Class<T> clazz, Object value, Iterator<Path.Node> propertyIter, PathImpl propertyPath, List<MetaConstraint<?>> metaConstraintsList) {
		Path.Node elem = propertyIter.next();
		Object newValue = value;

		BeanMetaData<?> metaData = beanMetaDataManager.getBeanMetaData( clazz );
		//use precomputed method list as ReflectionHelper#containsMember is slow
		if ( !metaData.isPropertyPresent( elem.getName() ) ) {
			throw log.getInvalidPropertyPathException( elem.getName(), metaData.getBeanClass().getName() );
		}

		if ( !propertyIter.hasNext() ) {
			for ( Class<?> hierarchyClass : metaData.getClassHierarchy() ) {
				metaData = beanMetaDataManager.getBeanMetaData( hierarchyClass );
				for ( MetaConstraint<?> constraint : metaData.getDirectMetaConstraints() ) {
					if ( elem.getName() != null && elem.getName()
							.equals( ReflectionHelper.getPropertyName( constraint.getLocation().getMember() ) ) ) {
						metaConstraintsList.add( constraint );
					}
				}
			}
		}
		else {
			Set<Member> cascadedMembers = metaData.getCascadedMembers();
			for ( Member m : cascadedMembers ) {
				if ( ReflectionHelper.getPropertyName( m ).equals( elem.getName() ) ) {
					Type type = ReflectionHelper.getType( m );
					newValue = newValue == null ? null : ReflectionHelper.getValue( m, newValue );
					if ( elem.isInIterable() ) {
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

					Class<?> castedValueClass = newValue == null ? ( Class<?> ) type : newValue.getClass();
					return collectMetaConstraintsForPath(
							castedValueClass,
							newValue,
							propertyIter,
							propertyPath,
							metaConstraintsList
					);
				}
			}
		}

		if ( newValue == null ) {
			return ValueContext.getLocalExecutionContext( ( Class<U> ) clazz, propertyPath );
		}
		return ValueContext.getLocalExecutionContext( ( U ) value, propertyPath );
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

	private boolean isValidationRequired(ValidationContext<?, ?> validationContext, ValueContext<?, ?> valueContext, MetaConstraint<?> metaConstraint) {
		if ( !metaConstraint.getGroupList().contains( valueContext.getCurrentGroup() ) ) {
			return false;
		}

		// HV-524 - class level constraints are reachable
		if ( ElementType.TYPE.equals( metaConstraint.getElementType() ) ) {
			return true;
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
			throw log.getErrorDuringCallOfTraversableResolverIsReachableException( e );
		}

		return isReachable;
	}

	private boolean isCascadeRequired(ValidationContext<?, ?> validationContext, ValueContext<?, ?> valueContext, Member member) {
		final ElementType type = member instanceof Field ? ElementType.FIELD : ElementType.METHOD;
		boolean isReachable;
		boolean isCascadable;

		PathImpl path = valueContext.getPropertyPath();
		Path pathToObject = path.getPathWithoutLeafNode();

		// HV-524 - class level constraints are reachable
		if ( ElementType.TYPE.equals( type ) ) {
			isReachable = true;
		}
		else {
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
				throw log.getErrorDuringCallOfTraversableResolverIsReachableException( e );
			}
		}

		if ( ElementType.TYPE.equals( type ) ) {
			isCascadable = true;
		}
		else {
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
				throw log.getErrorDuringCallOfTraversableResolverIsCascadableException( e );
			}
		}

		return isReachable && isCascadable;
	}

	private boolean shouldFailFast(ValidationContext context) {
		return context.isFailFastModeEnabled() && !context.getFailingConstraints().isEmpty();
	}
}

