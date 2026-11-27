package org.hibernate.validator.internal.constraintvalidators.hv;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.Odd;

/**
 * @author Koen Aers
 */
public class OddValidatorForInteger implements ConstraintValidator<Odd, Integer> {

	@Override
	public boolean isValid(Integer value, ConstraintValidatorContext context) {
		if ( value == null ) {
			return true;
		}
		return value % 2 != 0;
	}
}
