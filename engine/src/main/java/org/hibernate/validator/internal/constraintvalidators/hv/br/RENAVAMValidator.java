/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv.br;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.br.RENAVAM;

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

		char checkDigit = value.charAt( length - 1 );
		if ( checkDigit < '0' || checkDigit > '9' ) {
			return false;
		}

		int sum = 0;
		int weight = 2;
		for ( int i = length - 2; i >= 0; i-- ) {
			char character = value.charAt( i );
			if ( character < '0' || character > '9' ) {
				return false;
			}
			sum += ( character - '0' ) * weight;
			// The legacy and current formats both use weights cycling from two through nine.
			weight = weight == 9 ? 2 : weight + 1;
		}

		int remainder = sum % 11;
		return checkDigit - '0' == ( remainder < 2 ? 0 : 11 - remainder );
	}
}
