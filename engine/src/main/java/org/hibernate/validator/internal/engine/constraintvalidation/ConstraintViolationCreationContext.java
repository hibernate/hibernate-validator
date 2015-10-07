/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.constraintvalidation;

import java.util.Collections;
import java.util.Map;

import org.hibernate.validator.internal.engine.path.PathImpl;

/**
 * Container class for the information needed to create a constraint violation.
 *
 * @author Hardy Ferentschik
 */
public class ConstraintViolationCreationContext {
	private final String message;
	private final PathImpl propertyPath;
	private final Map<String, Object> expressionVariables;
	private final Object dynamicPayload;

	public ConstraintViolationCreationContext(String message, PathImpl property) {
		this( message, property, Collections.<String, Object>emptyMap(), null );
	}

	public ConstraintViolationCreationContext(String message, PathImpl property, Map<String, Object> expressionVariables, Object dynamicPayload) {
		this.message = message;
		this.propertyPath = property;
		this.expressionVariables = expressionVariables;
		this.dynamicPayload = dynamicPayload;
	}

	public final String getMessage() {
		return message;
	}

	public final PathImpl getPath() {
		return propertyPath;
	}

	public Map<String, Object> getExpressionVariables() {
		return expressionVariables;
	}

	public Object getDynamicPayload() {
		return dynamicPayload;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder( "ConstraintViolationCreationContext{" );
		sb.append( "message='" ).append( message ).append( '\'' );
		sb.append( ", propertyPath=" ).append( propertyPath );
		sb.append( ", messageParameters=" ).append( expressionVariables );
		sb.append( ", dynamicPayload=" ).append( dynamicPayload );
		sb.append( '}' );
		return sb.toString();
	}
}
