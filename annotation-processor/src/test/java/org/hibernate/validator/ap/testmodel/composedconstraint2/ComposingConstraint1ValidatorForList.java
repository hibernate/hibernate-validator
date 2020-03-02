/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.testmodel.composedconstraint2;

import java.util.List;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ComposingConstraint1ValidatorForList implements ConstraintValidator<ComposingConstraint1, List<?>> {
	@Override
	public void initialize(ComposingConstraint1 constraintAnnotation) {

	}

	@Override
	public boolean isValid(List<?> object, ConstraintValidatorContext constraintContext) {
		return true;
	}
}
