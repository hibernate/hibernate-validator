/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv.pattern;

import java.util.regex.Matcher;
import javax.validation.ConstraintValidatorContext;

/**
 * Validates that given char sequence matches given regexp.
 *
 * @author Hardy Ferentschik
 */
public class PatternValidatorForCharSequence extends BasePatternValidator<CharSequence> {

	@Override
	public boolean isValid(CharSequence value, ConstraintValidatorContext constraintValidatorContext) {
		if ( value == null ) {
			return true;
		}
		Matcher m = pattern.matcher( value );
		return m.matches();
	}
}
