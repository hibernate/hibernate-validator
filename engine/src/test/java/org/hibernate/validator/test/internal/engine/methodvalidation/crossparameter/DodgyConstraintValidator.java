/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.methodvalidation.crossparameter;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraintvalidation.SupportedValidationTarget;
import javax.validation.constraintvalidation.ValidationTarget;

/**
 * @author Hardy Ferentschik
 */
@SupportedValidationTarget( value = ValidationTarget.PARAMETERS)
public class DodgyConstraintValidator implements ConstraintValidator<DodgyConstraint, Object[]> {

	@Override
	public boolean isValid(Object[] value, ConstraintValidatorContext context) {
		return false;
	}
}
