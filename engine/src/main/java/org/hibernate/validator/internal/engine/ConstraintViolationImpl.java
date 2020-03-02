/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Map;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import jakarta.validation.metadata.ConstraintDescriptor;

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.hibernate.validator.engine.HibernateConstraintViolation;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 */
public class ConstraintViolationImpl<T> implements HibernateConstraintViolation<T>, Serializable {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );
	private static final long serialVersionUID = -4970067626703103139L;

	private final String interpolatedMessage;
	private final T rootBean;
	private final Object value;
	private final Path propertyPath;
	private final Object leafBeanInstance;
	private final ConstraintDescriptor<?> constraintDescriptor;
	private final String messageTemplate;
	private final Map<String, Object> messageParameters;
	private final Map<String, Object> expressionVariables;
	private final Class<T> rootBeanClass;
	private final Object[] executableParameters;
	private final Object executableReturnValue;
	private final Object dynamicPayload;
	private final int hashCode;

	public static <T> ConstraintViolation<T> forBeanValidation(String messageTemplate,
			Map<String, Object> messageParameters,
			Map<String, Object> expressionVariables,
			String interpolatedMessage,
			Class<T> rootBeanClass,
			T rootBean,
			Object leafBeanInstance,
			Object value,
			Path propertyPath,
			ConstraintDescriptor<?> constraintDescriptor,
			Object dynamicPayload) {
		return new ConstraintViolationImpl<>(
				messageTemplate,
				messageParameters,
				expressionVariables,
				interpolatedMessage,
				rootBeanClass,
				rootBean,
				leafBeanInstance,
				value,
				propertyPath,
				constraintDescriptor,
				null,
				null,
				dynamicPayload
		);
	}

	public static <T> ConstraintViolation<T> forParameterValidation(String messageTemplate,
			Map<String, Object> messageParameters,
			Map<String, Object> expressionVariables,
			String interpolatedMessage,
			Class<T> rootBeanClass,
			T rootBean,
			Object leafBeanInstance,
			Object value,
			Path propertyPath,
			ConstraintDescriptor<?> constraintDescriptor,
			Object[] executableParameters,
			Object dynamicPayload) {
		return new ConstraintViolationImpl<>(
				messageTemplate,
				messageParameters,
				expressionVariables,
				interpolatedMessage,
				rootBeanClass,
				rootBean,
				leafBeanInstance,
				value,
				propertyPath,
				constraintDescriptor,
				executableParameters,
				null,
				dynamicPayload
		);
	}

	public static <T> ConstraintViolation<T> forReturnValueValidation(String messageTemplate,
			Map<String, Object> messageParameters,
			Map<String, Object> expressionVariables,
			String interpolatedMessage,
			Class<T> rootBeanClass,
			T rootBean,
			Object leafBeanInstance,
			Object value,
			Path propertyPath,
			ConstraintDescriptor<?> constraintDescriptor,
			Object executableReturnValue,
			Object dynamicPayload) {
		return new ConstraintViolationImpl<>(
				messageTemplate,
				messageParameters,
				expressionVariables,
				interpolatedMessage,
				rootBeanClass,
				rootBean,
				leafBeanInstance,
				value,
				propertyPath,
				constraintDescriptor,
				null,
				executableReturnValue,
				dynamicPayload
		);
	}

	private ConstraintViolationImpl(String messageTemplate,
			Map<String, Object> messageParameters,
			Map<String, Object> expressionVariables,
			String interpolatedMessage,
			Class<T> rootBeanClass,
			T rootBean,
			Object leafBeanInstance,
			Object value,
			Path propertyPath,
			ConstraintDescriptor<?> constraintDescriptor,
			Object[] executableParameters,
			Object executableReturnValue,
			Object dynamicPayload) {
		this.messageTemplate = messageTemplate;
		this.messageParameters = messageParameters;
		this.expressionVariables = expressionVariables;
		this.interpolatedMessage = interpolatedMessage;
		this.rootBean = rootBean;
		this.value = value;
		this.propertyPath = propertyPath;
		this.leafBeanInstance = leafBeanInstance;
		this.constraintDescriptor = constraintDescriptor;
		this.rootBeanClass = rootBeanClass;
		this.executableParameters = executableParameters;
		this.executableReturnValue = executableReturnValue;
		this.dynamicPayload = dynamicPayload;
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

	/**
	 * @return the message parameters added using {@link HibernateConstraintValidatorContext#addMessageParameter(String, Object)}
	 */
	public Map<String, Object> getMessageParameters() {
		return messageParameters;
	}

	/**
	 * @return the expression variables added using {@link HibernateConstraintValidatorContext#addExpressionVariable(String, Object)}
	 */
	public Map<String, Object> getExpressionVariables() {
		return expressionVariables;
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
		// Keep backward compatibility
		if ( type.isAssignableFrom( ConstraintViolation.class ) ) {
			return type.cast( this );
		}
		if ( type.isAssignableFrom( HibernateConstraintViolation.class ) ) {
			return type.cast( this );
		}
		throw LOG.getTypeNotSupportedForUnwrappingException( type );
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
	public <C> C getDynamicPayload(Class<C> type) {
		if ( dynamicPayload != null && type.isAssignableFrom( dynamicPayload.getClass() ) ) {
			return type.cast( dynamicPayload );
		}
		else {
			return null;
		}
	}

	/**
	 * IMPORTANT - some behaviour of Validator depends on the correct implementation of this equals method! (HF)
	 * <p>
	 * {@code messageParameters}, {@code expressionVariables} and {@code dynamicPayload} are not taken into account for
	 * equality. These variables solely enrich the actual Constraint Violation with additional information e.g how we
	 * actually got to this CV.
	 *
	 * @return true if the two ConstraintViolation's are considered equals; false otherwise
	 */
	@Override
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
		if ( messageTemplate != null ? !messageTemplate.equals( that.messageTemplate ) : that.messageTemplate != null ) {
			return false;
		}
		if ( propertyPath != null ? !propertyPath.equals( that.propertyPath ) : that.propertyPath != null ) {
			return false;
		}
		if ( rootBean != null ? ( rootBean != that.rootBean ) : that.rootBean != null ) {
			return false;
		}
		if ( leafBeanInstance != null ? ( leafBeanInstance != that.leafBeanInstance ) : that.leafBeanInstance != null ) {
			return false;
		}
		if ( value != null ? ( value != that.value ) : that.value != null ) {
			return false;
		}
		if ( constraintDescriptor != null ? !constraintDescriptor.equals( that.constraintDescriptor ) : that.constraintDescriptor != null ) {
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

	/**
	 * @see #equals(Object) on which fields are taken into account
	 */
	private int createHashCode() {
		int result = interpolatedMessage != null ? interpolatedMessage.hashCode() : 0;
		result = 31 * result + ( propertyPath != null ? propertyPath.hashCode() : 0 );
		result = 31 * result + System.identityHashCode( rootBean );
		result = 31 * result + System.identityHashCode( leafBeanInstance );
		result = 31 * result + System.identityHashCode( value );
		result = 31 * result + ( constraintDescriptor != null ? constraintDescriptor.hashCode() : 0 );
		result = 31 * result + ( messageTemplate != null ? messageTemplate.hashCode() : 0 );
		return result;
	}
}
