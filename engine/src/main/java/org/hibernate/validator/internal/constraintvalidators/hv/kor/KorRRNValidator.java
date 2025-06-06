/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv.kor;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.kor.KorRRN;
import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.ModUtil;

/**
 * Checks that a given character sequence is a valid RRN.
 *
 * @author Taewoo Kim
 * @see <a href="https://www.law.go.kr/LSW/lsInfoP.do?lsId=008230&ancYnChk=0#0000">Korean Resident Registration Act Implementation Rules</a>
 */
public class KorRRNValidator implements ConstraintValidator<KorRRN, CharSequence> {

	private static final List<Integer> GENDER_DIGIT = List.of( 1, 2, 3, 4 );
	// Check sum weight for ModUtil
	private static final int[] CHECK_SUM_WEIGHT = new int[] { 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2 };
	// index of the digit representing the gender
	private static final int GENDER_DIGIT_INDEX = 6;

	private RRNValidationAlgorithm rrnValidationAlgorithm;

	@Override
	public void initialize(KorRRN constraintAnnotation) {
		this.rrnValidationAlgorithm = RRNValidationAlgorithm.from( constraintAnnotation.validateCheckDigit() );
	}

	@Override
	public boolean isValid(CharSequence rrnValue, ConstraintValidatorContext context) {
		if ( rrnValue == null ) {
			return true;
		}
		return rrnValidationAlgorithm.isValid( rrnValue.toString().replace( "-", "" ) );
	}

	private enum RRNValidationAlgorithm {
		NEVER {
			@Override
			public boolean isValid(String rrn) {
				return isValidLength( rrn ) && isValidDate( rrn ) && isValidGenderDigit( rrn );
			}
		},
		ALWAYS {
			@Override
			public boolean isValid(String rrn) {
				return isValidLength( rrn ) && isValidDate( rrn ) && isValidGenderDigit( rrn ) && isValidChecksum( rrn );
			}
		};

		// Returns an implementation of the algorithm based on the value of ValidateCheckDigit
		static RRNValidationAlgorithm from(KorRRN.ValidateCheckDigit validateCheckDigit) {
			Contracts.assertNotNull( validateCheckDigit );
			if ( validateCheckDigit == KorRRN.ValidateCheckDigit.ALWAYS ) {
				return RRNValidationAlgorithm.ALWAYS;
			}
			return RRNValidationAlgorithm.NEVER;
		}

		abstract boolean isValid(String rrn);

		private static final int VALID_LENGTH = 13;
		private static final int THRESHOLD = 9;
		private static final int MODULDO = 11;

		// Check the check-digit of the RRN using ModUtil
		private static boolean isValidChecksum(final String rrn) {
			int checksum = ModUtil.calculateModXCheckWithWeights(
					toChecksumDigits( rrn ),
					MODULDO,
					THRESHOLD,
					CHECK_SUM_WEIGHT
			);
			checksum = checksum >= 10 ? checksum - 10 : checksum;
			return checksum == getCheckDigit( rrn );
		}

		private static boolean isValidDate(final String rrn) {
			final int month = extractMonth( rrn );
			final int day = extractDay( rrn );
			if ( month < 1 || month > 12 || day < 1 || day > 31 ) {
				return false;
			}
			return day <= 31 && ( day <= 30 || ( month != 4 && month != 6 && month != 9 && month != 11 ) ) && ( day <= 29 || month != 2 );
		}

		private static boolean isValidLength(String rrn) {
			return rrn.length() == VALID_LENGTH;
		}

		private static boolean isValidGenderDigit(String rrn) {
			return GENDER_DIGIT.contains( extractGenderDigit( rrn ) );
		}

		private static int extractGenderDigit(String rrn) {
			return Character.getNumericValue( rrn.charAt( GENDER_DIGIT_INDEX ) );
		}

		private static List<Integer> toChecksumDigits(String rrn) {
			List<Integer> collect = new ArrayList<>();
			for ( int i = 0; i < rrn.length() - 1; i++ ) {
				collect.add( Character.getNumericValue( rrn.charAt( i ) ) );
			}
			return collect;
		}

		private static int getCheckDigit(String rrn) {
			return Character.getNumericValue( rrn.charAt( rrn.length() - 1 ) );
		}

		private static int extractDay(String rrn) {
			return Integer.parseInt( rrn.substring( 4, 6 ) );
		}

		private static int extractMonth(String rrn) {
			return Integer.parseInt( rrn.substring( 2, 4 ) );
		}
	}
}
