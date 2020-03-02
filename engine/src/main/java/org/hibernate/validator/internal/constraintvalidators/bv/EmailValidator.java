/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv;

import java.lang.invoke.MethodHandles;
import java.util.regex.Matcher;
import java.util.regex.PatternSyntaxException;

import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

import org.hibernate.validator.internal.constraintvalidators.AbstractEmailValidator;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * Checks that a given character sequence (e.g. string) is a well-formed email address.
 *
 * @author Guillaume Smet
 */
public class EmailValidator extends AbstractEmailValidator<Email> {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private java.util.regex.Pattern pattern;

	@Override
	public void initialize(Email emailAnnotation) {
		super.initialize( emailAnnotation );

		Pattern.Flag[] flags = emailAnnotation.flags();
		int intFlag = 0;
		for ( Pattern.Flag flag : flags ) {
			intFlag = intFlag | flag.getValue();
		}

		// we only apply the regexp if there is one to apply
		if ( !".*".equals( emailAnnotation.regexp() ) || emailAnnotation.flags().length > 0 ) {
			try {
				pattern = java.util.regex.Pattern.compile( emailAnnotation.regexp(), intFlag );
			}
			catch (PatternSyntaxException e) {
				throw LOG.getInvalidRegularExpressionException( e );
			}
		}
	}

	@Override
	public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
		if ( value == null ) {
			return true;
		}

		boolean isValid = super.isValid( value, context );
		if ( pattern == null || !isValid ) {
			return isValid;
		}

		Matcher m = pattern.matcher( value );
		return m.matches();
	}
}
