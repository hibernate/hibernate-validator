/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.hv.ru;

import java.util.regex.Pattern;

import org.hibernate.validator.constraints.ru.INN;
import org.hibernate.validator.internal.util.Contracts;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Checks that a given character sequence (e.g. string) is a valid INN.
 *
 * @author Artem Boiarshinov
 */
public class INNValidator implements ConstraintValidator<INN, CharSequence> {

	private static final Pattern NUMBERS_ONLY_PATTERN = Pattern.compile( "[0-9]+" );
	private static final int RADIX = 10;

	private INNValidationAlgorithm innValidationAlgorithm;

	@Override
	public void initialize(INN constraintAnnotation) {
		this.innValidationAlgorithm = INNValidationAlgorithm.from( constraintAnnotation.type() );
	}

	@Override
	public boolean isValid(CharSequence innCharSeq, ConstraintValidatorContext context) {
		if ( innCharSeq == null ) {
			return true;
		}

		final String inn = innCharSeq.toString();

		final boolean hasOnlyNumbers = NUMBERS_ONLY_PATTERN.matcher( inn ).matches();
		if ( !hasOnlyNumbers ) {
			return false;
		}

		if ( !innValidationAlgorithm.isValidLength( inn.length() ) ) {
			return false;
		}

		final int[] digits = inn.codePoints().map( symbol -> Character.digit( symbol, RADIX ) ).toArray();

		return innValidationAlgorithm.isValidChecksum( digits );

	}

	private interface INNValidationAlgorithm {
		boolean isValidLength(int length);

		boolean isValidChecksum(int[] digits);

		static INNValidationAlgorithm from(org.hibernate.validator.constraints.ru.INN.Type type) {
			Contracts.assertNotNull( type );
			switch ( type ) {
				case JURIDICAL:
					return INNValidationAlgorithmImpl.JURIDICAL;
				case INDIVIDUAL:
					return INNValidationAlgorithmImpl.INDIVIDUAL;
				case ANY:
				default:
					return INNValidationAlgorithmImpl.ANY;
			}
		}
	}

	private enum INNValidationAlgorithmImpl implements INNValidationAlgorithm {

		INDIVIDUAL {
			@Override
			public boolean isValidLength(int length) {
				return 12 == length;
			}

			@Override
			public boolean isValidChecksum(int[] digits) {
				return checkChecksumPersonalINN( digits );
			}
		},
		JURIDICAL {
			@Override
			public boolean isValidLength(int length) {
				return 10 == length;
			}

			@Override
			public boolean isValidChecksum(int[] digits) {
				return checkChecksumJuridicalINN( digits );
			}
		},
		ANY {
			@Override
			public boolean isValidLength(int length) {
				return 10 == length || 12 == length;
			}

			@Override
			public boolean isValidChecksum(int[] digits) {
				final int length = digits.length;
				if ( length == 12 ) {
					return checkChecksumPersonalINN( digits );
				}
				else if ( length == 10 ) {
					return checkChecksumJuridicalINN( digits );
				}
				throw new IllegalStateException( "Invalid/unsupported inn value length" );
			}
		};

		private static final int[] INDIVIDUAL_WEIGHTS_11 = { 7, 2, 4, 10, 3, 5, 9, 4, 6, 8 };
		private static final int[] INDIVIDUAL_WEIGHTS_12 = { 3, 7, 2, 4, 10, 3, 5, 9, 4, 6, 8 };
		private static final int[] JURIDICAL_WEIGHTS = { 2, 4, 10, 3, 5, 9, 4, 6, 8 };

		private static final int MOD_11 = 11;
		private static final int MOD_10 = 10;

		/**
		 * Check the digits for personal INN using algorithm from
		 * <a href="https://ru.wikipedia.org/wiki/%D0%98%D0%B4%D0%B5%D0%BD%D1%82%D0%B8%D1%84%D0%B8%D0%BA%D0%B0%D1%86%D0%B8%D0%BE%D0%BD%D0%BD%D1%8B%D0%B9_%D0%BD%D0%BE%D0%BC%D0%B5%D1%80_%D0%BD%D0%B0%D0%BB%D0%BE%D0%B3%D0%BE%D0%BF%D0%BB%D0%B0%D1%82%D0%B5%D0%BB%D1%8C%D1%89%D0%B8%D0%BA%D0%B0#%D0%92%D1%8B%D1%87%D0%B8%D1%81%D0%BB%D0%B5%D0%BD%D0%B8%D0%B5_%D0%BA%D0%BE%D0%BD%D1%82%D1%80%D0%BE%D0%BB%D1%8C%D0%BD%D1%8B%D1%85_%D1%86%D0%B8%D1%84%D1%80">Wikipedia</a>.
		 */
		private static boolean checkChecksumPersonalINN(int[] digits) {
			final int checkSum11 = getCheckSum( digits, INDIVIDUAL_WEIGHTS_11 );
			final int checkSum12 = getCheckSum( digits, INDIVIDUAL_WEIGHTS_12 );

			final boolean isCheckSum11Correct = checkSum11 == digits[digits.length - 2];
			final boolean isCheckSum12Correct = checkSum12 == digits[digits.length - 1];

			return isCheckSum11Correct && isCheckSum12Correct;
		}

		/**
		 * Check the digits for juridical INN using algorithm from
		 * <a href="https://ru.wikipedia.org/wiki/%D0%98%D0%B4%D0%B5%D0%BD%D1%82%D0%B8%D1%84%D0%B8%D0%BA%D0%B0%D1%86%D0%B8%D0%BE%D0%BD%D0%BD%D1%8B%D0%B9_%D0%BD%D0%BE%D0%BC%D0%B5%D1%80_%D0%BD%D0%B0%D0%BB%D0%BE%D0%B3%D0%BE%D0%BF%D0%BB%D0%B0%D1%82%D0%B5%D0%BB%D1%8C%D1%89%D0%B8%D0%BA%D0%B0#%D0%92%D1%8B%D1%87%D0%B8%D1%81%D0%BB%D0%B5%D0%BD%D0%B8%D0%B5_%D0%BA%D0%BE%D0%BD%D1%82%D1%80%D0%BE%D0%BB%D1%8C%D0%BD%D1%8B%D1%85_%D1%86%D0%B8%D1%84%D1%80">Wikipedia</a>.
		 */
		private static boolean checkChecksumJuridicalINN(int[] digits) {
			final int checkSum = getCheckSum( digits, JURIDICAL_WEIGHTS );
			return digits[digits.length - 1] == checkSum;
		}

		private static int getCheckSum(int[] digits, int[] weights) {
			int sum = 0;
			for ( int i = 0; i < weights.length; i++ ) {
				sum += digits[i] * weights[i];
			}
			return  ( sum % MOD_11 ) % MOD_10;
		}
	}
}
