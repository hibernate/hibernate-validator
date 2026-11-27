package org.hibernate.validator.internal.constraintvalidators.hv;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.Even;

/**
 * @author Koen Aers
 */
public class EvenValidatorForByte implements ConstraintValidator<Even, Byte> {

	@Override
	public boolean isValid(Byte value, ConstraintValidatorContext context) {
		if ( value == null ) {
			return true;
		}
		return value % 2 == 0;
	}
}
