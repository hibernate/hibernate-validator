package org.hibernate.validator.internal.constraintvalidators.hv;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.SingleLine;

/**
 * Checks that a character sequence contains no line break characters.
 *
 * @since 9.2
 */
public class SingleLineValidator implements ConstraintValidator<SingleLine, CharSequence> {

	@Override
	public boolean isValid(CharSequence value, ConstraintValidatorContext constraintValidatorContext) {
		if ( value == null ) {
			return true;
		}
		for ( int i = 0; i < value.length(); i++ ) {
			char c = value.charAt( i );
			if ( c == '\n' || c == '\r' || c == '\u0085' || c == '\u2028' || c == '\u2029' ) {
				return false;
			}
		}
		return true;
	}
}
