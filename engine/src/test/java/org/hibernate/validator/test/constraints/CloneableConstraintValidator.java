package org.hibernate.validator.test.constraints;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * @author Hardy Ferentschik
 */
public class CloneableConstraintValidator implements ConstraintValidator<org.hibernate.validator.test.constraints.Cloneable, java.lang.Cloneable> {

	@Override
	public boolean isValid(java.lang.Cloneable value, ConstraintValidatorContext constraintValidatorContext) {
		return true;
	}
}
