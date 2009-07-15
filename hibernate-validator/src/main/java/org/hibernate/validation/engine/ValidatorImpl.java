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
import java.lang.annotation.ElementType;
import java.lang.reflect.Field;
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
import javax.validation.Path;
import javax.validation.TraversableResolver;
import javax.validation.ValidationException;
import javax.validation.Validator;
import javax.validation.groups.Default;
import javax.validation.metadata.BeanDescriptor;

import com.googlecode.jtype.TypeUtils;

import org.hibernate.validation.engine.groups.Group;
import org.hibernate.validation.engine.groups.GroupChain;
import org.hibernate.validation.engine.groups.GroupChainGenerator;
import org.hibernate.validation.engine.resolver.SingleThreadCachedTraversableResolver;
import org.hibernate.validation.metadata.BeanMetaData;
import org.hibernate.validation.metadata.BeanMetaDataCache;
import org.hibernate.validation.metadata.BeanMetaDataImpl;
import org.hibernate.validation.metadata.ConstraintHelper;
import org.hibernate.validation.metadata.MetaConstraint;
import org.hibernate.validation.util.ReflectionHelper;

/**
 * The main Bean Validation class. This is the core processing class of Hibernate Validator.
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 */
public class ValidatorImpl implements Validator {

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

	public <T> Set<ConstraintViolation<T>> validate(T object, Class<?>... groups) {
		if ( object == null ) {
			throw new IllegalArgumentException( "Validation of a null object" );
		}

		GroupChain groupChain = determineGroupExecutionOrder( groups );

		GlobalExecutionContext<T> context = GlobalExecutionContext.getContextForValidate(
				object, messageInterpolator, constraintValidatorFactory, getCachingTraversableResolver()
		);

		List<ConstraintViolation<T>> list = validateInContext(
				object, context, groupChain, PathImpl.createNewRootPath()
		);
		return new HashSet<ConstraintViolation<T>>( list );
	}

	public <T> Set<ConstraintViolation<T>> validateProperty(T object, String propertyName, Class<?>... groups) {
		if ( object == null ) {
			throw new IllegalArgumentException( "Validated object cannot be null." );
		}
		sanityCheckPropertyPath( propertyName );
		GroupChain groupChain = determineGroupExecutionOrder( groups );

		List<ConstraintViolation<T>> failingConstraintViolations = new ArrayList<ConstraintViolation<T>>();
		validateProperty(
				object, PathImpl.createPathFromString( propertyName ), failingConstraintViolations, groupChain
		);
		return new HashSet<ConstraintViolation<T>>( failingConstraintViolations );
	}

	public <T> Set<ConstraintViolation<T>> validateValue(Class<T> beanType, String propertyName, Object value, Class<?>... groups) {
		if ( beanType == null ) {
			throw new IllegalArgumentException( "The bean type cannot be null." );
		}

		sanityCheckPropertyPath( propertyName );
		GroupChain groupChain = determineGroupExecutionOrder( groups );

		List<ConstraintViolation<T>> failingConstraintViolations = new ArrayList<ConstraintViolation<T>>();
		validateValue(
				beanType, value, PathImpl.createPathFromString( propertyName ), failingConstraintViolations, groupChain
		);
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
	 * Validates the given object using the available context information.
	 *
	 * @param value The value to validate.
	 * @param context Global context. Used amongst others to collect all failing constraints.
	 * @param groupChain Contains the information which and in which oder groups have to be executed
	 * @param path The current path of the valiation.
	 * @param <T> The root bean type.
	 * @param <V> The type of the current object on the validation stack.
	 *
	 * @return List of constraint violations or the empty set if there were no violations.
	 */
	private <T, U, V> List<ConstraintViolation<T>> validateInContext(U value, GlobalExecutionContext<T> context, GroupChain groupChain, PathImpl path) {
		if ( value == null ) {
			return Collections.emptyList();
		}

		path = PathImpl.createShallowCopy( path );
		LocalExecutionContext<U, V> localExecutionContext = LocalExecutionContext.getLocalExecutionContext( value );

		BeanMetaData<U> beanMetaData = getBeanMetaData( localExecutionContext.getCurrentBeanType() );
		if ( beanMetaData.defaultGroupSequenceIsRedefined() ) {
			groupChain.assertDefaulGroupSequenceIsExpandable( beanMetaData.getDefaultGroupSequence() );
		}

		// process first single groups. For these we can skip some object traversal, by first running all validations on the current bean
		// before traversing the object.
		Iterator<Group> groupIterator = groupChain.getGroupIterator();
		while ( groupIterator.hasNext() ) {
			Group group = groupIterator.next();
			localExecutionContext.setCurrentGroup( group.getGroup() );
			validateConstraintsForCurrentGroup( context, localExecutionContext, path );
		}
		groupIterator = groupChain.getGroupIterator();
		while ( groupIterator.hasNext() ) {
			Group group = groupIterator.next();
			localExecutionContext.setCurrentGroup( group.getGroup() );
			validateCascadedConstraints( context, localExecutionContext, path );
		}

		// now we process sequences. For sequences I have to traverse the object graph since I have to stop processing when an error occurs.
		Iterator<List<Group>> sequenceIterator = groupChain.getSequenceIterator();
		while ( sequenceIterator.hasNext() ) {
			List<Group> sequence = sequenceIterator.next();
			for ( Group group : sequence ) {
				int numberOfViolations = context.getFailingConstraints().size();
				localExecutionContext.setCurrentGroup( group.getGroup() );

				validateConstraintsForCurrentGroup( context, localExecutionContext, path );
				validateCascadedConstraints( context, localExecutionContext, path );

				if ( context.getFailingConstraints().size() > numberOfViolations ) {
					break;
				}
			}
		}
		return context.getFailingConstraints();
	}

	private <T, U, V> void validateConstraintsForCurrentGroup(GlobalExecutionContext<T> globalExecutionContext, LocalExecutionContext<U, V> localExecutionContext, PathImpl path) {
		BeanMetaData<U> beanMetaData = getBeanMetaData( localExecutionContext.getCurrentBeanType() );
		boolean validatingDefault = localExecutionContext.validatingDefault();
		boolean validatedBeanRedefinesDefault = beanMetaData.defaultGroupSequenceIsRedefined();

		// if we are not validating the default group there is nothing slecial to consider
		if ( !validatingDefault ) {
			validateConstraintsForNonDefaultGroup( globalExecutionContext, localExecutionContext, path );
			return;
		}

		// if we are validating the default group we have to distinguish between the case where the main entity type redefines the default group and
		// where not
		if ( validatedBeanRedefinesDefault ) {
			validateConstraintsForRedefinedDefaultGroupOnMainEntity(
					globalExecutionContext, localExecutionContext, path, beanMetaData
			);
		}
		else {
			validateConstraintsForRedefinedDefaultGroup(
					globalExecutionContext, localExecutionContext, path, beanMetaData
			);
		}
	}

	private <T, U, V> void validateConstraintsForRedefinedDefaultGroup(GlobalExecutionContext<T> globalExecutionContext, LocalExecutionContext<U, V> localExecutionContext, PathImpl path, BeanMetaData<U> beanMetaData) {
		// in the case where the main entity does not redefine the default group we have to check whether the entity which defines the constraint does
		for ( Map.Entry<Class<?>, List<MetaConstraint<U, ? extends Annotation>>> entry : beanMetaData.geMetaConstraintsAsMap()
				.entrySet() ) {
			Class<?> hostingBeanClass = entry.getKey();
			List<MetaConstraint<U, ? extends Annotation>> constraints = entry.getValue();

			List<Class<?>> defaultGroupSequence = getBeanMetaData( hostingBeanClass ).getDefaultGroupSequence();
			for ( Class<?> defaultSequenceMember : defaultGroupSequence ) {
				localExecutionContext.setCurrentGroup( defaultSequenceMember );
				boolean validationSuccessful = true;
				for ( MetaConstraint<U, ? extends Annotation> metaConstraint : constraints ) {
					boolean tmp = validateConstraint(
							globalExecutionContext, localExecutionContext, metaConstraint, path
					);
					validationSuccessful = validationSuccessful && tmp;
				}
				if ( !validationSuccessful ) {
					break;
				}
			}
		}
	}

	private <T, U, V> void validateConstraintsForRedefinedDefaultGroupOnMainEntity(GlobalExecutionContext<T> globalExecutionContext, LocalExecutionContext<U, V> localExecutionContext, PathImpl path, BeanMetaData<U> beanMetaData) {
		// in the case where the main entity redefines the default group we can interate over all constraints independend of the bean they are
		// defined in. The redefined group sequence applies for all constraints.
		List<Class<?>> defaultGroupSequence = beanMetaData.getDefaultGroupSequence();
		for ( Class<?> defaultSequenceMember : defaultGroupSequence ) {
			localExecutionContext.setCurrentGroup( defaultSequenceMember );
			boolean validationSuccessful = true;
			for ( MetaConstraint<U, ? extends Annotation> metaConstraint : beanMetaData.geMetaConstraintsAsList() ) {
				boolean tmp = validateConstraint(
						globalExecutionContext, localExecutionContext, metaConstraint, path
				);
				validationSuccessful = validationSuccessful && tmp;
			}
			if ( !validationSuccessful ) {
				break;
			}
		}
	}

	private <T, U, V> void validateConstraintsForNonDefaultGroup(GlobalExecutionContext<T> globalExecutionContext, LocalExecutionContext<U, V> localExecutionContext, PathImpl path) {
		BeanMetaData<U> beanMetaData = getBeanMetaData( localExecutionContext.getCurrentBeanType() );
		for ( MetaConstraint<U, ? extends Annotation> metaConstraint : beanMetaData.geMetaConstraintsAsList() ) {
			validateConstraint( globalExecutionContext, localExecutionContext, metaConstraint, path );
		}
	}

	private <T, U, V> boolean validateConstraint(GlobalExecutionContext<T> globalExecutionContext, LocalExecutionContext<U, V> localExecutionContext, MetaConstraint<U, ?> metaConstraint, PathImpl path) {
		boolean validationSuccessful = true;
		PathImpl newPath = PathImpl.createShallowCopy( path );
		if ( !"".equals( metaConstraint.getPropertyName() ) ) {
			newPath.addNode( new NodeImpl( metaConstraint.getPropertyName() ) );
		}
		localExecutionContext.setPropertyPath( newPath );
		if ( isValidationRequired( globalExecutionContext, localExecutionContext, metaConstraint ) ) {
			Object valueToValidate = metaConstraint.getValue( localExecutionContext.getCurrentBean() );
			localExecutionContext.setCurrentValidatedValue( ( V ) valueToValidate );
			validationSuccessful = metaConstraint.validateConstraint( globalExecutionContext, localExecutionContext );
		}
		globalExecutionContext.markProcessed(
				localExecutionContext.getCurrentBean(),
				localExecutionContext.getCurrentGroup(),
				localExecutionContext.getPropertyPath()
		);

		return validationSuccessful;
	}

	/**
	 * Validates all cascaded constraints for the given bean using the current group set in the execution context.
	 * This method must always be called after validateConstraints for the same context.
	 *
	 * @param globalExecutionContext The execution context
	 * @param localExecutionContext Collected information for single validation
	 * @param path The current path of the valiation.
	 */
	private <T, U, V> void validateCascadedConstraints(GlobalExecutionContext<T> globalExecutionContext, LocalExecutionContext<U, V> localExecutionContext, PathImpl path) {
		List<Member> cascadedMembers = getBeanMetaData( localExecutionContext.getCurrentBeanType() )
				.getCascadedMembers();
		for ( Member member : cascadedMembers ) {
			Type type = ReflectionHelper.typeOf( member );
			PathImpl newPath = PathImpl.createShallowCopy( path );
			newPath.addNode( new NodeImpl( ReflectionHelper.getPropertyName( member ) ) );
			localExecutionContext.setPropertyPath( newPath );
			if ( isCascadeRequired( globalExecutionContext, localExecutionContext, member ) ) {
				Object value = ReflectionHelper.getValue( member, localExecutionContext.getCurrentBean() );
				if ( value != null ) {
					Iterator<?> iter = createIteratorForCascadedValue( localExecutionContext, type, value );
					boolean isIndexable = isIndexable( type );
					validateCascadedConstraint(
							globalExecutionContext,
							iter,
							isIndexable,
							localExecutionContext.getCurrentGroup(),
							localExecutionContext.getPropertyPath()
					);
				}
			}
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
	private <U, V> Iterator<?> createIteratorForCascadedValue(LocalExecutionContext<U, V> context, Type type, Object value) {
		Iterator<?> iter;
		if ( ReflectionHelper.isIterable( type ) ) {
			iter = ( ( Iterable<?> ) value ).iterator();
			context.markCurrentPropertyAsIterable();
		}
		else if ( ReflectionHelper.isMap( type ) ) {
			Map<?, ?> map = ( Map<?, ?> ) value;
			iter = map.entrySet().iterator();
			context.markCurrentPropertyAsIterable();
		}
		else if ( TypeUtils.isArray( type ) ) {
			List<?> arrayList = Arrays.asList( ( Object[] ) value );
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
	private <T> void validateCascadedConstraint(GlobalExecutionContext<T> context, Iterator<?> iter, boolean isIndexable, Class<?> currentGroup, PathImpl currentPath) {
		Object value;
		Integer index;
		Object mapKey = null;
		int i = 0;
		while ( iter.hasNext() ) {
			value = iter.next();
			index = i;
			if ( value instanceof Map.Entry ) {
				mapKey = ( ( Map.Entry ) value ).getKey();
				value = ( ( Map.Entry ) value ).getValue();
				currentPath.getLeafNode().setKey( mapKey );
			}
			else if ( isIndexable ) {
				currentPath.getLeafNode().setIndex( index );
			}

			if ( !context.isAlreadyValidated( value, currentGroup, currentPath ) ) {
				GroupChain groupChain = groupChainGenerator.getGroupChainFor(
						Arrays.asList(
								new Class<?>[] {
										currentGroup
								}
						)
				);
				validateInContext( value, context, groupChain, currentPath );
			}
			i++;
		}
	}

	private <T> void validateProperty(T object, PathImpl propertyPath, List<ConstraintViolation<T>> failingConstraintViolations, GroupChain groupChain) {

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

	private <T, U, V> void validatePropertyForGroup(
			T object,
			PathImpl path,
			List<ConstraintViolation<T>> failingConstraintViolations,
			Set<MetaConstraint<T, ?>> metaConstraints,
			U hostingBeanInstance,
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
				GlobalExecutionContext<T> context = GlobalExecutionContext.getContextForValidateProperty(
						object,
						messageInterpolator,
						constraintValidatorFactory,
						cachedTraversableResolver
				);
				LocalExecutionContext<U, V> localContext = LocalExecutionContext.getLocalExecutionContext(
						hostingBeanInstance
				);
				localContext.setPropertyPath( path );
				localContext.setCurrentGroup( groupClass );
				if ( isValidationRequired( context, localContext, metaConstraint ) ) {
					Object valueToValidate = metaConstraint.getValue( localContext.getCurrentBean() );
					localContext.setCurrentValidatedValue( ( V ) valueToValidate );
					metaConstraint.validateConstraint( context, localContext );
					failingConstraintViolations.addAll( context.getFailingConstraints() );
				}
			}
			if ( failingConstraintViolations.size() > numberOfConstraintViolationsBefore ) {
				break;
			}
		}
	}

	@SuppressWarnings("unchecked")
	private <T> void validateValue(Class<T> beanType, Object value, PathImpl propertyPath, List<ConstraintViolation<T>> failingConstraintViolations, GroupChain groupChain) {
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

	private <U, V> void validateValueForGroup(
			Class<U> beanType,
			V value,
			PathImpl path,
			List<ConstraintViolation<U>> failingConstraintViolations,
			Set<MetaConstraint<U, ?>> metaConstraints,
			Group group,
			TraversableResolver cachedTraversableResolver) {
		int numberOfConstraintViolations = failingConstraintViolations.size();
		BeanMetaData<U> beanMetaData = getBeanMetaData( metaConstraints.iterator().next().getBeanClass() );

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
				GlobalExecutionContext<U> context = GlobalExecutionContext.getContextForValidateValue(
						beanType, messageInterpolator, constraintValidatorFactory, cachedTraversableResolver
				);
				LocalExecutionContext<U, V> localContext = LocalExecutionContext.getLocalExecutionContext( beanType );
				localContext.setPropertyPath( path );
				localContext.setCurrentGroup( groupClass );
				localContext.setCurrentValidatedValue( value );
				if ( isValidationRequired( context, localContext, metaConstraint ) ) {
					metaConstraint.validateConstraint( context, localContext );
					failingConstraintViolations.addAll( context.getFailingConstraints() );
				}
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
	private <T> Object collectMetaConstraintsForPath(Class<T> clazz, Object value, Iterator<Path.Node> propertyIter, Set<MetaConstraint<T, ?>> metaConstraints) {
		Path.Node elem = propertyIter.next();
		if ( elem.getName() == null ) { // skip root node
			elem = propertyIter.next();
		}

		final BeanMetaData<T> metaData = getBeanMetaData( clazz );
		if ( !propertyIter.hasNext() ) {

			//use metadata first as ReflectionHelper#containsMember is slow
			if ( metaData.getPropertyDescriptor( elem.getName() ) == null ) {
				throw new IllegalArgumentException(
						"Invalid property path. There is no property " + elem.getName() + " in entity " + metaData.getBeanClass()
								.getName()
				);
			}

			List<MetaConstraint<T, ? extends Annotation>> metaConstraintList = metaData.geMetaConstraintsAsList();
			for ( MetaConstraint<T, ?> metaConstraint : metaConstraintList ) {
				if ( metaConstraint.getPropertyName().equals( elem.getName() ) ) {
					metaConstraints.add( metaConstraint );
				}
			}
		}
		else {
			List<Member> cascadedMembers = metaData.getCascadedMembers();
			for ( Member m : cascadedMembers ) {
				if ( ReflectionHelper.getPropertyName( m ).equals( elem.getName() ) ) {
					Type type = ReflectionHelper.typeOf( m );
					value = value == null ? null : ReflectionHelper.getValue( m, value );
					if ( elem.isInIterable() ) {
						if ( value != null && elem.getIndex() != null ) {
							value = ReflectionHelper.getIndexedValue( value, elem.getIndex() );
						}
						else if ( value != null && elem.getKey() != null ) {

						}
						type = ReflectionHelper.getIndexedType( type );
					}

					@SuppressWarnings("unchecked")
					Class<T> valueClass = ( Class<T> ) ( value == null ? type : value.getClass() );

					return collectMetaConstraintsForPath(
							valueClass,
							value,
							propertyIter,
							metaConstraints
					);
				}
			}
		}
		return value;
	}

	private <U> BeanMetaData<U> getBeanMetaData(Class<U> beanClass) {
		BeanMetaDataImpl<U> metadata = beanMetaDataCache.getBeanMetaData( beanClass );
		if ( metadata == null ) {
			metadata = new BeanMetaDataImpl<U>( beanClass, constraintHelper );
			beanMetaDataCache.addBeanMetaData( beanClass, metadata );
		}
		return metadata;
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

	@SuppressWarnings("SimplifiableIfStatement")
	private boolean isValidationRequired(GlobalExecutionContext globalContext, LocalExecutionContext localContext, MetaConstraint metaConstraint) {
		if ( !metaConstraint.getGroupList().contains( localContext.getCurrentGroup() ) ) {
			return false;
		}

		return globalContext.getTraversableResolver().isReachable(
				localContext.getCurrentBean(),
				localContext.getPropertyPath().getLeafNode(),
				globalContext.getRootBeanClass(),
				localContext.getPropertyPath().getPathWithoutLeafNode(),
				metaConstraint.getElementType()
		);
	}

	private boolean isCascadeRequired(GlobalExecutionContext globalContext, LocalExecutionContext localContext, Member member) {
		final ElementType type = member instanceof Field ? ElementType.FIELD : ElementType.METHOD;
		return globalContext.getTraversableResolver().isReachable(
				localContext.getCurrentBean(),
				localContext.getPropertyPath().getLeafNode(),
				globalContext.getRootBeanClass(),
				localContext.getPropertyPath().getPathWithoutLeafNode(),
				type
		)
				&& globalContext.getTraversableResolver().isCascadable(
				localContext.getCurrentBean(),
				localContext.getPropertyPath().getLeafNode(),
				globalContext.getRootBeanClass(),
				localContext.getPropertyPath().getPathWithoutLeafNode(),
				type
		);
	}
}
