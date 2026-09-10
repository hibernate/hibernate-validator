/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.engine.messageinterpolation;

import static org.hibernate.validator.internal.engine.messageinterpolation.util.InterpolationHelper.resolveToString;

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
			resolvedExpression = resolveToString( variable );
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
}
