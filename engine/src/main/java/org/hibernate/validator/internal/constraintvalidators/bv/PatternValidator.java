/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv;

import java.util.regex.Matcher;
import java.util.regex.PatternSyntaxException;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.Pattern;

import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * @author Hardy Ferentschik
 */
public class PatternValidator implements ConstraintValidator<Pattern, CharSequence> {

	private static final Log log = LoggerFactory.make();

	private java.util.regex.Pattern pattern;

	public void initialize(Pattern parameters) {
		Pattern.Flag[] flags = parameters.flags();
		int intFlag = 0;
		for ( Pattern.Flag flag : flags ) {
			intFlag = intFlag | flag.getValue();
		}

		try {
			pattern = java.util.regex.Pattern.compile( parameters.regexp(), intFlag );
		}
		catch ( PatternSyntaxException e ) {
			throw log.getInvalidRegularExpressionException( e );
		}
	}

	public boolean isValid(CharSequence value, ConstraintValidatorContext constraintValidatorContext) {
		if ( value == null ) {
			return true;
		}
		Matcher m = pattern.matcher( value );
		return m.matches();
	}
}
