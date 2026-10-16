/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv.br;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.br.RENAVAM;
import org.hibernate.validator.internal.util.DigitUtil;
import org.hibernate.validator.internal.util.ModUtil;

/**
 * Validates current and legacy RENAVAM representations.
 */
public class RENAVAMValidator implements ConstraintValidator<RENAVAM, CharSequence> {

	@Override
	public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
		if ( value == null ) {
			return true;
		}

		int length = value.length();
		if ( length != 9 && length != 11 ) {
			return false;
		}

		int checkDigit = DigitUtil.getAsciiDigit( value.charAt( length - 1 ) );
		if ( checkDigit < 0 ) {
			return false;
		}
		int complement = ModUtil.calculateMod11Check( value, 0, length - 1, 9 );
		return complement >= 0 && checkDigit == ModUtil.toZeroMappedMod11CheckDigit( complement );
	}
}
