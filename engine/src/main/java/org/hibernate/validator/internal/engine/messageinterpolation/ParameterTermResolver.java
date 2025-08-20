/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.engine.messageinterpolation;

import java.util.Arrays;
import java.util.Locale;

import jakarta.validation.MessageInterpolator.Context;

import org.hibernate.validator.messageinterpolation.HibernateMessageInterpolatorContext;

/**
 * Resolves given parameter.
 *
 * @author Hardy Ferentschik
 * @author Adam Stawicki
 * @author Guillaume Smet
 * @author Alexander Gatsenko
 */
public final class ParameterTermResolver implements TermResolver {

	public static final ParameterTermResolver INSTANCE = new ParameterTermResolver();

	private ParameterTermResolver() {
	}

	@Override
	public String interpolate(Context context, Locale locale, String expression) {
		String resolvedExpression;
		Object variable = getVariable( context, removeCurlyBraces( expression ) );
		if ( variable != null ) {
			resolvedExpression = resolveExpression( variable );
		}
		else {
			resolvedExpression = expression;
		}
		return resolvedExpression;
	}

	private Object getVariable(Context context, String parameter) {
		if ( context instanceof HibernateMessageInterpolatorContext hibernateMessageInterpolatorContext ) {
			Object variable = hibernateMessageInterpolatorContext.getMessageParameters().get( parameter );
			if ( variable != null ) {
				return variable;
			}
		}
		return context.getConstraintDescriptor().getAttributes().get( parameter );
	}

	private String removeCurlyBraces(String parameter) {
		return parameter.substring( 1, parameter.length() - 1 );
	}

	private String resolveExpression(Object variable) {
		final String resolvedExpression;
		if ( variable.getClass().isArray() ) {
			if ( variable.getClass() == boolean[].class ) {
				resolvedExpression = Arrays.toString( (boolean[]) variable );
			}
			else if ( variable.getClass() == char[].class ) {
				resolvedExpression = Arrays.toString( (char[]) variable );
			}
			else if ( variable.getClass() == byte[].class ) {
				resolvedExpression = Arrays.toString( (byte[]) variable );
			}
			else if ( variable.getClass() == short[].class ) {
				resolvedExpression = Arrays.toString( (short[]) variable );
			}
			else if ( variable.getClass() == int[].class ) {
				resolvedExpression = Arrays.toString( (int[]) variable );
			}
			else if ( variable.getClass() == long[].class ) {
				resolvedExpression = Arrays.toString( (long[]) variable );
			}
			else if ( variable.getClass() == float[].class ) {
				resolvedExpression = Arrays.toString( (float[]) variable );
			}
			else if ( variable.getClass() == double[].class ) {
				resolvedExpression = Arrays.toString( (double[]) variable );
			}
			else {
				resolvedExpression = Arrays.toString( (Object[]) variable );
			}
		}
		else {
			resolvedExpression = variable.toString();
		}
		return resolvedExpression;
	}
}
