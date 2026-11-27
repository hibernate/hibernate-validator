package org.hibernate.validator.internal.constraintvalidators.hv;

import java.math.BigInteger;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.Even;

/**
 * @author Koen Aers
 */
public class EvenValidatorForBigInteger implements ConstraintValidator<Even, BigInteger> {

	@Override
	public boolean isValid(BigInteger value, ConstraintValidatorContext context) {
		if ( value == null ) {
			return true;
		}
		return value.mod( BigInteger.TWO ).equals( BigInteger.ZERO );
	}
}
