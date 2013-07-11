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
package org.hibernate.validator.internal.engine.constraintvalidation;

import java.util.Collections;
import java.util.Map;
import javax.validation.Path;

/**
 * Container class for the information needed to create a constraint violation.
 *
 * @author Hardy Ferentschik
 */
public class ConstraintViolationCreationContext {
	private final String message;
	private final Path propertyPath;
	private final Map<String, Object> expressionVariables;

	public ConstraintViolationCreationContext(String message, Path property) {
		this( message, property, Collections.<String, Object>emptyMap() );
	}

	public ConstraintViolationCreationContext(String message, Path property, Map<String, Object> expressionVariables) {
		this.message = message;
		this.propertyPath = property;
		this.expressionVariables = expressionVariables;
	}

	public final String getMessage() {
		return message;
	}

	public final Path getPath() {
		return propertyPath;
	}

	public Map<String, Object> getExpressionVariables() {
		return expressionVariables;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder( "ConstraintViolationCreationContext{" );
		sb.append( "message='" ).append( message ).append( '\'' );
		sb.append( ", propertyPath=" ).append( propertyPath );
		sb.append( ", messageParameters=" ).append( expressionVariables );
		sb.append( '}' );
		return sb.toString();
	}
}
