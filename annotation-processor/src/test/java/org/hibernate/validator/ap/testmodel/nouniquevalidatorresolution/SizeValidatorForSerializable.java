/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.testmodel.nouniquevalidatorresolution;

import java.io.Serializable;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class SizeValidatorForSerializable implements ConstraintValidator<Size, Serializable> {

	@Override
	public void initialize(Size constraintAnnotation) {
	}

	@Override
	public boolean isValid(Serializable object, ConstraintValidatorContext constraintContext) {
		return true;
	}
}
