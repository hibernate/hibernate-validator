/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraints.NotNull;

/**
 * Validate that the object is not {@code null}.
 *
 * @author Emmanuel Bernard
 */
public class NotNullValidator implements ConstraintValidator<NotNull, Object> {

	@Override
	public boolean isValid(Object object, ConstraintValidatorContext constraintValidatorContext) {
		return object != null;
	}
}
