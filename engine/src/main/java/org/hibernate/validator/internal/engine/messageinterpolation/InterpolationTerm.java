/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
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
public class InterpolationTerm {
	/**
	 * Meta character to designate an EL expression.
	 */
	private static final String EL_DESIGNATION_CHARACTER = "$";

	/**
	 * The actual expression (parameter or EL expression).
	 */
	private final String expression;

	/**
	 * The type of the expression.
	 */
	private final InterpolationTermType type;

	/**
	 * The resolver for the expression.
	 */
	private final TermResolver resolver;

	/**
	 * Create an interpolation term for an expression.
	 * @param expression the expression.
	 * @param locale the locale.
	 * @param expressionFactory the expression factory to use if the expression uses EL.
     */
	public InterpolationTerm(String expression, Locale locale, ExpressionFactory expressionFactory) {
		this.expression = expression;
		if ( isElExpression( expression ) ) {
			this.type = InterpolationTermType.EL;
			this.resolver = new ElTermResolver( locale, expressionFactory );
		}
		else {
			this.type = InterpolationTermType.PARAMETER;
			this.resolver = new ParameterTermResolver();
		}
	}

	public static boolean isElExpression(String expression) {
		return expression.startsWith( EL_DESIGNATION_CHARACTER );
	}

	public String interpolate(MessageInterpolator.Context context) {
		return resolver.interpolate( context, expression );
	}

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


