/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv.pattern;

/**
 * Validates that given char sequence matches given regexp.
 *
 * @author Hardy Ferentschik
 */
public class PatternValidatorForCharSequence extends BasePatternValidator<CharSequence> {

	@Override
	protected CharSequence getCharSequenceRepresentation(CharSequence value) {
		return value;
	}
}
