package org.hibernate.validator.internal.constraintvalidators.hv;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.Even;

/**
 * @author Koen Aers
 */
public class EvenValidatorForInteger implements ConstraintValidator<Even, Integer> {

	@Override
	public boolean isValid(Integer value, ConstraintValidatorContext context) {
		if ( value == null ) {
			return true;
		}
		return value % 2 == 0;
	}
}
