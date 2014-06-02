/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.internal.engine.messageinterpolation;

import javax.validation.MessageInterpolator;

/**
 * Helper class dealing with the interpolation of a single message parameter or expression extracted from a message
 * descriptor.
 *
 * @author Hardy Ferentschik
 */
public abstract class InterpolationTerm {
	/**
	 * Meta character to designate an EL expression.
	 */
	private static final String EL_DESIGNATION_CHARACTER = "$";

	/**
	 * The actual expression (parameter or EL expression).
	 */
	protected final String expression;

	/**
	 * The type of the expression.
	 */
	private final InterpolationTermType type;

	public InterpolationTerm(String expression) {
		this.expression = expression;
		if ( isElExpression(expression) ) {
			this.type = InterpolationTermType.EL;
		}
		else {
			this.type = InterpolationTermType.PARAMETER;
		}
	}

	public static boolean isElExpression(String expression) {
		return expression.startsWith( EL_DESIGNATION_CHARACTER );
	}
	
	/**
	 * Interpolates given term based on the constraint validation context.
	 * 
	 * @param context contextual information related to the interpolation
	 * 
	 * @return interpolated message
	 */
	public abstract String interpolate(MessageInterpolator.Context context);

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append( "InterpolationExpression" );
		sb.append( "{expression='" ).append( expression ).append( '\'' );
		sb.append( ", type=" ).append( type );
		sb.append( '}' );
		return sb.toString();
	}
}


