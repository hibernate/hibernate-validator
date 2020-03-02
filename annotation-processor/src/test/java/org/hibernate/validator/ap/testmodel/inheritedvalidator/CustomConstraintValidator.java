/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.testmodel.inheritedvalidator;

import jakarta.validation.ConstraintValidatorContext;

public class CustomConstraintValidator extends AbstractCustomConstraintValidator {

	@Override
	public boolean isValid(String object, ConstraintValidatorContext constraintContext) {
		return true;
	}
}
