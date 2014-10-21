/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.constraintvalidator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;


public class MustMatchValidator implements ConstraintValidator<MustMatch, String> {

	private String match;

	@Override
	public void initialize(MustMatch constraintAnnotation) {
		this.match = constraintAnnotation.value();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if ( value == null ) {
			return true;
		}

		return value.equals( match );
	}
}


