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
import java.util.concurrent.ConcurrentHashMap;
import javax.validation.ConstraintDescriptor;
import javax.validation.ConstraintFactory;
import javax.validation.ElementDescriptor;
import javax.validation.InvalidConstraint;
import javax.validation.MessageResolver;
import javax.validation.Validator;

import org.hibernate.validation.Version;
import org.hibernate.validation.ValidatorConstants;
import org.hibernate.validation.impl.ConstraintDescriptorImpl;
import org.hibernate.validation.impl.ConstraintFactoryImpl;
import org.hibernate.validation.impl.InvalidConstraintImpl;
import org.hibernate.validation.impl.ResourceBundleMessageResolver;
import org.hibernate.validation.util.ReflectionHelper;
import org.hibernate.validation.util.PropertyIterator;

/**
 * The main Bean Validation class.
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 * @todo Make all properties transient for serializability.
 */
public class ValidatorImpl<T> implements Validator<T> {

    private static final Set<Class> INDEXABLE_CLASS = new HashSet<Class>();

	static {
		INDEXABLE_CLASS.add( Integer.class );
		INDEXABLE_CLASS.add( Long.class );
		INDEXABLE_CLASS.add( String.class );
	}

	static {
		Version.touch();
	}

	@SuppressWarnings("unchecked")
	private final List<InvalidConstraintImpl<T>> EMPTY_CONSTRAINTS_LIST = Collections.EMPTY_LIST;

	/**
	 * A map for caching validators for cascaded entities.
	 */
	private final Map<Class<?>, ValidatorImpl> subValidators = new ConcurrentHashMap<Class<?>, ValidatorImpl>();

	/**
	 * Gives access to the required parsed meta data.
	 */
	private final MetaDataProvider<T> metaDataProvider;

	/**
	 * Message resolver used  for interpolating error messages.
	 */
	private final MessageResolver messageResolver;

	public ValidatorImpl(Class<T> beanClass, ConstraintFactory constraintFactory, MessageResolver messageResolver) {
		if ( beanClass == null ) {
			throw new IllegalArgumentException( "Bean class paramter cannot be null" );
		}

		metaDataProvider = new MetaDataProviderImpl<T>( beanClass, constraintFactory );
		this.messageResolver = messageResolver;
	}

	public ValidatorImpl(Class<T> beanClass) {
		this( beanClass, new ConstraintFactoryImpl(), new ResourceBundleMessageResolver() );
	}


	/**
	 * {@inheritDoc}
	 */
	public Set<InvalidConstraint<T>> validate(T object, String... groups) {
		if ( object == null ) {
			throw new IllegalArgumentException( "Validation of a null object" );
		}

		ValidationContext<T> context = new ValidationContext<T>( object );
		List<InvalidConstraintImpl<T>> list = validate( context, Arrays.asList( groups ) );
		return new HashSet<InvalidConstraint<T>>( list );
	}

	/**
	 * Validates the ovject contained in <code>context</code>.
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
	private List<InvalidConstraintImpl<T>> validate(ValidationContext<T> context, List<String> groups) {
		if ( context.peekValidatedObject() == null ) {
			return EMPTY_CONSTRAINTS_LIST;
		}

		// if no group is specified use the default
		if ( groups.size() == 0 ) {
			groups = Arrays.asList( ValidatorConstants.DEFAULT_GROUP_NAME );
		}

		List<String> expandedGroups;
		boolean isGroupSequence;
		for ( String group : groups ) {
			expandedGroups = new ArrayList<String>();
			isGroupSequence = expandGroupName( group, expandedGroups );

			for ( String expandedGroupName : expandedGroups ) {
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
	 * @param context The current validation context.
	 */
	private void validateConstraints(ValidationContext<T> context) {
		for ( ValidatorMetaData metaData : metaDataProvider.getConstraintMetaDataList() ) {
			ConstraintDescriptorImpl constraintDescriptor = metaData.getDescriptor();
			context.pushProperty( metaData.getPropertyName() );

			if ( !context.needsValidation( constraintDescriptor.getGroups() ) ) {
				context.popProperty();
				continue;
			}

			final Object leafBeanInstance = context.peekValidatedObject();
			Object value = metaData.getValue( leafBeanInstance );
			ContextImpl contextImpl = new ContextImpl(constraintDescriptor);

			if ( !constraintDescriptor.getConstraintImplementation().isValid( value, contextImpl ) ) {
				for ( ContextImpl.ErrorMessage error : contextImpl.getErrorMessages() ) {
					String message = messageResolver.interpolate(
							error.getMessage(),
							constraintDescriptor,
							leafBeanInstance
					);
					InvalidConstraintImpl<T> failingConstraint = new InvalidConstraintImpl<T>(
							message,
							context.getRootBean(),
							metaDataProvider.getBeanClass(),
							leafBeanInstance,
							value,
							context.peekPropertyPath(), //FIXME use error.getProperty()
							context.getCurrentGroup()
					);
					context.addConstraintFailure( failingConstraint );
				}
			}
			context.popProperty();
		}
		context.markProcessedForCurrentGroup();
	}

	private void validateCascadedConstraint(ValidationContext<T> context, Type type, Object value) {
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

	private void validateCascadedConstraints(ValidationContext<T> context) {
		List<Member> cascadedMembers = getMetaDataProvider().getCascadedMembers();
		for ( Member member : cascadedMembers ) {
			Type type = ReflectionHelper.typeOf( member );
			context.pushProperty( ReflectionHelper.getPropertyName( member ) );
			ReflectionHelper.setAccessibility( member );
			Object value = ReflectionHelper.getValue( member, context.peekValidatedObject() );
			validateCascadedConstraint( context, type, value );
			context.popProperty();
		}
	}

	private void validateCascadedConstraint(ValidationContext<T> context, Iterator<?> iter) {
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

			Class cascadedClass = actualValue.getClass();
			ValidatorImpl validatorImpl = getValidatorForClass( cascadedClass );
			context.pushValidatedObject( actualValue );
			validatorImpl.validate( context, Arrays.asList( context.getCurrentGroup() ) );
			context.popValidatedObject();
			i++;
		}
	}


	/**
	 * {@inheritDoc}
	 */
	public Set<InvalidConstraint<T>> validateProperty(T object, String propertyName, String... groups) {
		List<InvalidConstraintImpl<T>> failingConstraints = new ArrayList<InvalidConstraintImpl<T>>();
		validateProperty( object, new PropertyIterator( propertyName ), failingConstraints, groups );
		return new HashSet<InvalidConstraint<T>>( failingConstraints );
	}


	private void validateProperty(T object, PropertyIterator propertyIter, List<InvalidConstraintImpl<T>> failingConstraints, String... groups) {
		DesrciptorValueWrapper wrapper = getConstraintDescriptorAndValueForPath( this, propertyIter, object );

		if ( wrapper == null ) {
			return;
		}

		// if no group is specified use the default
		if ( groups.length == 0 ) {
			groups = new String[] { ValidatorConstants.DEFAULT_GROUP_NAME };
		}

		List<String> expandedGroups;
		boolean isGroupSequence;
		for ( String group : groups ) {
			expandedGroups = new ArrayList<String>();
			isGroupSequence = expandGroupName( group, expandedGroups );

			for ( String expandedGroupName : expandedGroups ) {

				if ( !wrapper.descriptor.isInGroups( expandedGroupName ) ) {
					continue;
				}

				ContextImpl contextImpl = new ContextImpl(wrapper.descriptor);
				if ( !wrapper.descriptor.getConstraintImplementation().isValid( wrapper.value, contextImpl ) ) {

					for ( ContextImpl.ErrorMessage error : contextImpl.getErrorMessages() ) {
						String message = messageResolver.interpolate(
								error.getMessage(),
								wrapper.descriptor,
								wrapper.value
						);
						InvalidConstraintImpl<T> failingConstraint = new InvalidConstraintImpl<T>(
								message,
								object,
								( Class<T> ) object.getClass(),
								object,
								wrapper.value,
								propertyIter.getOriginalProperty(), //FIXME use error.getProperty()
								group
						);
						addFailingConstraint( failingConstraints, failingConstraint );
					}
				}

				if ( isGroupSequence && failingConstraints.size() > 0 ) {
					break;
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<InvalidConstraint<T>> validateValue(String propertyName, Object value, String... groups) {
		List<InvalidConstraintImpl<T>> failingConstraints = new ArrayList<InvalidConstraintImpl<T>>();
		validateValue( value, new PropertyIterator( propertyName ), failingConstraints, groups );
		return new HashSet<InvalidConstraint<T>>( failingConstraints );
	}


	private void validateValue(Object object, PropertyIterator propertyIter, List<InvalidConstraintImpl<T>> failingConstraints, String... groups) {
		ConstraintDescriptorImpl constraintDescriptor = getConstraintDescriptorForPath( this, propertyIter );

		if ( constraintDescriptor == null ) {
			return;
		}

		// if no group is specified use the default
		if ( groups.length == 0 ) {
			groups = new String[] { ValidatorConstants.DEFAULT_GROUP_NAME };
		}

		List<String> expandedGroups;
		boolean isGroupSequence;
		for ( String group : groups ) {
			expandedGroups = new ArrayList<String>();
			isGroupSequence = expandGroupName( group, expandedGroups );

			for ( String expandedGroupName : expandedGroups ) {

				if ( !constraintDescriptor.isInGroups( expandedGroupName ) ) {
					continue;
				}

				ContextImpl contextImpl = new ContextImpl(constraintDescriptor);
				if ( !constraintDescriptor.getConstraintImplementation().isValid( object, contextImpl ) ) {
					for ( ContextImpl.ErrorMessage error : contextImpl.getErrorMessages() ) {
						String message = messageResolver.interpolate(
								error.getMessage(),
								constraintDescriptor,
								object
						);
						InvalidConstraintImpl<T> failingConstraint = new InvalidConstraintImpl<T>(
								message,
								null,
								null,
								null,
								object,
								propertyIter.getOriginalProperty(),  //FIXME use error.getProperty()
								""
						);
						addFailingConstraint( failingConstraints, failingConstraint );
					}
				}

				if ( isGroupSequence && failingConstraints.size() > 0 ) {
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
	 * @param validator the validator to check for constraints.
	 * @param propertyIter an instance of <code>PropertyIterator</code>
	 *
	 * @return The constraint descriptor matching the given path.
	 */
	private ConstraintDescriptorImpl getConstraintDescriptorForPath(ValidatorImpl<?> validator, PropertyIterator propertyIter) {

		ConstraintDescriptorImpl matchingConstraintDescriptor = null;
		propertyIter.split();

		if ( !propertyIter.hasNext() ) {
			List<ValidatorMetaData> metaDataList = validator.getMetaDataProvider().getConstraintMetaDataList();
			for ( ValidatorMetaData metaData : metaDataList ) {
				ConstraintDescriptor constraintDescriptor = metaData.getDescriptor();
				if ( metaData.getPropertyName().equals( propertyIter.getHead() ) ) {
					matchingConstraintDescriptor = ( ConstraintDescriptorImpl ) constraintDescriptor;
				}
			}
		}
		else {
			List<Member> cascadedMembers = validator.getMetaDataProvider().getCascadedMembers();
			for ( Member m : cascadedMembers ) {
				if ( ReflectionHelper.getPropertyName( m ).equals( propertyIter.getHead() ) ) {
					Type type = ReflectionHelper.typeOf( m );

					if ( propertyIter.isIndexed() ) {
						type = ReflectionHelper.getIndexedType( type );
						if ( type == null ) {
							continue;
						}
					}

					ValidatorImpl v = getValidatorForClass( ( Class ) type );
					matchingConstraintDescriptor = v.getConstraintDescriptorForPath( v, propertyIter );
				}
			}
		}

		return matchingConstraintDescriptor;
	}


	private DesrciptorValueWrapper getConstraintDescriptorAndValueForPath(ValidatorImpl<?> validator, PropertyIterator propertyIter, Object value) {

		DesrciptorValueWrapper wrapper = null;
		propertyIter.split();


		// bottom out - there is only one token left
		if ( !propertyIter.hasNext() ) {
			List<ValidatorMetaData> metaDataList = validator.getMetaDataProvider().getConstraintMetaDataList();
			for ( ValidatorMetaData metaData : metaDataList ) {
				ConstraintDescriptor constraintDescriptor = metaData.getDescriptor();
				if ( metaData.getPropertyName().equals( propertyIter.getHead() ) ) {
					return new DesrciptorValueWrapper(
							( ConstraintDescriptorImpl ) constraintDescriptor, metaData.getValue( value )
					);
				}
			}
		}
		else {
			List<Member> cascadedMembers = validator.getMetaDataProvider().getCascadedMembers();
			for ( Member m : cascadedMembers ) {
				if ( ReflectionHelper.getPropertyName( m ).equals( propertyIter.getHead() ) ) {
					ReflectionHelper.setAccessibility( m );
					Object newValue = null;
					if ( propertyIter.isIndexed() ) {
						newValue = ReflectionHelper.getValue( m, value );
					}
					else {
						newValue = ReflectionHelper.getIndexedValue( value, propertyIter.getIndex() );
					}
					ValidatorImpl cascadedValidator = getValidatorForClass( newValue.getClass() );
					wrapper = cascadedValidator.getConstraintDescriptorAndValueForPath(
							cascadedValidator, propertyIter, newValue
					);
				}
			}
		}

		return wrapper;
	}


	private void addFailingConstraint(List<InvalidConstraintImpl<T>> failingConstraints, InvalidConstraintImpl<T> failingConstraint) {
		int i = failingConstraints.indexOf( failingConstraint );
		if ( i == -1 ) {
			failingConstraints.add( failingConstraint );
		}
		else {
			failingConstraints.get( i ).addGroups( failingConstraint.getGroups() );
		}
	}


	/**
	 * @todo add child validation
	 */
	public boolean hasConstraints() {
		return metaDataProvider.getConstraintMetaDataList().size() > 0;
	}

	public ElementDescriptor getConstraintsForBean() {
		return metaDataProvider.getBeanDescriptor();
	}

	public ElementDescriptor getConstraintsForProperty(String propertyName) {
		return metaDataProvider.getPropertyDescriptors().get( propertyName );
	}

	public String[] getValidatedProperties() {
		return metaDataProvider.getPropertyDescriptors()
				.keySet()
				.toArray( new String[metaDataProvider.getPropertyDescriptors().size()] );
	}

	public MetaDataProvider<T> getMetaDataProvider() {
		return metaDataProvider;
	}

	/**
	 * Checks whether the provided group name is a group sequence and if so expands the group name and add the expanded
	 * groups names to <code>expandedGroupName </code>
	 *
	 * @param groupName The group name to expand
	 * @param expandedGroupNames The exanded group names or just a list with the single provided group name id the name
	 * was not expandable
	 *
	 * @return <code>true</code> if an expansion took place, <code>false</code> otherwise.
	 */
	private boolean expandGroupName(String groupName, List<String> expandedGroupNames) {
		if ( expandedGroupNames == null ) {
			throw new IllegalArgumentException( "List cannot be empty" );
		}

		boolean isGroupSequence;

		if ( metaDataProvider.getGroupSequences().containsKey( groupName ) ) {
			expandedGroupNames.addAll( metaDataProvider.getGroupSequences().get( groupName ) );
			isGroupSequence = true;
		}
		else {
			expandedGroupNames.add( groupName );
			isGroupSequence = false;
		}
		return isGroupSequence;
	}

	@SuppressWarnings("unchecked")
	private <V> ValidatorImpl<V> getValidatorForClass(Class<V> cascadedClass) {
		ValidatorImpl<V> validatorImpl = subValidators.get( cascadedClass );
		if ( validatorImpl == null ) {
			validatorImpl = new ValidatorImpl<V>( cascadedClass );
			subValidators.put( cascadedClass, validatorImpl );
		}
		return validatorImpl;
	}

	private class DesrciptorValueWrapper {
		final ConstraintDescriptorImpl descriptor;
		final Object value;

		DesrciptorValueWrapper(ConstraintDescriptorImpl descriptor, Object value) {
			this.descriptor = descriptor;
			this.value = value;
		}
	}
}
