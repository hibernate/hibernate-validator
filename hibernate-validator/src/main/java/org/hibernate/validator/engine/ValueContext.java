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

import java.lang.annotation.ElementType;
import java.lang.reflect.Type;
import javax.validation.Path;
import javax.validation.groups.Default;

/**
 * An instance of this class is used to collect all the relevant information for validating a single class/property.
 *
 * @author Hardy Ferentschik
 */
public class ValueContext<T, V> {

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

	/**
	 * The {@code ElementType} the constraint was defined on
	 */
	private ElementType elementType;

	/**
	 * The type of annotated element.
	 */
	private Type typeOfAnnotatedElement;

	public static <T, V> ValueContext<T, V> getLocalExecutionContext(T value, PathImpl propertyPath) {
		@SuppressWarnings("unchecked")
		Class<T> rootBeanClass = (Class<T>) value.getClass();
		return new ValueContext<T, V>( value, rootBeanClass, propertyPath );
	}

	public static <T, V> ValueContext<T, V> getLocalExecutionContext(Class<T> type, PathImpl propertyPath) {
		return new ValueContext<T, V>( null, type, propertyPath );
	}

	public ValueContext(T currentBean, Class<T> currentBeanType, PathImpl propertyPath) {
		this.currentBean = currentBean;
		this.currentBeanType = currentBeanType;
		this.propertyPath = propertyPath == null ? null : PathImpl.createCopy( propertyPath );
	}

	/**
	 * @return returns the current path. A copy is made to avoid side effects.
	 */
	public final PathImpl getPropertyPath() {
		return PathImpl.createCopy( propertyPath );
	}

	public final Class<?> getCurrentGroup() {
		return currentGroup;
	}

	public final T getCurrentBean() {
		return currentBean;
	}

	public final Class<T> getCurrentBeanType() {
		return currentBeanType;
	}

	public final V getCurrentValidatedValue() {
		return currentValue;
	}

	/**
	 * Sets the property path to the match the currently validated value. To avoid side effects a copy of the
	 * provided path is stored.
	 *
	 * @param propertyPath Sets the new property path.
	 */
	public final void setPropertyPath(Path propertyPath) {
		this.propertyPath = PathImpl.createCopy( (PathImpl) propertyPath );
	}

	/**
	 * Adds a new node with the specified name to the current property path.
	 *
	 * @param node the name of the new node. Cannot be {@code null}.
	 */
	public final void appendNode(String node) {
		if ( node == null ) {
			throw new IllegalArgumentException();
		}
		else {
			propertyPath.addNode( node );
		}
	}

	public final void markCurrentPropertyAsIterable() {
		propertyPath.getLeafNode().setIterable( true );
	}

	public final void setKey(Object key) {
		propertyPath.getLeafNode().setKey( key );
	}

	public final void setIndex(Integer index) {
		propertyPath.getLeafNode().setIndex( index );
	}

	public final void setCurrentGroup(Class<?> currentGroup) {
		this.currentGroup = currentGroup;
	}

	public final void setCurrentValidatedValue(V currentValue) {
		this.currentValue = currentValue;
	}

	public final boolean validatingDefault() {
		return getCurrentGroup() != null && getCurrentGroup().getName().equals( Default.class.getName() );
	}

	public final ElementType getElementType() {
		return elementType;
	}

	public final void setElementType(ElementType elementType) {
		this.elementType = elementType;
	}

	public final Type getTypeOfAnnotatedElement() {
		return typeOfAnnotatedElement;
	}

	public final void setTypeOfAnnotatedElement(Type typeOfAnnotatedElement) {
		this.typeOfAnnotatedElement = typeOfAnnotatedElement;
	}

	@Override
	public final String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append( "ValueContext" );
		sb.append( "{currentBean=" ).append( currentBean );
		sb.append( ", currentBeanType=" ).append( currentBeanType );
		sb.append( ", propertyPath=" ).append( propertyPath );
		sb.append( ", currentGroup=" ).append( currentGroup );
		sb.append( ", currentValue=" ).append( currentValue );
		sb.append( ", elementType=" ).append( elementType );
		sb.append( ", typeOfAnnotatedElement=" ).append( typeOfAnnotatedElement );
		sb.append( '}' );
		return sb.toString();
	}
}
