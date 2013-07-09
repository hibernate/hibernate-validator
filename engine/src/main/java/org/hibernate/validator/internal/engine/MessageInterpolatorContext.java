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
