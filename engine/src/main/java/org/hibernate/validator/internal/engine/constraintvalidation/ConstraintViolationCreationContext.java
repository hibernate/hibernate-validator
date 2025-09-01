/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.engine.constraintvalidation;

import java.util.Map;

import jakarta.validation.Path;

import org.hibernate.validator.internal.engine.path.MutablePath;
import org.hibernate.validator.internal.util.stereotypes.Immutable;
import org.hibernate.validator.messageinterpolation.ExpressionLanguageFeatureLevel;

/**
 * Container class for the information needed to create a constraint violation.
 *
 * @author Hardy Ferentschik
 * @author Guillaume Smet
 */
public class ConstraintViolationCreationContext {

	private final String message;
	private final ExpressionLanguageFeatureLevel expressionLanguageFeatureLevel;
	private final boolean customViolation;
	private final Path propertyPath;
	@Immutable
	private final Map<String, Object> messageParameters;
	@Immutable
	private final Map<String, Object> expressionVariables;
	private final Object dynamicPayload;

	public ConstraintViolationCreationContext(String message,
			ExpressionLanguageFeatureLevel expressionLanguageFeatureLevel,
			boolean customViolation,
			MutablePath property,
			Map<String, Object> messageParameters,
			Map<String, Object> expressionVariables,
			Object dynamicPayload) {
		this.message = message;
		this.expressionLanguageFeatureLevel = expressionLanguageFeatureLevel;
		this.customViolation = customViolation;
		// at this point we make a copy of the path to avoid side effects
		this.propertyPath = property.materialize();
		this.messageParameters = messageParameters;
		this.expressionVariables = expressionVariables;
		this.dynamicPayload = dynamicPayload;
	}

	public final String getMessage() {
		return message;
	}

	public ExpressionLanguageFeatureLevel getExpressionLanguageFeatureLevel() {
		return expressionLanguageFeatureLevel;
	}

	public boolean isCustomViolation() {
		return customViolation;
	}

	public final Path getPath() {
		return propertyPath;
	}

	public Map<String, Object> getMessageParameters() {
		return messageParameters;
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
		sb.append( ", expressionLanguageFeatureLevel=" ).append( expressionLanguageFeatureLevel );
		sb.append( ", customViolation=" ).append( customViolation );
		sb.append( ", propertyPath=" ).append( propertyPath );
		sb.append( ", messageParameters=" ).append( messageParameters );
		sb.append( ", expressionVariables=" ).append( expressionVariables );
		sb.append( ", dynamicPayload=" ).append( dynamicPayload );
		sb.append( '}' );
		return sb.toString();
	}
}
