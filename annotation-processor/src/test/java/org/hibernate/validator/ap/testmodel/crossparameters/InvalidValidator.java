/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.testmodel.crossparameters;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraintvalidation.SupportedValidationTarget;
import javax.validation.constraintvalidation.ValidationTarget;

@SupportedValidationTarget(ValidationTarget.PARAMETERS)
public class InvalidValidator implements ConstraintValidator<InvalidValidatorConstraint, String> {

	@Override
	public void initialize(final InvalidValidatorConstraint constraintAnnotation) {
	}

	@Override
	public boolean isValid(final String value, final ConstraintValidatorContext context) {
		// some validation logic
		return true;
	}

}
