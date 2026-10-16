/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.EAN;

/**
 * Checks that a given character sequence (e.g. string) is a valid EAN barcode.
 *
 * @author Hardy Ferentschik
 */
public class EANValidator implements ConstraintValidator<EAN, CharSequence> {

	private int size;

	@Override
	public void initialize(EAN constraintAnnotation) {
		switch ( constraintAnnotation.type() ) {
			case EAN8: {
				size = 8;
				break;
			}
			case EAN13: {
				size = 13;
				break;
			}
		}
	}

	@Override
	public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
		if ( value == null ) {
			return true;
		}
		int length = value.length();
		if ( length != size ) {
			return false;
		}

		char checkDigit = value.charAt( length - 1 );
		if ( !Character.isDigit( checkDigit ) ) {
			return false;
		}

		int sum = 0;
		boolean multiplyByThree = true;
		for ( int i = length - 2; i >= 0; i-- ) {
			char character = value.charAt( i );
			// Preserve the ASCII-only body normalization of the composing Mod10Check constraint.
			if ( character < '0' || character > '9' ) {
				continue;
			}
			int digit = character - '0';
			sum += digit * ( multiplyByThree ? 3 : 1 );
			multiplyByThree = !multiplyByThree;
		}

		return Character.digit( checkDigit, 10 ) == ( 10 - sum % 10 ) % 10;
	}
}
