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
import java.util.Set;
import java.util.Stack;

import org.hibernate.validation.impl.ConstraintViolationImpl;
import org.hibernate.validation.util.IdentitySet;

/**
 * Context object keeping track of all processed objects and all failing constraints.
 * At the same time it keeps track of the currently validated object, the current group and property path.
 * The way the validation works at the moment the validated object and the property path have to be processed
 * in a stack fashion.
 * <p/>
 * all sort of information needed  Introduced to reduce the parameters passed around between the different
 * validate methdods in <code>ValidatorImpl</code>.
 *
 * @author Hardy Ferentschik
 * @author Emmanuel Bernard
 */
public class ValidationContext<T> {

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
	 * The current property path.
	 */
	private String propertyPath;

	/**
	 * The current group which is getting processed.
	 */
	private Class<?> currentGroup;

	/**
	 * Stack for keeping track of the currently validated object.
	 */
	private Stack<ValidatedBean> validatedObjectStack = new Stack<ValidatedBean>();


	public ValidationContext(T object) {
		this( object, object );
	}

	public ValidationContext(T rootBean, Object object) {
		this.rootBean = rootBean;
		validatedObjectStack.push( new ValidatedBean( object ) );
		processedObjects = new HashMap<Class<?>, IdentitySet>();
		propertyPath = "";
		failingConstraintViolations = new ArrayList<ConstraintViolationImpl<T>>();
	}

	public Object peekValidatedObject() {
		return validatedObjectStack.peek().bean;
	}

	public Class<?> peekValidatedObjectType() {
		return validatedObjectStack.peek().beanType;
	}

	public void pushValidatedObject(Object validatedObject) {
		validatedObjectStack.push( new ValidatedBean( validatedObject ) );
	}

	public void popValidatedObject() {
		validatedObjectStack.pop();
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
			processedObjects.get( currentGroup ).add( validatedObjectStack.peek().bean );
		}
		else {
			IdentitySet set = new IdentitySet();
			set.add( validatedObjectStack.peek().bean );
			processedObjects.put( currentGroup, set );
		}
	}

	public boolean isProcessedForCurrentGroup(Object value) {
		final IdentitySet objectsProcessedInCurrentGroups = processedObjects.get( currentGroup );
		return objectsProcessedInCurrentGroups != null && objectsProcessedInCurrentGroups.contains( value );
	}

	public void addConstraintFailure(ConstraintViolationImpl<T> failingConstraintViolation) {
		int i = failingConstraintViolations.indexOf( failingConstraintViolation );
		if ( i == -1 ) {
			failingConstraintViolations.add( failingConstraintViolation );
		}
		else {
			failingConstraintViolations.get( i ).addGroups( failingConstraintViolation.getGroups() );
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
		if ( propertyPath.length() == 0 ) {
			propertyPath = property;
		}
		else {
			propertyPath = propertyPath + "." + property;
		}
	}

	/**
	 * Drops the last level of the current property path of this context.
	 */
	public void popProperty() {
		int lastIndex = propertyPath.lastIndexOf( '.' );
		if ( lastIndex != -1 ) {
			propertyPath = propertyPath.substring( 0, lastIndex );
		}
		else {
			propertyPath = "";
		}
	}

	public void appendIndexToPropertyPath(String index) {
		propertyPath += index;
	}

	public void replacePropertyIndex(String index) {
		propertyPath = propertyPath.replaceAll( "\\{0\\}", index );
	}

	public String peekPropertyPath() {
		return propertyPath;
	}

	public boolean needsValidation(Set<Class<?>> groups) {
		return groups.contains( currentGroup );
	}

	/**
	 * @todo Is it useful to cache the object class?
	 */
	private static class ValidatedBean {

		final Object bean;
		final Class<?> beanType;

		private ValidatedBean(Object bean) {
			this.bean = bean;
			if ( bean == null ) {
				this.beanType = null;
			}
			else {
				this.beanType = bean.getClass();
			}
		}
	}
}