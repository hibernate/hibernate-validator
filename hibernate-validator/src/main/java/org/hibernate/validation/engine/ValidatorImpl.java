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

import java.lang.annotation.Annotation;
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
import javax.validation.ConstraintValidatorFactory;
import javax.validation.ConstraintViolation;
import javax.validation.MessageInterpolator;
import javax.validation.TraversableResolver;
import javax.validation.ValidationException;
import javax.validation.Validator;
import javax.validation.groups.Default;
import javax.validation.metadata.BeanDescriptor;

import com.googlecode.jtype.TypeUtils;
import org.slf4j.Logger;

import org.hibernate.validation.engine.groups.Group;
import org.hibernate.validation.engine.groups.GroupChain;
import org.hibernate.validation.engine.groups.GroupChainGenerator;
import org.hibernate.validation.engine.resolver.SingleThreadCachedTraversableResolver;
import org.hibernate.validation.metadata.BeanMetaData;
import org.hibernate.validation.metadata.BeanMetaDataCache;
import org.hibernate.validation.metadata.BeanMetaDataImpl;
import org.hibernate.validation.metadata.ConstraintHelper;
import org.hibernate.validation.metadata.MetaConstraint;
import org.hibernate.validation.util.LoggerFactory;
import org.hibernate.validation.util.PropertyPath;
import org.hibernate.validation.util.ReflectionHelper;

/**
 * The main Bean Validation class. This is the core processing class of Hibernate Validator.
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 * @todo Make all properties transient for serializability.
 */
public class ValidatorImpl implements Validator {
	private static final Logger log = LoggerFactory.make();

	/**
	 * Set of classes which can be used as index in a map.
	 */
	private static final Set<Class<?>> VALID_MAP_INDEX_CLASSES = new HashSet<Class<?>>();

	static {
		VALID_MAP_INDEX_CLASSES.add( Integer.class );
		VALID_MAP_INDEX_CLASSES.add( Long.class );
		VALID_MAP_INDEX_CLASSES.add( String.class );
	}

	/**
	 * The default group array used in case any of the validate methods is called without a group.
	 */
	private static final Class<?>[] DEFAULT_GROUP_ARRAY = new Class<?>[] { Default.class };

	/**
	 * Used to resolve the group execution order for a validate call.
	 */
	private GroupChainGenerator groupChainGenerator;

	private final ConstraintValidatorFactory constraintValidatorFactory;
	private final MessageInterpolator messageInterpolator;
	//never use it directly, always use getCachingTraversableResolver() to retrieved the single threaded caching wrapper.
	private final TraversableResolver traversableResolver;
	private final ConstraintHelper constraintHelper;
	private final BeanMetaDataCache beanMetaDataCache;


	public ValidatorImpl(ConstraintValidatorFactory constraintValidatorFactory, MessageInterpolator messageInterpolator, TraversableResolver traversableResolver, ConstraintHelper constraintHelper, BeanMetaDataCache beanMetaDataCache) {
		this.constraintValidatorFactory = constraintValidatorFactory;
		this.messageInterpolator = messageInterpolator;
		this.traversableResolver = traversableResolver;
		this.constraintHelper = constraintHelper;
		this.beanMetaDataCache = beanMetaDataCache;

		groupChainGenerator = new GroupChainGenerator();
	}

	/**
	 * {@inheritDoc}
	 */
	public <T> Set<ConstraintViolation<T>> validate(T object, Class<?>... groups) {
		if ( object == null ) {
			throw new IllegalArgumentException( "Validation of a null object" );
		}
		GroupChain groupChain = determineGroupExecutionOrder( groups );

		ExecutionContext<T> context = ExecutionContext.getContextForValidate(
				object, messageInterpolator, constraintValidatorFactory, getCachingTraversableResolver()
		);

		List<ConstraintViolation<T>> list = validateInContext( context, groupChain );
		return new HashSet<ConstraintViolation<T>>( list );
	}

	/**
	 * {@inheritDoc}
	 */
	public <T> Set<ConstraintViolation<T>> validateProperty(T object, String propertyName, Class<?>... groups) {
		if ( object == null ) {
			throw new IllegalArgumentException( "Validated object cannot be null." );
		}
		sanityCheckPropertyPath( propertyName );
		GroupChain groupChain = determineGroupExecutionOrder( groups );

		List<ConstraintViolation<T>> failingConstraintViolations = new ArrayList<ConstraintViolation<T>>();
		validateProperty( object, new PropertyPath( propertyName ), failingConstraintViolations, groupChain );
		return new HashSet<ConstraintViolation<T>>( failingConstraintViolations );
	}

	/**
	 * {@inheritDoc}
	 */
	public <T> Set<ConstraintViolation<T>> validateValue(Class<T> beanType, String propertyName, Object value, Class<?>... groups) {
		if ( beanType == null ) {
			throw new IllegalArgumentException( "The bean type cannot be null." );
		}
		sanityCheckPropertyPath( propertyName );
		GroupChain groupChain = determineGroupExecutionOrder( groups );

		List<ConstraintViolation<T>> failingConstraintViolations = new ArrayList<ConstraintViolation<T>>();
		validateValue( beanType, value, new PropertyPath( propertyName ), failingConstraintViolations, groupChain );
		return new HashSet<ConstraintViolation<T>>( failingConstraintViolations );
	}

	/**
	 * {@inheritDoc}
	 */
	public BeanDescriptor getConstraintsForClass(Class<?> clazz) {
		return getBeanMetaData( clazz ).getBeanDescriptor();
	}

	public <T> T unwrap(Class<T> type) {
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

		// if no groups is specified use the default
		if ( groups.length == 0 ) {
			groups = DEFAULT_GROUP_ARRAY;
		}

		return groupChainGenerator.getGroupChainFor( Arrays.asList( groups ) );
	}

	/**
	 * Validates the object contained in <code>context</code>.
	 *
	 * @param context A context object containing the object to validate together with other state information needed
	 * for validation.
	 * @param groupChain A <code>GroupChain</code> instance containing the resolved group sequence to execute
	 *
	 * @return List of invalid constraints.
	 */
	private <T> List<ConstraintViolation<T>> validateInContext(ExecutionContext<T> context, GroupChain groupChain) {
		if ( context.peekCurrentBean() == null ) {
			return Collections.emptyList();
		}

		//FIXME if context not accessible do not call isTraversable??

		// process all groups breadth-first
		Iterator<Group> groupIterator = groupChain.getGroupIterator();
		while ( groupIterator.hasNext() ) {
			Group group = groupIterator.next();
			context.setCurrentGroup( group.getGroup() );
			validateConstraints( context );
		}
		groupIterator = groupChain.getGroupIterator();
		while ( groupIterator.hasNext() ) {
			Group group = groupIterator.next();
			context.setCurrentGroup( group.getGroup() );
			validateCascadedConstraints( context );
		}

		// process group sequences depth-first to guarantee that groups following a violation within a group won't get executed.
		Iterator<List<Group>> sequenceIterator = groupChain.getSequenceIterator();
		while ( sequenceIterator.hasNext() ) {
			List<Group> sequence = sequenceIterator.next();
			for ( Group group : sequence ) {
				int numberOfViolations = context.getFailingConstraints().size();
				context.setCurrentGroup( group.getGroup() );

				validateConstraints( context );
				validateCascadedConstraints( context );

				if ( context.getFailingConstraints().size() > numberOfViolations ) {
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
		@SuppressWarnings(" unchecked")
		BeanMetaData<T> beanMetaData = getBeanMetaData( ( Class<T> ) executionContext.peekCurrentBeanType() );
		if ( executionContext.getCurrentGroup().getName().equals( Default.class.getName() ) ) {
			List<Class<?>> defaultGroupSequence = beanMetaData.getDefaultGroupSequence();
			if ( log.isTraceEnabled() && defaultGroupSequence.size() > 0 && defaultGroupSequence.get( 0 ) != Default.class ) {
				log.trace(
						"Executing re-defined Default group for bean {} as sequence {}",
						beanMetaData.getBeanClass().getName(),
						defaultGroupSequence
				);
			}
			for ( Class<?> defaultSequenceMember : defaultGroupSequence ) {
				executionContext.setCurrentGroup( defaultSequenceMember );
				boolean validationSuccessful = validateConstraintsForCurrentGroup( executionContext, beanMetaData );
				if ( !validationSuccessful ) {
					break;
				}
			}
		}
		else {
			validateConstraintsForCurrentGroup( executionContext, beanMetaData );
		}
	}

	/**
	 * Validates all constraints for the given bean using the current group set in the execution context.
	 *
	 * @param executionContext The execution context.
	 * @param beanMetaData The bean metadata object for the bean to validate.
	 *
	 * @return <code>true</code> if the validation was successful (meaning no constraint violations), <code>false</code>
	 *         otherwise.
	 */
	private <T> boolean validateConstraintsForCurrentGroup(ExecutionContext<T> executionContext, BeanMetaData<T> beanMetaData) {
		boolean validationSuccessful = true;
		for ( MetaConstraint<T, ?> metaConstraint : beanMetaData.geMetaConstraintList() ) {
			executionContext.pushProperty( metaConstraint.getPropertyName() );
			if ( executionContext.isValidationRequired( metaConstraint ) ) {
				boolean tmp = metaConstraint.validateConstraint( executionContext );
				validationSuccessful = validationSuccessful && tmp;
			}
			executionContext.popProperty();
		}
		return validationSuccessful;
	}

	//this method must always be called after validateConstraints for the same context
	//TODO define a validate that calls  validateConstraints and then validateCascadedConstraints
	private <T> void validateCascadedConstraints(ExecutionContext<T> context) {
		List<Member> cascadedMembers = getBeanMetaData( context.peekCurrentBeanType() )
				.getCascadedMembers();
		for ( Member member : cascadedMembers ) {
			Type type = ReflectionHelper.typeOf( member );
			context.pushProperty( ReflectionHelper.getPropertyName( member ) );
			if ( context.isCascadeRequired( member ) ) {
				Object value = ReflectionHelper.getValue( member, context.peekCurrentBean() );
				if ( value != null ) {
					Iterator<?> iter = createIteratorForCascadedValue( context, type, value );
					boolean isIndexable = isIndexable( type );
					validateCascadedConstraint( context, iter, isIndexable );
				}
			}
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
	 *
	 * @return An iterator over the value of a cascaded property.
	 */
	private <T> Iterator<?> createIteratorForCascadedValue(ExecutionContext<T> context, Type type, Object value) {
		Iterator<?> iter;
		if ( ReflectionHelper.isIterable( type ) ) {
			iter = ( ( Iterable<?> ) value ).iterator();
			context.markCurrentPropertyAsIterable();
		}
		else if ( ReflectionHelper.isMap( type ) ) {
			Map<?, ?> map = ( Map<?, ?> ) value;
			iter = map.values().iterator();
			context.markCurrentPropertyAsIterable();
		}
		else if ( TypeUtils.isArray( type ) ) {
			List<?> arrayList = Arrays.asList( value );
			iter = arrayList.iterator();
			context.markCurrentPropertyAsIterable();
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

	@SuppressWarnings("RedundantArrayCreation")
	private <T> void validateCascadedConstraint(ExecutionContext<T> context, Iterator<?> iter, boolean isIndexable) {
		Object actualValue;
		String propertyIndex;
		int i = 0;
		while ( iter.hasNext() ) {
			actualValue = iter.next();
			propertyIndex = String.valueOf( i );
			if ( actualValue instanceof Map.Entry ) {
				Object key = ( ( Map.Entry ) actualValue ).getKey();
				if ( VALID_MAP_INDEX_CLASSES.contains( key.getClass() ) ) {
					propertyIndex = key.toString();
				}
				actualValue = ( ( Map.Entry ) actualValue ).getValue();
			}

			if ( !context.isAlreadyValidated( actualValue ) ) {
				if ( isIndexable ) {
					context.setPropertyIndex( propertyIndex );
				}
				context.pushCurrentBean( actualValue );
				GroupChain groupChain = groupChainGenerator.getGroupChainFor( Arrays.asList( new Class<?>[] { context.getCurrentGroup() } ) );
				validateInContext( context, groupChain );
				context.popCurrentBean();
			}
			i++;
		}
	}

	private <T> void validateProperty(T object, PropertyPath propertyPath, List<ConstraintViolation<T>> failingConstraintViolations, GroupChain groupChain) {

		@SuppressWarnings("unchecked")
		final Class<T> beanType = ( Class<T> ) object.getClass();

		Set<MetaConstraint<T, ?>> metaConstraints = new HashSet<MetaConstraint<T, ?>>();
		Object hostingBeanInstance = collectMetaConstraintsForPath(
				beanType, object, propertyPath.iterator(), metaConstraints
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

	private <T> void validatePropertyForGroup(
			T object,
			PropertyPath propertyIter,
			List<ConstraintViolation<T>> failingConstraintViolations,
			Set<MetaConstraint<T, ?>> metaConstraints,
			Object hostingBeanInstance,
			Group group,
			TraversableResolver cachedTraversableResolver) {
		int numberOfConstraintViolationsBefore = failingConstraintViolations.size();
		BeanMetaData<T> beanMetaData = getBeanMetaData( metaConstraints.iterator().next().getBeanClass() );

		List<Class<?>> groupList;
		if ( group.isDefaultGroup() ) {
			groupList = beanMetaData.getDefaultGroupSequence();
		}
		else {
			groupList = new ArrayList<Class<?>>();
			groupList.add( group.getGroup() );
		}

		for ( Class<?> groupClass : groupList ) {
			for ( MetaConstraint<T, ?> metaConstraint : metaConstraints ) {
				ExecutionContext<T> context = ExecutionContext.getContextForValidateProperty(
						object,
						hostingBeanInstance,
						messageInterpolator,
						constraintValidatorFactory,
						cachedTraversableResolver
				);
				context.pushProperty( propertyIter.getOriginalProperty() );
				context.setCurrentGroup( groupClass );
				if ( context.isValidationRequired( metaConstraint ) ) {
					metaConstraint.validateConstraint( context );
					failingConstraintViolations.addAll( context.getFailingConstraints() );
				}
				context.popProperty();
			}
			if ( failingConstraintViolations.size() > numberOfConstraintViolationsBefore ) {
				break;
			}
		}
	}

	@SuppressWarnings("unchecked")
	private <T> void validateValue(Class<T> beanType, Object value, PropertyPath propertyPath, List<ConstraintViolation<T>> failingConstraintViolations, GroupChain groupChain) {
		Set<MetaConstraint<T, ?>> metaConstraints = new HashSet<MetaConstraint<T, ?>>();
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

		// process squences
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

	private <T> void validateValueForGroup(
			Class<T> beanType,
			Object value,
			PropertyPath propertyIter,
			List<ConstraintViolation<T>> failingConstraintViolations,
			Set<MetaConstraint<T, ?>> metaConstraints,
			Group group,
			TraversableResolver cachedTraversableResolver) {
		int numberOfConstraintViolations = failingConstraintViolations.size();
		BeanMetaData<T> beanMetaData = getBeanMetaData( metaConstraints.iterator().next().getBeanClass() );

		List<Class<?>> groupList;
		if ( group.isDefaultGroup() ) {
			groupList = beanMetaData.getDefaultGroupSequence();
		}
		else {
			groupList = new ArrayList<Class<?>>();
			groupList.add( group.getGroup() );
		}


		for ( Class<?> groupClass : groupList ) {
			for ( MetaConstraint<T, ?> metaConstraint : metaConstraints ) {
				ExecutionContext<T> context = ExecutionContext.getContextForValidateValue(
						beanType, value, messageInterpolator, constraintValidatorFactory, cachedTraversableResolver
				);
				context.pushProperty( propertyIter.getOriginalProperty() );
				context.setCurrentGroup( groupClass );
				if ( context.isValidationRequired( metaConstraint ) ) {
					metaConstraint.validateConstraint( value, context );
					failingConstraintViolations.addAll( context.getFailingConstraints() );
				}
				context.popProperty();
			}
			if ( failingConstraintViolations.size() > numberOfConstraintViolations ) {
				break;
			}
		}
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
	@SuppressWarnings("unchecked")
	private <T> Object collectMetaConstraintsForPath(Class<T> clazz, Object value, Iterator<PropertyPath.PathElement> propertyIter, Set<MetaConstraint<T, ?>> metaConstraints) {

		PropertyPath.PathElement elem = propertyIter.next();
		final BeanMetaData<T> metaData = getBeanMetaData( clazz );
		if ( !propertyIter.hasNext() ) {

			//use metadata first as ReflectionHelper#containsMember is slow
			//TODO store some metadata here?
			if ( metaData.getPropertyDescriptor( elem.value() ) == null
					&& !ReflectionHelper.containsMember( clazz, elem.value() ) ) {
				//TODO better error report
				throw new IllegalArgumentException( "Invalid property path." );
			}

			List<MetaConstraint<T, ? extends Annotation>> metaConstraintList = metaData.geMetaConstraintList();
			for ( MetaConstraint<T, ?> metaConstraint : metaConstraintList ) {
				if ( metaConstraint.getPropertyName().equals( elem.value() ) ) {
					metaConstraints.add( metaConstraint );
				}
			}
		}
		else {
			List<Member> cascadedMembers = metaData.getCascadedMembers();
			for ( Member m : cascadedMembers ) {
				if ( ReflectionHelper.getPropertyName( m ).equals( elem.value() ) ) {
					Type type = ReflectionHelper.typeOf( m );
					value = value == null ? null : ReflectionHelper.getValue( m, value );
					if ( elem.isIndexed() ) {
						type = ReflectionHelper.getIndexedType( type );
						value = value == null ? null : ReflectionHelper.getIndexedValue(
								value, elem.getIndex()
						);
						if ( type == null ) {
							continue;
						}
					}
					return collectMetaConstraintsForPath( ( Class<T> ) type, value, propertyIter, metaConstraints );
				}
			}
		}
		return value;
	}

	/**
	 * {@inheritDoc}
	 */
	private <T> BeanMetaData<T> getBeanMetaData(Class<T> beanClass) {
		BeanMetaDataImpl<T> metadata = beanMetaDataCache.getBeanMetaData( beanClass );
		if ( metadata == null ) {
			metadata = new BeanMetaDataImpl<T>( beanClass, constraintHelper );
			beanMetaDataCache.addBeanMetaData( beanClass, metadata );
		}
		return metadata;
	}

	/**
	 * Must be called and stored for the duration of the stack call
	 * A new instance is returned each time
	 */
	private TraversableResolver getCachingTraversableResolver() {
		return new SingleThreadCachedTraversableResolver( traversableResolver );
	}
}
