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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import javax.validation.ConstraintDescriptor;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.MessageInterpolator;
import javax.validation.TraversableResolver;

import org.hibernate.validation.util.IdentitySet;

/**
 * Context object keeping track of all processed objects and failing constraints.
 * It also keeps track of the currently validated object, group and property path.
 *
 * @author Hardy Ferentschik
 * @author Emmanuel Bernard
 * @todo Look for ways to improve this data structure. It is quite fragile and depends on the right oder of calls
 * in order to work.
 */
public class ExecutionContext<T> implements ConstraintValidatorContext {

	private static final String PROPERTY_ROOT = "";

	private static final String PROPERTY_PATH_SEPERATOR = ".";

	/**
	 * The root bean of the validation.
	 */
	private final T rootBean;

	/**
	 * Maps for each group to an identity set to keep track of already validated objects. We have to make sure
	 * that each object gets only validated once (per group).
	 */
	private final Map<Class<?>, IdentitySet> processedObjects;

	/**
	 * A list of all failing constraints so far.
	 */
	private final List<ConstraintViolationImpl<T>> failingConstraintViolations;

	/**
	 * The current property based based from the root bean.
	 */
	private List<String> propertyPath;

	/**
	 * The current group we are validating.
	 */
	private Class<?> currentGroup;

	/**
	 * Reference to a <code>ValidatedProperty</code> keeping track of the property we are currently validating,
	 * together with required meta data.
	 */
	private ValidatedProperty currentValidatedProperty;

	/**
	 * Stack for keeping track of the currently validated bean.
	 */
	private Stack<Object> beanStack = new Stack<Object>();

	/**
	 * The message resolver which should be used in this context.
	 */
	private final MessageInterpolator messageInterpolator;

	/**
	 * The constraint factory which should be used in this context.
	 */
	private final ConstraintValidatorFactory constraintValidatorFactory;

	/**
	 * Allows a JPA provider to decide whether a property should be validated.
	 */
	private final TraversableResolver traversableResolver;

	public ExecutionContext(T object, MessageInterpolator messageInterpolator, ConstraintValidatorFactory constraintValidatorFactory, TraversableResolver traversableResolver) {
		this( object, object, messageInterpolator, constraintValidatorFactory, traversableResolver );
	}

	public ExecutionContext(T rootBean, Object object, MessageInterpolator messageInterpolator, ConstraintValidatorFactory constraintValidatorFactory, TraversableResolver traversableResolver) {
		this.rootBean = rootBean;
		this.messageInterpolator = messageInterpolator;
		this.constraintValidatorFactory = constraintValidatorFactory;
		this.traversableResolver = traversableResolver;

		beanStack.push( object );
		processedObjects = new HashMap<Class<?>, IdentitySet>();
		propertyPath = new ArrayList<String>();
		failingConstraintViolations = new ArrayList<ConstraintViolationImpl<T>>();
	}

	public ConstraintValidatorFactory getConstraintValidatorFactory() {
		return constraintValidatorFactory;
	}

	public void disableDefaultError() {
		assert currentValidatedProperty != null;
		currentValidatedProperty.disableDefaultError();
	}

	public String getDefaultErrorMessage() {
		assert currentValidatedProperty != null;
		return currentValidatedProperty.getDefaultErrorMessage();
	}

	public void addError(String message) {
		assert currentValidatedProperty != null;
		currentValidatedProperty.addError( message );
	}

	public void addError(String message, String property) {
		assert currentValidatedProperty != null;
		currentValidatedProperty.addError( message, property );
	}

	public void setCurrentConstraintDescriptor(ConstraintDescriptor constraintDescriptor) {
		assert currentValidatedProperty != null;
		currentValidatedProperty.setConstraintDescriptor( constraintDescriptor );
	}

	public Object peekCurrentBean() {
		return beanStack.peek();
	}

	public Class<?> peekCurrentBeanType() {
		return beanStack.peek().getClass();
	}

	public void pushCurrentBean(Object validatedBean) {
		beanStack.push( validatedBean );
	}

	public void popCurrentBean() {
		beanStack.pop();
	}

	public T getRootBean() {
		return rootBean;
	}

	public Class<?> getCurrentGroup() {
		return currentGroup;
	}

	public void setCurrentGroup(Class<?> currentGroup) {
		this.currentGroup = currentGroup;
	}

	public void markProcessedForCurrentGroup() {
		if ( processedObjects.containsKey( currentGroup ) ) {
			processedObjects.get( currentGroup ).add( beanStack.peek() );
		}
		else {
			IdentitySet set = new IdentitySet();
			set.add( beanStack.peek() );
			processedObjects.put( currentGroup, set );
		}
	}

	public boolean isValidatedAgainstCurrentGroup(Object value) {
		final IdentitySet objectsProcessedInCurrentGroups = processedObjects.get( currentGroup );
		return objectsProcessedInCurrentGroups != null && objectsProcessedInCurrentGroups.contains( value );
	}

	private void addConstraintFailure(ConstraintViolationImpl<T> failingConstraintViolation) {
		int i = failingConstraintViolations.indexOf( failingConstraintViolation );
		if ( i == -1 ) {
			failingConstraintViolations.add( failingConstraintViolation );
		}
	}

	public void addConstraintFailures(List<ConstraintViolationImpl<T>> failingConstraintViolations) {
		for ( ConstraintViolationImpl<T> violation : failingConstraintViolations ) {
			addConstraintFailure( violation );
		}
	}

	public List<ConstraintViolationImpl<T>> getFailingConstraints() {
		return failingConstraintViolations;
	}

	/**
	 * Adds a new level to the current property path of this context.
	 *
	 * @param property the new property to add to the current path.
	 */
	public void pushProperty(String property) {
		currentValidatedProperty = new ValidatedProperty( peekPropertyPath(), property );
		propertyPath.add( property );
	}

	/**
	 * Drops the last level of the current property path of this context.
	 */
	public void popProperty() {
		if ( propertyPath.size() > 0 ) {
			propertyPath.remove( propertyPath.size() - 1 );
			currentValidatedProperty = null;
		}
	}

	public void markCurrentPropertyAsIndexed() {
		String property = peekProperty();
		property += "[]";
		popProperty();
		pushProperty( property );
	}

	public void replacePropertyIndex(String index) {
		// replace the last occurance of [<oldIndex>] with [<index>]
		String property = peekProperty();
		property = property.replaceAll( "\\[[0-9]*\\]$", "[" + index + "]" );
		popProperty();
		pushProperty( property );
	}

	public String peekPropertyPath() {
		StringBuilder builder = new StringBuilder();
		for ( int i = 0; i <= propertyPath.size() - 1; i++ ) {
			builder.append( propertyPath.get( i ) );
			if ( i < propertyPath.size() - 1 ) {
				builder.append( PROPERTY_PATH_SEPERATOR );
			}
		}
		return builder.toString();
	}

	public String peekProperty() {
		if ( propertyPath.size() == 0 ) {
			return PROPERTY_ROOT;
		}
		return propertyPath.get( propertyPath.size() - 1 );
	}

	public boolean isValidationRequired(MetaConstraint metaConstraint) {
		if ( !metaConstraint.getGroupList().contains( currentGroup ) ) {
			return false;
		}

		Class<?> rootBeanClass = rootBean == null ? null : rootBean.getClass();
		return traversableResolver.isTraversable(
				peekCurrentBean(),
				peekProperty(),
				rootBeanClass,
				peekPropertyPath(),
				metaConstraint.getElementType()
		);
	}

	public List<ConstraintViolationImpl<T>> createConstraintViolations(Object value) {
		List<ConstraintViolationImpl<T>> constraintViolations = new ArrayList<ConstraintViolationImpl<T>>();
		for ( ErrorMessage error : currentValidatedProperty.getErrorMessages() ) {
			constraintViolations.add( createConstraintViolation( value, error ) );
		}
		return constraintViolations;
	}

	public ConstraintViolationImpl<T> createConstraintViolation(Object value, ErrorMessage error) {
		ConstraintDescriptor descriptor = currentValidatedProperty.getConstraintDescriptor();
		String messageTemplate = error.getMessage();
		String interpolatedMessage = messageInterpolator.interpolate(
				messageTemplate,
				new MessageInterpolatorContext( descriptor, peekCurrentBean() )
		);
		return new ConstraintViolationImpl<T>(
				messageTemplate,
				interpolatedMessage,
				getRootBean(),
				peekCurrentBean(),
				value,
				error.getProperty(),
				descriptor
		);
	}

	class ValidatedProperty {

		private final List<ErrorMessage> errorMessages = new ArrayList<ErrorMessage>( 3 );
		private final String property;
		private final String propertyParent;
		private ConstraintDescriptor constraintDescriptor;
		private boolean defaultDisabled;


		private ValidatedProperty(String propertyParent, String property) {
			this.property = property;
			this.propertyParent = propertyParent;
		}

		public void setConstraintDescriptor(ConstraintDescriptor constraintDescriptor) {
			this.constraintDescriptor = constraintDescriptor;
		}

		public ConstraintDescriptor getConstraintDescriptor() {
			return constraintDescriptor;
		}

		void disableDefaultError() {
			defaultDisabled = true;
		}

		public boolean isDefaultErrorDisabled() {
			return defaultDisabled;
		}

		public String getDefaultErrorMessage() {
			return ( String ) constraintDescriptor.getAttributes().get( "message" );
		}

		public void addError(String message) {
			errorMessages.add( new ErrorMessage( message, buildPropertyPath( propertyParent, property ) ) );
		}

		public void addError(String message, String property) {
			errorMessages.add( new ErrorMessage( message, buildPropertyPath( propertyParent, property ) ) );
		}

		public List<ErrorMessage> getErrorMessages() {
			List<ErrorMessage> returnedErrorMessages = new ArrayList<ErrorMessage>( errorMessages );
			if ( !defaultDisabled ) {
				returnedErrorMessages.add(
						new ErrorMessage( getDefaultErrorMessage(), buildPropertyPath( propertyParent, property ) )
				);
			}
			return returnedErrorMessages;
		}

		private String buildPropertyPath(String parent, String leaf) {
			if ( PROPERTY_ROOT.equals( parent ) ) {
				return leaf;
			}
			else {
				return new StringBuilder().append( parent ).append( PROPERTY_PATH_SEPERATOR ).append( leaf ).toString();
			}
		}
	}

	public class ErrorMessage {
		private final String message;
		private final String property;

		public ErrorMessage(String message, String property) {
			this.message = message;
			this.property = property;
		}

		public String getMessage() {
			return message;
		}

		public String getProperty() {
			return property;
		}
	}
}