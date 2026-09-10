/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.engine.messageinterpolation;

import static org.hibernate.validator.internal.engine.messageinterpolation.util.InterpolationHelper.resolveToString;

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
public class ParameterTermResolver implements TermResolver {

	@Override
	public String interpolate(Context context, String expression) {
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
		if ( context instanceof HibernateMessageInterpolatorContext ) {
			Object variable = ( (HibernateMessageInterpolatorContext) context ).getMessageParameters().get( parameter );
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
