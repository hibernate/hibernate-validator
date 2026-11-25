package org.hibernate.validator.internal.constraintvalidators.hv;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.Odd;

/**
 * @author Koen Aers
 */
public class OddValidatorForLong implements ConstraintValidator<Odd, Long> {

	@Override
	public boolean isValid(Long value, ConstraintValidatorContext context) {
		if ( value == null ) {
			return true;
		}
		return value % 2 != 0;
	}
}
