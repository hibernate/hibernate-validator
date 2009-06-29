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

import javax.validation.groups.Default;

/**
 * An instance of this class is used to collect all the relevant information for validating a single entity/bean.
 *
 * @author Hardy Ferentschik
 */
public class LocalExecutionContext<T, V> {

	/**
	 * The current bean which gets validated. This is the bean hosting the constraints which get validated.
	 */
	private final T currentBean;

	/**
	 * The class of the current bean.
	 */
	private final Class<T> currentBeanType;

	/**
	 * The current property path we are validating.
	 */
	private PathImpl propertyPath;

	/**
	 * The current group we are validating.
	 */
	private Class<?> currentGroup;

	/**
	 * The value which gets currently evaluated.
	 */
	private V currentValue;

	public static <T, V> LocalExecutionContext<T, V> getLocalExecutionContext(T value) {
		@SuppressWarnings("unchecked")
		Class<T> rootBeanClass = ( Class<T> ) value.getClass();
		return new LocalExecutionContext<T, V>( value, rootBeanClass );
	}

	public static <T, V> LocalExecutionContext<T, V> getLocalExecutionContext(Class<T> type) {
		return new LocalExecutionContext<T, V>( null, type );
	}

	public LocalExecutionContext(T currentBean, Class<T> currentBeanType) {
		this.currentBean = currentBean;
		this.currentBeanType = currentBeanType;
	}

	public PathImpl getPropertyPath() {
		return propertyPath;
	}

	public Class<?> getCurrentGroup() {
		return currentGroup;
	}

	public T getCurrentBean() {
		return currentBean;
	}

	public Class<T> getCurrentBeanType() {
		return currentBeanType;
	}

	public V getCurrentValidatedValue() {
		return currentValue;
	}

	public void setPropertyPath(PathImpl propertyPath) {
		this.propertyPath = propertyPath;
	}

	public void setCurrentGroup(Class<?> currentGroup) {
		this.currentGroup = currentGroup;
	}

	public void setCurrentValidatedValue(V currentValue) {
		this.currentValue = currentValue;
	}

	public void markCurrentPropertyAsIterable() {
		propertyPath.getLeafNode().setInIterable( true );
	}

	public boolean validatingDefault() {
		return getCurrentGroup() != null && getCurrentGroup().getName().equals( Default.class.getName() );
	}

	@Override
	public String toString() {
		return "LocalExecutionContext{" +
				"currentBean=" + currentBean +
				", currentBeanType=" + currentBeanType +
				", propertyPath=" + propertyPath +
				", currentGroup=" + currentGroup +
				", currentValue=" + currentValue +
				'}';
	}
}
