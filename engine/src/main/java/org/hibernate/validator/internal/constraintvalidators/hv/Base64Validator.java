package org.hibernate.validator.internal.constraintvalidators.hv;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.Base64;

/**
 * Checks that the character sequence is a valid Base64 encoded string, using either the basic or the
 * URL and Filename safe alphabet of RFC 4648.
 *
 * @author Andrea Boriero
 */
public class Base64Validator implements ConstraintValidator<Base64, CharSequence> {

	private java.util.Base64.Decoder decoder;

	@Override
	public void initialize(Base64 constraintAnnotation) {
		decoder = constraintAnnotation.urlSafe()
				? java.util.Base64.getUrlDecoder()
				: java.util.Base64.getDecoder();
	}

	@Override
	public boolean isValid(CharSequence value, ConstraintValidatorContext constraintValidatorContext) {
		if ( value == null ) {
			return true;
		}

		try {
			decoder.decode( value.toString() );
			return true;
		}
		catch (IllegalArgumentException e) {
			return false;
		}
	}
}
