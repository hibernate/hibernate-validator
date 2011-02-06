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
import java.lang.reflect.Method;
import javax.validation.Path;
import javax.validation.metadata.ConstraintDescriptor;

import org.hibernate.validator.method.MethodConstraintViolation;

/**
 * @author Gunnar Morling
 */
public class MethodConstraintViolationImpl<T> extends ConstraintViolationImpl<T>
		implements MethodConstraintViolation<T> {

	private static final long serialVersionUID = 7907489574577836537L;

	private final Method method;
	private final Integer parameterIndex;
	private final String parameterName;
	private final Kind kind;

	public MethodConstraintViolationImpl(
			String messageTemplate,
			String interpolatedMessage,
			Method method,
			Integer parameterIndex,
			String parameterName,
			Class<T> rootBeanClass,
			T rootBean,
			Object leafBeanInstance,
			Object value,
			Path propertyPath,
			ConstraintDescriptor<?> constraintDescriptor,
			ElementType elementType) {
		super(
				messageTemplate,
				interpolatedMessage,
				rootBeanClass,
				rootBean,
				leafBeanInstance,
				value,
				propertyPath,
				constraintDescriptor,
				elementType
		);

		this.method = method;
		this.parameterIndex = parameterIndex;
		this.parameterName = parameterName;
		this.kind = parameterIndex != null ? Kind.PARAMETER : Kind.RETURN_VALUE;
	}

	public Method getMethod() {
		return method;
	}

	public Integer getParameterIndex() {
		return parameterIndex;
	}

	public String getParameterName() {
		return parameterName;
	}

	public Kind getKind() {
		return kind;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ( ( kind == null ) ? 0 : kind.hashCode() );
		result = prime * result + ( ( method == null ) ? 0 : method.hashCode() );
		result = prime * result
				+ ( ( parameterIndex == null ) ? 0 : parameterIndex.hashCode() );
		result = prime * result
				+ ( ( parameterName == null ) ? 0 : parameterName.hashCode() );
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		if ( !super.equals( obj ) ) {
			return false;
		}
		if ( getClass() != obj.getClass() ) {
			return false;
		}
		MethodConstraintViolationImpl<?> other = (MethodConstraintViolationImpl<?>) obj;
		if ( kind != other.kind ) {
			return false;
		}
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
		if ( parameterName == null ) {
			if ( other.parameterName != null ) {
				return false;
			}
		}
		else if ( !parameterName.equals( other.parameterName ) ) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "MethodConstraintViolationImpl [method=" + method
				+ ", parameterIndex=" + parameterIndex + ", parameterName="
				+ parameterName + ", kind=" + kind + ", message="
				+ getMessage() + ", messageTemplate=" + getMessageTemplate()
				+ ", rootBean=" + getRootBean() + ", rootBeanClass="
				+ getRootBeanClass() + ", leafBean=" + getLeafBean()
				+ ", invalidValue=" + getInvalidValue() + ", propertyPath="
				+ getPropertyPath() + ", constraintDescriptor="
				+ getConstraintDescriptor() + "]";
	}

}
