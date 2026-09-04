/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.Trimmed;

/**
 * Checks that a character sequence has no leading or trailing whitespace.
 *
 * @since 9.2
 */
public class TrimmedValidator implements ConstraintValidator<Trimmed, CharSequence> {

	@Override
	public boolean isValid(CharSequence value, ConstraintValidatorContext constraintValidatorContext) {
		if ( value == null ) {
			return true;
		}
		int length = value.length();
		if ( length == 0 ) {
			return true;
		}
		// Only the first and last characters can be leading/trailing whitespace, so inspect them
		// directly instead of allocating a stripped copy. Whitespace code points are all in the BMP,
		// so a single char is enough to test each end. This matches String#strip() semantics.
		return !Character.isWhitespace( value.charAt( 0 ) )
				&& !Character.isWhitespace( value.charAt( length - 1 ) );
	}
}
