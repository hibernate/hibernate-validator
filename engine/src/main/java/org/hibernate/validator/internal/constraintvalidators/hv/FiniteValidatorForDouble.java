package org.hibernate.validator.internal.constraintvalidators.hv;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.Finite;

/**
 * Checks that a {@link Double} is finite.
 *
 * @since 9.2
 */
public class FiniteValidatorForDouble implements ConstraintValidator<Finite, Double> {

	@Override
	public boolean isValid(Double value, ConstraintValidatorContext context) {
		if ( value == null ) {
			return true;
		}
		return Double.isFinite( value );
	}
}
