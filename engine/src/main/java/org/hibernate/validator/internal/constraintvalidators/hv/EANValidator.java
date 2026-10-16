/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.EAN;
import org.hibernate.validator.internal.util.DigitUtil;
import org.hibernate.validator.internal.util.ModUtil;

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

		int checkDigit = DigitUtil.getDecimalDigit( value.charAt( length - 1 ) );
		// Preserve the ASCII-only body normalization of the former composing Mod10Check constraint.
		return checkDigit >= 0 && checkDigit == ModUtil.calculateMod10Check( value, 0, length - 1, true, 3, 1 );
	}
}
