/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package com.example.cdi;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidNumberValidator implements ConstraintValidator<ValidNumber, String> {

	private ValidNumber validNumber;

	@Override
	public void initialize(ValidNumber validNumber) {
		this.validNumber = validNumber;
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if ( "all9".equals( validNumber.value() ) ) {
			return value.chars().allMatch( c -> c == '9' );
		}

		return true;
	}
}
