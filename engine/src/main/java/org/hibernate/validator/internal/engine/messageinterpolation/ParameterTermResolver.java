/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.messageinterpolation;

import java.util.Arrays;

import javax.validation.MessageInterpolator.Context;

/**
 * Resolves given parameter.
 * 
 * @author Hardy Ferentschik
 * @author Adam Stawicki
 */
public class ParameterTermResolver implements TermResolver {

	@Override
	public String interpolate(Context context, String expression) {
		String resolvedExpression;
		Object variable = context.getConstraintDescriptor()
				.getAttributes()
				.get( removeCurlyBraces( expression ) );
		if ( variable != null ) {
			if ( variable.getClass().isArray() ) {
				resolvedExpression = Arrays.toString( (Object[]) variable );
			}
			else {
				resolvedExpression = variable.toString();
			}
		}
		else {
			resolvedExpression = expression;
		}
		return resolvedExpression;
	}

	private String removeCurlyBraces(String parameter) {
		return parameter.substring( 1, parameter.length() - 1 );
	}
}
