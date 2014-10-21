/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine;

import java.util.Map;
import javax.validation.metadata.ConstraintDescriptor;

import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.messageinterpolation.HibernateMessageInterpolatorContext;

/**
 * Implementation of the context used during message interpolation.
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 */
public class MessageInterpolatorContext implements HibernateMessageInterpolatorContext {

	private static final Log log = LoggerFactory.make();

	private final ConstraintDescriptor<?> constraintDescriptor;
	private final Object validatedValue;
	private final Class<?> rootBeanType;
	private final Map<String, Object> messageParameters;

	public MessageInterpolatorContext(ConstraintDescriptor<?> constraintDescriptor,
			Object validatedValue,
			Class<?> rootBeanType,
			Map<String, Object> messageParameters) {
		this.constraintDescriptor = constraintDescriptor;
		this.validatedValue = validatedValue;
		this.rootBeanType = rootBeanType;
		this.messageParameters = messageParameters;
	}

	@Override
	public ConstraintDescriptor<?> getConstraintDescriptor() {
		return constraintDescriptor;
	}

	@Override
	public Object getValidatedValue() {
		return validatedValue;
	}

	@Override
	public Class<?> getRootBeanType() {
		return rootBeanType;
	}

	public Map<String, Object> getMessageParameters() {
		return messageParameters;
	}

	@Override
	public <T> T unwrap(Class<T> type) {
		//allow unwrapping into public super types
		if ( type.isAssignableFrom( HibernateMessageInterpolatorContext.class ) ) {
			return type.cast( this );
		}
		throw log.getTypeNotSupportedForUnwrappingException( type );
	}

	@Override
	public boolean equals(Object o) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		MessageInterpolatorContext that = (MessageInterpolatorContext) o;

		if ( constraintDescriptor != null ? !constraintDescriptor.equals( that.constraintDescriptor ) : that.constraintDescriptor != null ) {
			return false;
		}
		if ( rootBeanType != null ? !rootBeanType.equals( that.rootBeanType ) : that.rootBeanType != null ) {
			return false;
		}
		if ( validatedValue != null ? !validatedValue.equals( that.validatedValue ) : that.validatedValue != null ) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = constraintDescriptor != null ? constraintDescriptor.hashCode() : 0;
		result = 31 * result + ( validatedValue != null ? validatedValue.hashCode() : 0 );
		result = 31 * result + ( rootBeanType != null ? rootBeanType.hashCode() : 0 );
		return result;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append( "MessageInterpolatorContext" );
		sb.append( "{constraintDescriptor=" ).append( constraintDescriptor );
		sb.append( ", validatedValue=" ).append( validatedValue );
		sb.append( '}' );
		return sb.toString();
	}
}
