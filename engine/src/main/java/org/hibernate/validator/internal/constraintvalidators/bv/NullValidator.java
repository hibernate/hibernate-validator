/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraints.Null;

/**
 * Validate that the object is {@code null}
 *
 * @author Alaa Nassef
 */
public class NullValidator implements ConstraintValidator<Null, Object> {

	@Override
	public boolean isValid(Object object, ConstraintValidatorContext constraintValidatorContext) {
		return object == null;
	}

}
