/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv.pattern;

/**
 * Validates that given number matches given regexp.
 *
 * @author Marko Bekhta
 */
public class PatternValidatorForNumber extends BasePatternValidator<Number> {

	@Override
	protected CharSequence getCharSequenceRepresentation(Number value) {
		return value.toString();
	}
}
