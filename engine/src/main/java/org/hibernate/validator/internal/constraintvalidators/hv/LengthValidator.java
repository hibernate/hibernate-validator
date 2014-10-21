/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.hv;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * Check that the character sequence length is between min and max.
 *
 * @author Emmanuel Bernard
 * @author Gavin King
 */
public class LengthValidator implements ConstraintValidator<Length, CharSequence> {

	private static final Log log = LoggerFactory.make();

	private int min;
	private int max;

	public void initialize(Length parameters) {
		min = parameters.min();
		max = parameters.max();
		validateParameters();
	}

	public boolean isValid(CharSequence value, ConstraintValidatorContext constraintValidatorContext) {
		if ( value == null ) {
			return true;
		}
		int length = value.length();
		return length >= min && length <= max;
	}

	private void validateParameters() {
		if ( min < 0 ) {
			throw log.getMinCannotBeNegativeException();
		}
		if ( max < 0 ) {
			throw log.getMaxCannotBeNegativeException();
		}
		if ( max < min ) {
			throw log.getLengthCannotBeNegativeException();
		}
	}
}
