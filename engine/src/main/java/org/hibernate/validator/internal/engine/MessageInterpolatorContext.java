/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine;

import static org.hibernate.validator.internal.util.CollectionHelper.toImmutableMap;

import java.lang.invoke.MethodHandles;
import java.util.Map;

import javax.validation.metadata.ConstraintDescriptor;

import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.util.stereotypes.Immutable;
import org.hibernate.validator.messageinterpolation.HibernateMessageInterpolatorContext;

/**
 * Implementation of the context used during message interpolation.
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Guillaume Smet
 */
public class MessageInterpolatorContext implements HibernateMessageInterpolatorContext {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private final ConstraintDescriptor<?> constraintDescriptor;
	private final Object validatedValue;
	private final Class<?> rootBeanType;
	@Immutable
	private final Map<String, Object> messageParameters;
	@Immutable
	private final Map<String, Object> expressionVariables;

	public MessageInterpolatorContext(ConstraintDescriptor<?> constraintDescriptor,
			Object validatedValue,
			Class<?> rootBeanType,
			Map<String, Object> messageParameters,
			Map<String, Object> expressionVariables) {
		this.constraintDescriptor = constraintDescriptor;
		this.validatedValue = validatedValue;
		this.rootBeanType = rootBeanType;
		this.messageParameters = toImmutableMap( messageParameters );
		this.expressionVariables = toImmutableMap( expressionVariables );
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

	@Override
	public Map<String, Object> getMessageParameters() {
		return messageParameters;
	}

	@Override
	public Map<String, Object> getExpressionVariables() {
		return expressionVariables;
	}

	@Override
	public <T> T unwrap(Class<T> type) {
		//allow unwrapping into public super types
		if ( type.isAssignableFrom( HibernateMessageInterpolatorContext.class ) ) {
			return type.cast( this );
		}
		throw LOG.getTypeNotSupportedForUnwrappingException( type );
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
		if ( validatedValue != null ? ( validatedValue != that.validatedValue ) : that.validatedValue != null ) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = constraintDescriptor != null ? constraintDescriptor.hashCode() : 0;
		result = 31 * result + System.identityHashCode( validatedValue );
		result = 31 * result + ( rootBeanType != null ? rootBeanType.hashCode() : 0 );
		return result;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append( "MessageInterpolatorContext" );
		sb.append( "{constraintDescriptor=" ).append( constraintDescriptor );
		sb.append( ", validatedValue=" ).append( validatedValue );
		sb.append( ", messageParameters=" ).append( messageParameters );
		sb.append( ", expressionVariables=" ).append( expressionVariables );
		sb.append( '}' );
		return sb.toString();
	}
}
