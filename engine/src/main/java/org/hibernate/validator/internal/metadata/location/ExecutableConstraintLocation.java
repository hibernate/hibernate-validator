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
package org.hibernate.validator.internal.metadata.location;

import java.lang.annotation.ElementType;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.hibernate.validator.internal.metadata.raw.ExecutableElement;
import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.ReflectionHelper;

/**
 * A {@link ConstraintLocation} implementation that represents a method or constructor.
 * parameter or return value.
 *
 * @author Gunnar Morling
 */
public class ExecutableConstraintLocation implements ConstraintLocation {
	private final ExecutableElement executableElement;
	private final Integer parameterIndex;

	public ExecutableConstraintLocation(Method method) {
		this( ExecutableElement.forMethod( method ) );
	}

	/**
	 * Creates a new {@link ExecutableConstraintLocation}.
	 *
	 * @param method The method of the location to be created.
	 * @param parameterIndex The parameter index of the location to be created.
	 */
	public ExecutableConstraintLocation(Method method, Integer parameterIndex) {
		this( ExecutableElement.forMethod( method ), parameterIndex );
	}

	public ExecutableConstraintLocation(ExecutableElement executableElement) {
		this( executableElement, null );
	}

	public ExecutableConstraintLocation(ExecutableElement executableElement, Integer parameterIndex) {
		Contracts.assertValueNotNull( executableElement, "executableElement" );

		this.executableElement = executableElement;
		this.parameterIndex = parameterIndex;
	}

	@Override
	public Class<?> getBeanClass() {
		return executableElement.getMember().getDeclaringClass();
	}

	@Override
	public Type typeOfAnnotatedElement() {
		Type t;

		if ( parameterIndex == null ) {
			t = ReflectionHelper.typeOf( executableElement.getMember() );
		}
		else {
			t = ReflectionHelper.typeOf( executableElement, parameterIndex );
		}

		if ( t instanceof Class && ( (Class<?>) t ).isPrimitive() ) {
			t = ReflectionHelper.boxedType( (Class<?>) t );
		}

		return t;
	}

	@Override
	public Member getMember() {
		return executableElement.getMember();
	}

	public ExecutableElement getExecutableElement() {
		return executableElement;
	}

	@Override
	public ElementType getElementType() {
		return parameterIndex != null ? ElementType.PARAMETER : executableElement.getElementType();
	}

	/**
	 * @return returns the parameter index of this constraint location or <code>null</code> if
	 *         this location represents a executableElement return value.
	 */
	public Integer getParameterIndex() {
		return parameterIndex;
	}

	public Class<?> getParameterType() {
		return parameterIndex != null ? executableElement.getParameterTypes()[parameterIndex] : null;
	}

	@Override
	public String toString() {
		return String.format(
				"%s#%s(%s)",
				executableElement.getMember().getDeclaringClass().getSimpleName(),
				executableElement.getMember().getName(),
				parameterIndex != null ? parameterIndex : ""
		);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ( ( executableElement == null ) ? 0 : executableElement.hashCode() );
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
		ExecutableConstraintLocation other = (ExecutableConstraintLocation) obj;
		if ( executableElement == null ) {
			if ( other.executableElement != null ) {
				return false;
			}
		}
		else if ( !executableElement.equals( other.executableElement ) ) {
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
