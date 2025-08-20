/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.engine.messageinterpolation;

import java.util.Locale;

import jakarta.el.ExpressionFactory;
import jakarta.validation.MessageInterpolator;

/**
 * Helper class dealing with the interpolation of a single message parameter or expression extracted from a message
 * descriptor.
 *
 * @author Hardy Ferentschik
 */
public class TermInterpolator {
	/**
	 * Meta character to designate an EL expression.
	 */
	private static final String EL_DESIGNATION_CHARACTER = "$";

	private final ElTermResolver elTermResolver;

	/**
	 * Create a term interpolator.
	 *
	 * @param expressionFactory the expression factory to use if the expression uses EL.
	 */
	public TermInterpolator(ExpressionFactory expressionFactory) {
		this.elTermResolver = new ElTermResolver( expressionFactory );
	}

	public static boolean isElExpression(String expression) {
		return expression.startsWith( EL_DESIGNATION_CHARACTER );
	}

	/**
	 * Interpolate an expression.
	 *
	 * @param expression the expression.
	 * @param locale the locale.
	 */
	public String interpolate(MessageInterpolator.Context context, String expression, Locale locale) {
		if ( isElExpression( expression ) ) {
			return elTermResolver.interpolate( context, locale, expression );
		}
		else {
			return ParameterTermResolver.INSTANCE.interpolate( context, locale, expression );
		}
	}
}
