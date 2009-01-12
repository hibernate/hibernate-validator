// $Id$
/*
* JBoss, Home of Professional Open Source
* Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.hibernate.validation.engine;

import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.ListIterator;
import javax.validation.BeanDescriptor;
import javax.validation.Constraint;
import javax.validation.ConstraintDescriptor;
import javax.validation.ConstraintFactory;
import javax.validation.ConstraintViolation;
import javax.validation.MessageResolver;
import javax.validation.TraversableResolver;
import javax.validation.ValidationException;
import javax.validation.Validator;
import javax.validation.groups.Default;

import org.slf4j.Logger;

import org.hibernate.validation.impl.ConstraintViolationImpl;
import org.hibernate.validation.util.PropertyIterator;
import org.hibernate.validation.util.ReflectionHelper;
import org.hibernate.validation.util.LoggerFactory;

/**
 * The main Bean Validation class.
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 * @todo Make all properties transient for serializability.
 */
public class ValidatorImpl implements Validator {
	private static final Logger log = LoggerFactory.make();
	private static final Set<Class<?>> INDEXABLE_CLASS = new HashSet<Class<?>>();
	private static final Class<?>[] DEFAULT_GROUP = new Class<?>[] { Default.class };

	static {
		INDEXABLE_CLASS.add( Integer.class );
		INDEXABLE_CLASS.add( Long.class );
		INDEXABLE_CLASS.add( String.class );
	}

	/**
	 * Message resolver used  for interpolating error messages.
	 */
	private final MessageResolver messageResolver;

	private final ValidatorFactoryImplementor factory;

	private final ConstraintFactory constraintFactory;
	private final TraversableResolver traversableResolver;

	public ValidatorImpl(ValidatorFactoryImplementor factory, MessageResolver messageResolver,
						 TraversableResolver traversableResolver, ConstraintFactory constraintFactory) {
		this.factory = factory;
		this.messageResolver = messageResolver;
		this.traversableResolver = traversableResolver;
		this.constraintFactory = constraintFactory;
	}

	/**
	 * {@inheritDoc}
	 */
	public <T> Set<ConstraintViolation<T>> validate(T object, Class<?>... groups) {
		if ( object == null ) {
			throw new IllegalArgumentException( "Validation of a null object" );
		}

		ValidationContext<T> context = new ValidationContext<T>( object );
		List<ConstraintViolationImpl<T>> list = validateInContext( context, Arrays.asList( groups ) );
		return new HashSet<ConstraintViolation<T>>( list );
	}

	/**
	 * Validates the object contained in <code>context</code>.
	 *
	 * @param context A context object containing the object to validate together with other state information needed
	 * for validation.
	 * @param groups A list of groups to validate.
	 *
	 * @return List of invalid constraints.
	 *
	 * @todo Currently we iterate the cascaded fields multiple times. Maybe we should change to an approach where we iterate the object graph only once.
	 * @todo Context root bean can be a different object than the current Validator<T> hence two different generics variables
	 */
	private <T> List<ConstraintViolationImpl<T>> validateInContext(ValidationContext<T> context, List<Class<?>> groups) {
		if ( context.peekValidatedObject() == null ) {
			return Collections.emptyList();
		}

		// if no group is specified use the default
		if ( groups.size() == 0 ) {
			groups = Arrays.asList( DEFAULT_GROUP );
		}

		List<Class<?>> expandedGroups;
		boolean isGroupSequence;
		for ( Class<?> group : groups ) {
			expandedGroups = new ArrayList<Class<?>>();
			isGroupSequence = expandGroup( context.peekValidatedObjectType(), group, expandedGroups );

			for ( Class<?> expandedGroupName : expandedGroups ) {
				context.setCurrentGroup( expandedGroupName );

				validateConstraints( context );
				validateCascadedConstraints( context );

				if ( isGroupSequence && context.getFailingConstraints().size() > 0 ) {
					break;
				}
			}
		}
		return context.getFailingConstraints();
	}

	/**
	 * Validates the non-cascaded constraints.
	 *
	 * @param validationContext The current validation context.
	 */
	private <T> void validateConstraints(ValidationContext<T> validationContext) {
		//casting rely on the fact that root object is at the top of the stack
		@SuppressWarnings("unchecked")
		BeanMetaData<T> beanMetaData =
				( BeanMetaData<T> ) factory.getBeanMetaData( validationContext.peekValidatedObjectType() );
		for ( MetaConstraint metaConstraint : beanMetaData.geMetaConstraintList() ) {
			ConstraintDescriptor mainConstraintDescriptor = metaConstraint.getDescriptor();
			validationContext.pushProperty( metaConstraint.getPropertyName() );

			if ( !validationContext.needsValidation( mainConstraintDescriptor.getGroups() ) ) {
				validationContext.popProperty();
				continue;
			}

			final Object leafBeanInstance = validationContext.peekValidatedObject();
			Object value = metaConstraint.getValue( leafBeanInstance );

			// we have to check the main constraint and all composing constraints
			List<ConstraintDescriptor> descriptors = new ArrayList<ConstraintDescriptor>();
			descriptors.add( mainConstraintDescriptor );
			// use a list iterator so that we can keep modifying the underlying list
			ListIterator<ConstraintDescriptor> listIter = descriptors.listIterator();
			while ( listIter.hasNext() ) {
				ConstraintDescriptor descriptor = listIter.next();
				ConstraintContextImpl constraintContext = new ConstraintContextImpl( descriptor );
				if ( log.isTraceEnabled() ) {
					log.trace( "Validating value {} against constraint defined by {}", value, descriptor );
				}
				if ( !getConstraint( descriptor ).isValid( value, constraintContext ) ) {
					for ( ConstraintContextImpl.ErrorMessage error : constraintContext.getErrorMessages() ) {
						final String message = error.getMessage();
						String interpolatedMessage = messageResolver.interpolate(
								message,
								descriptor,
								leafBeanInstance
						);
						ConstraintViolationImpl<T> failingConstraintViolation = new ConstraintViolationImpl<T>(
								message,
								interpolatedMessage,
								validationContext.getRootBean(),
								beanMetaData.getBeanClass(),
								leafBeanInstance,
								value,
								validationContext.peekPropertyPath(), //FIXME use error.getProperty()
								validationContext.getCurrentGroup(),
								descriptor
						);
						validationContext.addConstraintFailure( failingConstraintViolation );
					}
				}
				// check for composing constraints and add them to the list. The list iterator needs to set back each time!
				for ( ConstraintDescriptor composingDescriptor : descriptor.getComposingConstraints() ) {
					listIter.add( composingDescriptor );
					listIter = descriptors.listIterator( listIter.previousIndex() );
				}
			}
			validationContext.popProperty();
		}
		validationContext.markProcessedForCurrentGroup();
	}

	/**
	 * Called when processing cascaded constraints. This methods inspects the type of the cascaded constraints and in case
	 * of a list or array creates an iterator in order to validate each element.
	 *
	 * @param context the validation context.
	 * @param type the type of the cascaded field or property.
	 * @param value the actual value.
	 */
	private <T> void validateCascadedConstraint(ValidationContext<T> context, Type type, Object value) {
		if ( value == null ) {
			return;
		}

		Iterator<?> iter;
		if ( ReflectionHelper.isCollection( type ) ) {
			boolean isIterable = value instanceof Iterable;
			Map<?, ?> map = !isIterable ? ( Map<?, ?> ) value : null;
			Iterable<?> elements = isIterable ?
					( Iterable<?> ) value :
					map.entrySet();
			iter = elements.iterator();
			context.appendIndexToPropertyPath( "[{0}]" );
		}
		else if ( ReflectionHelper.isArray( type ) ) {
			List<?> arrayList = Arrays.asList( value );
			iter = arrayList.iterator();
			context.appendIndexToPropertyPath( "[{0}]" );
		}
		else {
			List<Object> list = new ArrayList<Object>();
			list.add( value );
			iter = list.iterator();
		}

		validateCascadedConstraint( context, iter );
	}

	private <T> void validateCascadedConstraints(ValidationContext<T> context) {
		List<Member> cascadedMembers = factory.getBeanMetaData( context.peekValidatedObjectType() )
				.getCascadedMembers();
		for ( Member member : cascadedMembers ) {
			Type type = ReflectionHelper.typeOf( member );
			context.pushProperty( ReflectionHelper.getPropertyName( member ) );
			Object value = ReflectionHelper.getValue( member, context.peekValidatedObject() );
			validateCascadedConstraint( context, type, value );
			context.popProperty();
		}
	}

	private <T> void validateCascadedConstraint(ValidationContext<T> context, Iterator<?> iter) {
		Object actualValue;
		String propertyIndex;
		int i = 0;
		while ( iter.hasNext() ) {
			actualValue = iter.next();
			propertyIndex = String.valueOf( i );
			if ( actualValue instanceof Map.Entry ) {
				Object key = ( ( Map.Entry ) actualValue ).getKey();
				if ( INDEXABLE_CLASS.contains( key.getClass() ) ) {
					propertyIndex = key.toString();
				}
				actualValue = ( ( Map.Entry ) actualValue ).getValue();
			}

			if ( context.isProcessedForCurrentGroup( actualValue ) ) {
				i++;
				continue;
			}

			context.replacePropertyIndex( propertyIndex );

			context.pushValidatedObject( actualValue );
			validateInContext( context, Arrays.asList( new Class<?>[] { context.getCurrentGroup() } ) );
			context.popValidatedObject();
			i++;
		}
	}


	/**
	 * {@inheritDoc}
	 */
	public <T> Set<ConstraintViolation<T>> validateProperty(T object, String propertyName, Class<?>... groups) {
		List<ConstraintViolationImpl<T>> failingConstraintViolations = new ArrayList<ConstraintViolationImpl<T>>();
		validateProperty( object, new PropertyIterator( propertyName ), failingConstraintViolations, groups );
		return new HashSet<ConstraintViolation<T>>( failingConstraintViolations );
	}


	private <T> void validateProperty(T object, PropertyIterator propertyIter, List<ConstraintViolationImpl<T>> failingConstraintViolations, Class<?>... groups) {
		if ( object == null ) {
			throw new IllegalArgumentException( "Validated object cannot be null" );
		}
		@SuppressWarnings("unchecked")
		final Class<T> beanType = ( Class<T> ) object.getClass();

		DesrciptorValueWrapper wrapper = getConstraintDescriptorAndValueForPath( beanType, propertyIter, object );

		if ( wrapper == null ) {
			return;
		}

		// if no group is specified use the default
		if ( groups.length == 0 ) {
			groups = DEFAULT_GROUP;
		}

		List<Class<?>> expandedGroups;
		boolean isGroupSequence;
		for ( Class<?> group : groups ) {
			expandedGroups = new ArrayList<Class<?>>();
			isGroupSequence = expandGroup( beanType, group, expandedGroups );

			for ( Class<?> expandedGroup : expandedGroups ) {

				if ( !wrapper.descriptor.getGroups().contains( expandedGroup ) ) {
					continue;
				}

				ConstraintContextImpl contextImpl = new ConstraintContextImpl( wrapper.descriptor );
				if ( !getConstraint( wrapper.descriptor ).isValid( wrapper.value, contextImpl ) ) {

					for ( ConstraintContextImpl.ErrorMessage error : contextImpl.getErrorMessages() ) {
						final String message = error.getMessage();
						String interpolatedMessage = messageResolver.interpolate(
								message,
								wrapper.descriptor,
								wrapper.value
						);
						ConstraintViolationImpl<T> failingConstraintViolation = new ConstraintViolationImpl<T>(
								message,
								interpolatedMessage,
								object,
								beanType,
								object,
								wrapper.value,
								propertyIter.getOriginalProperty(), //FIXME use error.getProperty()
								group,
								wrapper.descriptor
						);
						addFailingConstraint( failingConstraintViolations, failingConstraintViolation );
					}
				}

				if ( isGroupSequence && failingConstraintViolations.size() > 0 ) {
					break;
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public <T> Set<ConstraintViolation<T>> validateValue(Class<T> beanType, String propertyName, Object value, Class<?>... groups) {
		List<ConstraintViolationImpl<T>> failingConstraintViolations = new ArrayList<ConstraintViolationImpl<T>>();
		validateValue( beanType, value, new PropertyIterator( propertyName ), failingConstraintViolations, groups );
		return new HashSet<ConstraintViolation<T>>( failingConstraintViolations );
	}

	public BeanDescriptor getConstraintsForClass(Class<?> clazz) {
		return factory.getBeanMetaData( clazz ).getBeanDescriptor();
	}

	/**
	 * @todo Implement composing constraints.
	 */
	private <T> void validateValue(Class<T> beanType, Object object, PropertyIterator propertyIter, List<ConstraintViolationImpl<T>> failingConstraintViolations, Class<?>... groups) {
		ConstraintDescriptor constraintDescriptor = getConstraintDescriptorForPath( beanType, propertyIter );

		if ( constraintDescriptor == null ) {
			return;
		}

		// if no group is specified use the default
		if ( groups.length == 0 ) {
			groups = DEFAULT_GROUP;
		}

		List<Class<?>> expandedGroups;
		boolean isGroupSequence;
		for ( Class<?> group : groups ) {
			expandedGroups = new ArrayList<Class<?>>();
			isGroupSequence = expandGroup( beanType, group, expandedGroups );

			for ( Class<?> expandedGroup : expandedGroups ) {

				if ( !constraintDescriptor.getGroups().contains( expandedGroup ) ) {
					continue;
				}

				ConstraintContextImpl contextImpl = new ConstraintContextImpl( constraintDescriptor );
				if ( !getConstraint( constraintDescriptor ).isValid( object, contextImpl ) ) {
					for ( ConstraintContextImpl.ErrorMessage error : contextImpl.getErrorMessages() ) {
						final String message = error.getMessage();
						String interpolatedMessage = messageResolver.interpolate(
								message,
								constraintDescriptor,
								object
						);
						ConstraintViolationImpl<T> failingConstraintViolation = new ConstraintViolationImpl<T>(
								message,
								interpolatedMessage,
								null,
								null,
								null,
								object,
								propertyIter.getOriginalProperty(),  //FIXME use error.getProperty()
								null,
								//FIXME why is this a null group!! Used to be "" string should it be Default. Looks weird
								constraintDescriptor
						);
						addFailingConstraint( failingConstraintViolations, failingConstraintViolation );
					}
				}

				if ( isGroupSequence && failingConstraintViolations.size() > 0 ) {
					break;
				}
			}
		}
	}

	/**
	 * Returns the constraint descriptor for the given path relative to the specified validator.
	 * <p>
	 * This method does not traverse an actual object, but rather tries to resolve the porperty generically.
	 * </p>
	 * <p>
	 * This method is called recursively. Only if there is a valid 'validation path' through the object graph
	 * a constraint descriptor will be returned.
	 * </p>
	 *
	 * @param clazz the class type to check for constraints.
	 * @param propertyIter an instance of <code>PropertyIterator</code>
	 *
	 * @return The constraint descriptor matching the given path.
	 */
	private ConstraintDescriptor getConstraintDescriptorForPath(Class<?> clazz, PropertyIterator propertyIter) {

		ConstraintDescriptor matchingConstraintDescriptor = null;
		propertyIter.split();

		if ( !propertyIter.hasNext() ) {
			List<MetaConstraint> metaConstraintList = factory.getBeanMetaData( clazz ).geMetaConstraintList();
			for ( MetaConstraint metaConstraint : metaConstraintList ) {
				ConstraintDescriptor constraintDescriptor = metaConstraint.getDescriptor();
				if ( metaConstraint.getPropertyName().equals( propertyIter.getHead() ) ) {
					matchingConstraintDescriptor = constraintDescriptor;
				}
			}
		}
		else {
			List<Member> cascadedMembers = factory.getBeanMetaData( clazz ).getCascadedMembers();
			for ( Member m : cascadedMembers ) {
				if ( ReflectionHelper.getPropertyName( m ).equals( propertyIter.getHead() ) ) {
					Type type = ReflectionHelper.typeOf( m );

					if ( propertyIter.isIndexed() ) {
						type = ReflectionHelper.getIndexedType( type );
						if ( type == null ) {
							continue;
						}
					}

					matchingConstraintDescriptor = getConstraintDescriptorForPath( ( Class<?> ) type, propertyIter );
				}
			}
		}

		return matchingConstraintDescriptor;
	}


	private DesrciptorValueWrapper getConstraintDescriptorAndValueForPath(Class<?> clazz, PropertyIterator propertyIter, Object value) {

		DesrciptorValueWrapper wrapper = null;
		propertyIter.split();


		// bottom out - there is only one token left
		if ( !propertyIter.hasNext() ) {
			List<MetaConstraint> metaConstraintList = factory.getBeanMetaData( clazz ).geMetaConstraintList();
			for ( MetaConstraint metaConstraint : metaConstraintList ) {
				ConstraintDescriptor constraintDescriptor = metaConstraint.getDescriptor();
				if ( metaConstraint.getPropertyName().equals( propertyIter.getHead() ) ) {
					return new DesrciptorValueWrapper(
							constraintDescriptor, metaConstraint.getValue( value )
					);
				}
			}
		}
		else {
			List<Member> cascadedMembers = factory.getBeanMetaData( clazz ).getCascadedMembers();
			for ( Member m : cascadedMembers ) {
				if ( ReflectionHelper.getPropertyName( m ).equals( propertyIter.getHead() ) ) {
					Object newValue;
					if ( propertyIter.isIndexed() ) {
						newValue = ReflectionHelper.getValue( m, value );
					}
					else {
						newValue = ReflectionHelper.getIndexedValue( value, propertyIter.getIndex() );
					}
					wrapper = getConstraintDescriptorAndValueForPath(
							newValue.getClass(), propertyIter, newValue
					);
				}
			}
		}

		return wrapper;
	}


	private <T> void addFailingConstraint(List<ConstraintViolationImpl<T>> failingConstraintViolations, ConstraintViolationImpl<T> failingConstraintViolation) {
		int i = failingConstraintViolations.indexOf( failingConstraintViolation );
		if ( i == -1 ) {
			failingConstraintViolations.add( failingConstraintViolation );
		}
		else {
			failingConstraintViolations.get( i ).addGroups( failingConstraintViolation.getGroups() );
		}
	}


	/**
	 * Checks whether the provided group name is a group sequence and if so expands the group name and add the expanded
	 * groups names to <code>expandedGroupName</code>.
	 *
	 * @param beanType The class for which to expand the group names.
	 * @param group The group to expand
	 * @param expandedGroups The exanded group names or just a list with the single provided group name id the name
	 * was not expandable
	 *
	 * @return <code>true</code> if an expansion took place, <code>false</code> otherwise.
	 */
	private <T> boolean expandGroup(Class<T> beanType, Class<?> group, List<Class<?>> expandedGroups) {
		if ( expandedGroups == null ) {
			throw new IllegalArgumentException( "List cannot be empty" );
		}

		boolean isGroupSequence;
		BeanMetaData<T> metaDataProvider = factory.getBeanMetaData( beanType );
		if ( metaDataProvider.getGroupSequences().containsKey( group ) ) {
			expandedGroups.addAll( metaDataProvider.getGroupSequences().get( group ) );
			isGroupSequence = true;
		}
		else {
			expandedGroups.add( group );
			isGroupSequence = false;
		}
		return isGroupSequence;
	}

	private Constraint getConstraint(ConstraintDescriptor descriptor) {
		Constraint constraint;
		try {
			//unchecked
			constraint = constraintFactory.getInstance( descriptor.getConstraintClass() );
		}
		catch ( RuntimeException e ) {
			throw new ValidationException( "Unable to instantiate " + descriptor.getConstraintClass(), e );
		}

		try {
			constraint.initialize( descriptor.getAnnotation() );
		}
		catch ( RuntimeException e ) {
			throw new ValidationException( "Unable to intialize " + constraint.getClass().getName(), e );
		}
		return constraint;
	}

	private class DesrciptorValueWrapper {
		final ConstraintDescriptor descriptor;
		final Object value;

		DesrciptorValueWrapper(ConstraintDescriptor descriptor, Object value) {
			this.descriptor = descriptor;
			this.value = value;
		}
	}
}
