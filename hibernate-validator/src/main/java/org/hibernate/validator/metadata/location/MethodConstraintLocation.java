/*
* JBoss, Home of Professional Open Source
* Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.hibernate.validator.metadata.location;

import java.lang.annotation.ElementType;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.hibernate.validator.util.Contracts;
import org.hibernate.validator.util.ReflectionHelper;

/**
 * A {@link ConstraintLocation} implementation that represents a method
 * parameter or return value.
 *
 * @author Gunnar Morling
 */
public class MethodConstraintLocation implements ConstraintLocation {

	private final Method method;

	private final Integer parameterIndex;

	public MethodConstraintLocation(Method method) {

		Contracts.assertNotNull( method, "Method must not be null" );

		this.method = method;
		this.parameterIndex = null;
	}

	/**
	 * Creates a new {@link MethodConstraintLocation}.
	 *
	 * @param method The method of the location to be created.
	 * @param parameterIndex The parameter index of the location to be created.
	 */
	public MethodConstraintLocation(Method method, int parameterIndex) {

		Contracts.assertNotNull( method, "Method must not be null" );

		this.method = method;
		this.parameterIndex = parameterIndex;
	}

	public Class<?> getBeanClass() {
		return method.getDeclaringClass();
	}

	public Type typeOfAnnotatedElement() {
		Type t = null;

		if ( parameterIndex == null ) {
			t = ReflectionHelper.typeOf( method );
		}
		else {
			t = ReflectionHelper.typeOf( method, parameterIndex );
		}

		if ( t instanceof Class && ( (Class<?>) t ).isPrimitive() ) {
			t = ReflectionHelper.boxedType( t );
		}

		return t;
	}

	public Method getMember() {
		return method;
	}

	public ElementType getElementType() {
		return parameterIndex != null ? ElementType.PARAMETER : ElementType.METHOD;
	}

	/**
	 * The parameter index of this constraint location or <code>null</code> if
	 * this location represents a method return value.
	 */
	public Integer getParameterIndex() {
		return parameterIndex;
	}

	public Class<?> getParameterType() {
		return parameterIndex != null ? method.getParameterTypes()[parameterIndex] : null;
	}

	@Override
	public String toString() {
		return String.format(
				"%s#%s(%s)",
				method.getDeclaringClass().getSimpleName(),
				method.getName(),
				parameterIndex != null ? parameterIndex : ""
		);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ( ( method == null ) ? 0 : method.hashCode() );
		result = prime * result
				+ ( ( parameterIndex == null ) ? 0 : parameterIndex.hashCode() );
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		if ( obj == null ) {
			return false;
		}
		if ( getClass() != obj.getClass() ) {
			return false;
		}
		MethodConstraintLocation other = (MethodConstraintLocation) obj;
		if ( method == null ) {
			if ( other.method != null ) {
				return false;
			}
		}
		else if ( !method.equals( other.method ) ) {
			return false;
		}
		if ( parameterIndex == null ) {
			if ( other.parameterIndex != null ) {
				return false;
			}
		}
		else if ( !parameterIndex.equals( other.parameterIndex ) ) {
			return false;
		}
		return true;
	}

}
