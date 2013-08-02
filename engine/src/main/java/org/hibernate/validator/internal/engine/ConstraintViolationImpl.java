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

import java.io.Serializable;
import java.lang.annotation.ElementType;
import javax.validation.ConstraintViolation;
import javax.validation.Path;
import javax.validation.metadata.ConstraintDescriptor;

import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 */
public class ConstraintViolationImpl<T> implements ConstraintViolation<T>, Serializable {
	private static final Log log = LoggerFactory.make();
	private static final long serialVersionUID = -4970067626703103139L;

	private final String interpolatedMessage;
	private final T rootBean;
	private final Object value;
	private final Path propertyPath;
	private final Object leafBeanInstance;
	private final ConstraintDescriptor<?> constraintDescriptor;
	private final String messageTemplate;
	private final Class<T> rootBeanClass;
	private final ElementType elementType;
	private final Object[] executableParameters;
	private final Object executableReturnValue;
	private final int hashCode;

	public static <T> ConstraintViolation<T> forBeanValidation(String messageTemplate, String interpolatedMessage, Class<T> rootBeanClass, T rootBean, Object leafBeanInstance, Object value, Path propertyPath, ConstraintDescriptor<?> constraintDescriptor, ElementType elementType) {
		return new ConstraintViolationImpl<T>(
				messageTemplate,
				interpolatedMessage,
				rootBeanClass,
				rootBean,
				leafBeanInstance,
				value,
				propertyPath,
				constraintDescriptor,
				elementType,
				null,
				null
		);
	}

	public static <T> ConstraintViolation<T> forParameterValidation(String messageTemplate, String interpolatedMessage, Class<T> rootBeanClass, T rootBean, Object leafBeanInstance, Object value, Path propertyPath, ConstraintDescriptor<?> constraintDescriptor, ElementType elementType, Object[] executableParameters) {
		return new ConstraintViolationImpl<T>(
				messageTemplate,
				interpolatedMessage,
				rootBeanClass,
				rootBean,
				leafBeanInstance,
				value,
				propertyPath,
				constraintDescriptor,
				elementType,
				executableParameters,
				null
		);
	}

	public static <T> ConstraintViolation<T> forReturnValueValidation(String messageTemplate, String interpolatedMessage, Class<T> rootBeanClass, T rootBean, Object leafBeanInstance, Object value, Path propertyPath, ConstraintDescriptor<?> constraintDescriptor, ElementType elementType, Object executableReturnValue) {
		return new ConstraintViolationImpl<T>(
				messageTemplate,
				interpolatedMessage,
				rootBeanClass,
				rootBean,
				leafBeanInstance,
				value,
				propertyPath,
				constraintDescriptor,
				elementType,
				null,
				executableReturnValue
		);
	}

	private ConstraintViolationImpl(String messageTemplate,
			String interpolatedMessage,
			Class<T> rootBeanClass,
			T rootBean,
			Object leafBeanInstance,
			Object value,
			Path propertyPath,
			ConstraintDescriptor<?> constraintDescriptor,
			ElementType elementType,
			Object[] executableParameters,
			Object executableReturnValue) {
		this.messageTemplate = messageTemplate;
		this.interpolatedMessage = interpolatedMessage;
		this.rootBean = rootBean;
		this.value = value;
		this.propertyPath = propertyPath;
		this.leafBeanInstance = leafBeanInstance;
		this.constraintDescriptor = constraintDescriptor;
		this.rootBeanClass = rootBeanClass;
		this.elementType = elementType;
		this.executableParameters = executableParameters;
		this.executableReturnValue = executableReturnValue;
		// pre-calculate hash code, the class is immutable and hashCode is needed often
		this.hashCode = createHashCode();
	}

	@Override
	public final String getMessage() {
		return interpolatedMessage;
	}

	@Override
	public final String getMessageTemplate() {
		return messageTemplate;
	}

	@Override
	public final T getRootBean() {
		return rootBean;
	}

	@Override
	public final Class<T> getRootBeanClass() {
		return rootBeanClass;
	}

	@Override
	public final Object getLeafBean() {
		return leafBeanInstance;
	}

	@Override
	public final Object getInvalidValue() {
		return value;
	}

	@Override
	public final Path getPropertyPath() {
		return propertyPath;
	}

	@Override
	public final ConstraintDescriptor<?> getConstraintDescriptor() {
		return this.constraintDescriptor;
	}

	@Override
	public <C> C unwrap(Class<C> type) {
		if ( type.isAssignableFrom( ConstraintViolation.class ) ) {
			return type.cast( this );
		}
		throw log.getTypeNotSupportedForUnwrappingException( type );
	}

	@Override
	public Object[] getExecutableParameters() {
		return executableParameters;
	}

	@Override
	public Object getExecutableReturnValue() {
		return executableReturnValue;
	}

	@Override
	// IMPORTANT - some behaviour of Validator depends on the correct implementation of this equals method!
	public boolean equals(Object o) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		ConstraintViolationImpl<?> that = (ConstraintViolationImpl<?>) o;

		if ( interpolatedMessage != null ? !interpolatedMessage.equals( that.interpolatedMessage ) : that.interpolatedMessage != null ) {
			return false;
		}
		if ( propertyPath != null ? !propertyPath.equals( that.propertyPath ) : that.propertyPath != null ) {
			return false;
		}
		if ( rootBean != null ? !rootBean.equals( that.rootBean ) : that.rootBean != null ) {
			return false;
		}
		if ( leafBeanInstance != null ? !leafBeanInstance.equals( that.leafBeanInstance ) : that.leafBeanInstance != null ) {
			return false;
		}
		if ( constraintDescriptor != null ? !constraintDescriptor.equals( that.constraintDescriptor ) : that.constraintDescriptor != null ) {
			return false;
		}
		if ( elementType != null ? !elementType.equals( that.elementType ) : that.elementType != null ) {
			return false;
		}
		if ( messageTemplate != null ? !messageTemplate.equals( that.messageTemplate ) : that.messageTemplate != null ) {
			return false;
		}
		if ( rootBeanClass != null ? !rootBeanClass.equals( that.rootBeanClass ) : that.rootBeanClass != null ) {
			return false;
		}
		if ( value != null ? !value.equals( that.value ) : that.value != null ) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append( "ConstraintViolationImpl" );
		sb.append( "{interpolatedMessage='" ).append( interpolatedMessage ).append( '\'' );
		sb.append( ", propertyPath=" ).append( propertyPath );
		sb.append( ", rootBeanClass=" ).append( rootBeanClass );
		sb.append( ", messageTemplate='" ).append( messageTemplate ).append( '\'' );
		sb.append( '}' );
		return sb.toString();
	}

	private int createHashCode() {
		int result = interpolatedMessage != null ? interpolatedMessage.hashCode() : 0;
		result = 31 * result + ( propertyPath != null ? propertyPath.hashCode() : 0 );
		result = 31 * result + ( rootBean != null ? rootBean.hashCode() : 0 );
		result = 31 * result + ( leafBeanInstance != null ? leafBeanInstance.hashCode() : 0 );
		result = 31 * result + ( value != null ? value.hashCode() : 0 );
		result = 31 * result + ( constraintDescriptor != null ? constraintDescriptor.hashCode() : 0 );
		result = 31 * result + ( messageTemplate != null ? messageTemplate.hashCode() : 0 );
		result = 31 * result + ( rootBeanClass != null ? rootBeanClass.hashCode() : 0 );
		result = 31 * result + ( elementType != null ? elementType.hashCode() : 0 );
		return result;
	}
}
