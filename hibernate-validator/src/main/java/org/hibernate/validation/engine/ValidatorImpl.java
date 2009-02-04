// $Id: ValidatorImpl.java 15837 2009-01-30 15:49:57Z hardy.ferentschik $
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
import java.util.concurrent.ConcurrentHashMap;
import javax.validation.BeanDescriptor;
import javax.validation.ConstraintDescriptor;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.ConstraintViolation;
import javax.validation.MessageInterpolator;
import javax.validation.Validator;
import javax.validation.groups.Default;

import org.hibernate.validation.util.PropertyIterator;
import org.hibernate.validation.util.ReflectionHelper;

/**
 * The main Bean Validation class.
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 * @todo Make all properties transient for serializability.
 */
public class ValidatorImpl implements Validator {
	private static final Set<Class<?>> INDEXABLE_CLASS = new HashSet<Class<?>>();
	private static final Class<?>[] DEFAULT_GROUP = new Class<?>[] { Default.class };

	/**
	 * A map for the meta data for each entity. The key is the class and the value the bean meta data for this
	 * entity.
	 */
	private static Map<Class<?>, BeanMetaDataImpl<?>> metadataProviders
			= new ConcurrentHashMap<Class<?>, BeanMetaDataImpl<?>>( 10 );

	static {
		INDEXABLE_CLASS.add( Integer.class );
		INDEXABLE_CLASS.add( Long.class );
		INDEXABLE_CLASS.add( String.class );
	}

	private final ConstraintValidatorFactory constraintValidatorFactory;
	private final MessageInterpolator messageInterpolator;
	private final BuiltinConstraints builtinConstraints;

	public ValidatorImpl(ConstraintValidatorFactory constraintValidatorFactory, MessageInterpolator messageInterpolator, BuiltinConstraints builtinConstraints) {
		this.constraintValidatorFactory = constraintValidatorFactory;
		this.messageInterpolator = messageInterpolator;
		this.builtinConstraints = builtinConstraints;
	}

	/**
	 * {@inheritDoc}
	 */
	public <T> Set<ConstraintViolation<T>> validate(T object, Class<?>... groups) {
		if ( object == null ) {
			throw new IllegalArgumentException( "Validation of a null object" );
		}

		ExecutionContext<T> context = new ExecutionContext<T>(
				object, messageInterpolator, constraintValidatorFactory
		);
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
	private <T> List<ConstraintViolationImpl<T>> validateInContext(ExecutionContext<T> context, List<Class<?>> groups) {
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
	 * @param executionContext The current validation context.
	 */
	private <T> void validateConstraints(ExecutionContext<T> executionContext) {
		//casting rely on the fact that root object is at the top of the stack
		@SuppressWarnings("unchecked")
		BeanMetaData<T> beanMetaData =
				( BeanMetaData<T> ) getBeanMetaData( executionContext.peekValidatedObjectType() );
		for ( MetaConstraint metaConstraint : beanMetaData.geMetaConstraintList() ) {
			ConstraintDescriptor mainConstraintDescriptor = metaConstraint.getDescriptor();

			executionContext.pushProperty( metaConstraint.getPropertyName() );

			if ( !executionContext.needsValidation( mainConstraintDescriptor.getGroups() ) ) {
				executionContext.popProperty();
				continue;
			}

			metaConstraint.validateConstraint( beanMetaData.getBeanClass(), executionContext );
			executionContext.popProperty();
		}
		executionContext.markProcessedForCurrentGroup();
	}

	private <T> void validateCascadedConstraints(ExecutionContext<T> context) {
		List<Member> cascadedMembers = getBeanMetaData( context.peekValidatedObjectType() )
				.getCascadedMembers();
		for ( Member member : cascadedMembers ) {
			Type type = ReflectionHelper.typeOf( member );
			context.pushProperty( ReflectionHelper.getPropertyName( member ) );
			Object value = ReflectionHelper.getValue( member, context.peekValidatedObject() );
			if ( value == null ) {
				continue;
			}
			Iterator<?> iter = createIteratorForCascadedValue( context, type, value );
			validateCascadedConstraint( context, iter );
			context.popProperty();
		}
	}

	/**
	 * Called when processing cascaded constraints. This methods inspects the type of the cascaded constraints and in case
	 * of a list or array creates an iterator in order to validate each element.
	 *
	 * @param context the validation context.
	 * @param type the type of the cascaded field or property.
	 * @param value the actual value.
	 * @return An iterator over the value of a cascaded property.
	 */
	private <T> Iterator<?> createIteratorForCascadedValue(ExecutionContext<T> context, Type type, Object value) {
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
		return iter;
	}	

	private <T> void validateCascadedConstraint(ExecutionContext<T> context, Iterator<?> iter) {
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

		Set<MetaConstraint> metaConstraints = new HashSet<MetaConstraint>();
		getMetaConstraintsForPath( beanType, propertyIter, metaConstraints );

		if ( metaConstraints.size() == 0 ) {
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

				for ( MetaConstraint metaConstraint : metaConstraints ) {
					if ( !metaConstraint.getDescriptor().getGroups().contains( expandedGroup ) ) {
						continue;
					}

					ExecutionContext<T> context = new ExecutionContext<T>(
							object, messageInterpolator, constraintValidatorFactory
					);
					metaConstraint.validateConstraint( object.getClass(), context );
					failingConstraintViolations.addAll( context.getFailingConstraints() );
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
		return getBeanMetaData( clazz ).getBeanDescriptor();
	}

	private <T> void validateValue(Class<T> beanType, Object value, PropertyIterator propertyIter, List<ConstraintViolationImpl<T>> failingConstraintViolations, Class<?>... groups) {
		Set<MetaConstraint> metaConstraints = new HashSet<MetaConstraint>();
		getMetaConstraintsForPath( beanType, propertyIter, metaConstraints );

		if ( metaConstraints.size() == 0 ) {
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

				for ( MetaConstraint metaConstraint : metaConstraints ) {
					if ( !metaConstraint.getDescriptor().getGroups().contains( expandedGroup ) ) {
						continue;
					}

					ExecutionContext<T> context = new ExecutionContext<T>(
							( T ) value, messageInterpolator, constraintValidatorFactory
					);
					context.pushProperty( propertyIter.getOriginalProperty() );
					metaConstraint.validateConstraint( beanType, value, context );
					failingConstraintViolations.addAll( context.getFailingConstraints() );
				}

				if ( isGroupSequence && failingConstraintViolations.size() > 0 ) {
					break;
				}
			}
		}
	}

	/**
	 * Collects all <code>MetaConstraint</code>s which match the given path relative to the specified root class.
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
	 * @param metaConstraints Set of <code>MetaConstraint</code>s to collect all matching constraints.
	 */
	private void getMetaConstraintsForPath(Class<?> clazz, PropertyIterator propertyIter, Set<MetaConstraint> metaConstraints) {
		propertyIter.split();

		if ( !propertyIter.hasNext() ) {
			List<MetaConstraint> metaConstraintList = getBeanMetaData( clazz ).geMetaConstraintList();
			for ( MetaConstraint metaConstraint : metaConstraintList ) {
				if ( metaConstraint.getPropertyName().equals( propertyIter.getHead() ) ) {
					metaConstraints.add( metaConstraint );
				}
			}
		}
		else {
			List<Member> cascadedMembers = getBeanMetaData( clazz ).getCascadedMembers();
			for ( Member m : cascadedMembers ) {
				if ( ReflectionHelper.getPropertyName( m ).equals( propertyIter.getHead() ) ) {
					Type type = ReflectionHelper.typeOf( m );

					if ( propertyIter.isIndexed() ) {
						type = ReflectionHelper.getIndexedType( type );
						if ( type == null ) {
							continue;
						}
					}
					getMetaConstraintsForPath( ( Class<?> ) type, propertyIter, metaConstraints );
				}
			}
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
		BeanMetaData<T> metaDataProvider = getBeanMetaData( beanType );
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

	/**
	 * {@inheritDoc}
	 */
	private <T> BeanMetaDataImpl<T> getBeanMetaData(Class<T> beanClass) {
		if ( beanClass == null ) {
			throw new IllegalArgumentException( "Class cannot be null" );
		}
		@SuppressWarnings("unchecked")
		BeanMetaDataImpl<T> metadata = ( BeanMetaDataImpl<T> ) metadataProviders.get( beanClass );
		if ( metadata == null ) {
			metadata = new BeanMetaDataImpl<T>( beanClass, builtinConstraints );
			metadataProviders.put( beanClass, metadata );
		}
		return metadata;
	}
}
