package org.hibernate.validator.internal.constraintvalidators.hv;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.Finite;

/**
 * Checks that a {@link Float} is finite.
 *
 * @since 9.2
 */
public class FiniteValidatorForFloat implements ConstraintValidator<Finite, Float> {

	@Override
	public boolean isValid(Float value, ConstraintValidatorContext context) {
		if ( value == null ) {
			return true;
		}
		return Float.isFinite( value );
	}
}
