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
import java.lang.reflect.Type;

import javax.validation.ElementKind;
import javax.validation.groups.Default;

import org.hibernate.validator.internal.engine.path.PathImpl;
import org.hibernate.validator.internal.metadata.aggregated.ParameterMetaData;
import org.hibernate.validator.internal.metadata.facets.Cascadable;
import org.hibernate.validator.internal.metadata.facets.Validatable;
import org.hibernate.validator.spi.valuehandling.ValidatedValueUnwrapper;

/**
 * An instance of this class is used to collect all the relevant information for validating a single class, property or
 * method invocation.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
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

	private final Validatable currentValidatable;

	/**
	 * The {@code ElementType} the constraint was defined on
	 */
	private ElementType elementType;

	/**
	 * The type of annotated element.
	 */
	private Type typeOfAnnotatedElement;

	private ValidatedValueUnwrapper<? super V> validatedValueHandler;

	public static <T, V> ValueContext<T, V> getLocalExecutionContext(T value, Validatable validatable, PathImpl propertyPath) {
		@SuppressWarnings("unchecked")
		Class<T> rootBeanClass = (Class<T>) value.getClass();
		return new ValueContext<T, V>( value, rootBeanClass, validatable, propertyPath );
	}

	public static <T, V> ValueContext<T, V> getLocalExecutionContext(Class<T> type, Validatable validatable, PathImpl propertyPath) {
		return new ValueContext<T, V>( null, type, validatable, propertyPath );
	}

	private ValueContext(T currentBean, Class<T> currentBeanType, Validatable validatable, PathImpl propertyPath) {
		this.currentBean = currentBean;
		this.currentBeanType = currentBeanType;
		this.currentValidatable = validatable;
		this.propertyPath = propertyPath;
	}

	public final PathImpl getPropertyPath() {
		return propertyPath;
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

	public Validatable getCurrentValidatable() {
		return currentValidatable;
	}

	/**
	 * Returns the current value to be validated. If a {@link ValidatedValueUnwrapper} has been set, the value will be
	 * retrieved via that handler.
	 *
	 * @return the current value to be validated
	 */
	public final Object getCurrentValidatedValue() {
		return validatedValueHandler != null ? validatedValueHandler.handleValidatedValue( currentValue ) : currentValue;
	}

	public final void setPropertyPath(PathImpl propertyPath) {
		this.propertyPath = propertyPath;
	}

	public final void appendNode(Cascadable node) {
		propertyPath = PathImpl.createCopy( propertyPath );

		if ( node.getKind() == ElementKind.PROPERTY ) {
			propertyPath.addPropertyNode( node.getName() );
		}
		else if ( node.getKind() == ElementKind.PARAMETER ) {
			propertyPath.addParameterNode( node.getName(), ( (ParameterMetaData) node ).getIndex() );
		}
		else if ( node.getKind() == ElementKind.RETURN_VALUE ) {
			propertyPath.addReturnValueNode();
		}
	}

	public final void appendBeanNode() {
		propertyPath = PathImpl.createCopy( propertyPath );
		propertyPath.addBeanNode();
	}

	public final void appendCrossParameterNode() {
		propertyPath = PathImpl.createCopy( propertyPath );
		propertyPath.addCrossParameterNode();
	}

	public final void markCurrentPropertyAsIterable() {
		propertyPath.makeLeafNodeIterable();
	}

	public final void setKey(Object key) {
		propertyPath.setLeafNodeMapKey( key );
	}

	public final void setIndex(Integer index) {
		propertyPath.setLeafNodeIndex( index );
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

	/**
	 * Returns the declared (static) type of the currently validated element. If a {@link ValidatedValueUnwrapper} has
	 * been set, the type will be retrieved via that handler.
	 *
	 * @return the declared type of the currently validated element
	 */
	public final Type getTypeOfAnnotatedElement() {
		return validatedValueHandler != null ? validatedValueHandler.getValidatedValueType( typeOfAnnotatedElement ) : typeOfAnnotatedElement;
	}

	public final void setTypeOfAnnotatedElement(Type typeOfAnnotatedElement) {
		this.typeOfAnnotatedElement = typeOfAnnotatedElement;
	}

	@Override
	public String toString() {
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

	public void setValidatedValueHandler(ValidatedValueUnwrapper<? super V> handler) {
		this.validatedValueHandler = handler;
	}
}
