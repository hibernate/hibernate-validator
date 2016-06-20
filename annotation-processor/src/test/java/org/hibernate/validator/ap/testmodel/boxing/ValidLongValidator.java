/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.testmodel.boxing;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ValidLongValidator implements ConstraintValidator<ValidLong, Long> {
	@Override
	public void initialize(ValidLong constraintAnnotation) {
	}

	@Override
	public boolean isValid(Long object, ConstraintValidatorContext constraintContext) {
		return true;
	}
}
