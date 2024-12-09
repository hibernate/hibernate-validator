/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.hv;

import java.util.Arrays;

import org.hibernate.validator.constraints.UUID;
import org.hibernate.validator.internal.util.Contracts;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import static org.hibernate.validator.constraints.UUID.LetterCase;
import static org.hibernate.validator.internal.util.logging.Messages.MESSAGES;

/**
 * Checks that the annotated character sequence is a valid
 * <a href="https://en.wikipedia.org/wiki/Universally_unique_identifier">UUID</a>.
 *
 * @author Daniel Heid
 */
public class UUIDValidator implements ConstraintValidator<UUID, CharSequence> {

	private static final int[] GROUP_LENGTHS = { 8, 4, 4, 4, 12 };

	private boolean allowEmpty;

	private boolean allowNil;

	private int[] version;

	private int[] variant;

	private LetterCase letterCase;

	@Override
	public void initialize(UUID constraintAnnotation) {
		allowEmpty = constraintAnnotation.allowEmpty();
		allowNil = constraintAnnotation.allowNil();
		version = checkAndSortMultiOptionParameter( constraintAnnotation.version(), "version", 1, 15 );
		variant = checkAndSortMultiOptionParameter( constraintAnnotation.variant(), "variant", 0, 2 );
		letterCase = constraintAnnotation.letterCase();
		Contracts.assertNotNull( letterCase, MESSAGES.parameterMustNotBeNull( "letterCase" ) );
	}

	private static int[] checkAndSortMultiOptionParameter(
			int[] values,
			String parameterName,
			int minimum,
			int maximum) {
		Contracts.assertNotNull( values, MESSAGES.parameterMustNotBeNull( parameterName ) );
		Contracts.assertNotEmpty( values, MESSAGES.parameterMustNotBeEmpty( parameterName ) );
		for ( int value : values ) {
			Contracts.assertTrue(
					value >= minimum,
					MESSAGES.parameterShouldBeGreaterThanOrEqualTo( parameterName, minimum )
			);
			Contracts.assertTrue(
					value <= maximum,
					MESSAGES.parameterShouldBeLessThanOrEqualTo( parameterName, maximum )
			);
		}
		Arrays.sort( values );
		return values;
	}

	@Override
	public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
		if ( value == null ) {
			return true;
		}
		int valueLength = value.length();
		if ( valueLength == 0 ) {
			return allowEmpty;
		}
		else if ( valueLength != 36 ) {
			return false;
		}

		int groupIndex = 0;
		int groupLength = 0;
		int checksum = 0;
		int version = -1;
		int variant = -1;
		for ( int charIndex = 0; charIndex < valueLength; charIndex++ ) {

			char ch = value.charAt( charIndex );

			if ( ch == '-' ) {
				groupIndex++;
				groupLength = 0;
			}
			else {

				groupLength++;
				if ( groupLength > GROUP_LENGTHS[groupIndex] ) {
					return false;
				}

				int numericValue = Character.digit( ch, 16 );
				if ( numericValue == -1 ) {
					// not a hex digit
					return false;
				}
				if ( numericValue > 9 && !hasCorrectLetterCase( ch ) ) {
					return false;
				}
				checksum += numericValue;
				version = extractVersion( version, charIndex, numericValue );
				variant = extractVariant( variant, charIndex, numericValue );

			}

		}

		if ( checksum == 0 ) {
			return allowNil;
		}
		else {
			if ( Arrays.binarySearch( this.version, version ) < 0 ) {
				return false;
			}
			return Arrays.binarySearch( this.variant, variant ) > -1;
		}
	}

	/**
	 * Validates the letter case of the given character depending on the letter case parameter
	 *
	 * @param ch The letter to be tested
	 *
	 * @return {@code true} if the character is in the specified letter case or letter case is not specified
	 */
	private boolean hasCorrectLetterCase(char ch) {
		if ( letterCase == null ) {
			return true;
		}
		if ( letterCase == LetterCase.LOWER_CASE && !Character.isLowerCase( ch ) ) {
			return false;
		}
		return letterCase != LetterCase.UPPER_CASE || Character.isUpperCase( ch );
	}

	/**
	 * Get the 4 bit UUID version from the current value
	 *
	 * @param version The old version (in case the version has already been extracted)
	 * @param index The index of the current value to find the version to extract
	 * @param value The numeric value at the character position
	 */
	private static int extractVersion(int version, int index, int value) {
		if ( index == 14 ) {
			return value;
		}
		return version;
	}

	/**
	 * Get the 3 bit UUID variant from the current value
	 *
	 * @param variant The old variant (in case the variant has already been extracted)
	 * @param index The index of the current value to find the variant to extract
	 * @param value The numeric value at the character position
	 */
	private static int extractVariant(int variant, int index, int value) {
		if ( index == 19 ) {
			// 0xxx
			if ( value >> 3 == 0 ) {
				return 0;
			}
			// 10xx
			if ( value >> 2 == 2 ) {
				return 1;
			}
			// 110x
			if ( value >> 1 == 6 ) {
				return 2;
			}
		}
		return variant;
	}

}
