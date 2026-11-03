package org.hibernate.validator.internal.constraintvalidators.hv;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.Numeric;

/**
 * Checks that the character sequence contains only digits.
 *
 * @author Andrea Boriero
 */
public class NumericValidator implements ConstraintValidator<Numeric, CharSequence> {

	@Override
	public boolean isValid(CharSequence value, ConstraintValidatorContext constraintValidatorContext) {
		if ( value == null ) {
			return true;
		}

		// Every character must be a digit
		for ( int i = 0; i < value.length(); i++ ) {
			if ( !Character.isDigit( value.charAt( i ) ) ) {
				return false;
			}
		}
		return true;
	}
}
