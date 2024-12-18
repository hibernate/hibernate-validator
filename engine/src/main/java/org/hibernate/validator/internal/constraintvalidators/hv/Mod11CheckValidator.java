/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv;

import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.List;

import jakarta.validation.ConstraintValidator;

import org.hibernate.validator.constraints.Mod11Check;
import org.hibernate.validator.constraints.Mod11Check.ProcessingDirection;
import org.hibernate.validator.internal.util.ModUtil;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * Mod11 Check Digit validator
 *
 * http://en.wikipedia.org/wiki/Check_digit
 *
 * @author George Gastaldi
 * @author Hardy Ferentschik
 * @author Victor Rezende dos Santos
 */
public class Mod11CheckValidator extends ModCheckBase
		implements ConstraintValidator<Mod11Check, CharSequence> {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private boolean reverseOrder;

	/**
	 * The {@code char} that represents the check digit when mod11
	 * checksum equals 10.
	 */
	private char treatCheck10As;

	/**
	 * The {@code char} that represents the check digit when mod11
	 * checksum equals 10.
	 */
	private char treatCheck11As;

	/**
	 * The threshold for the algorithm multiplier multiplier growth
	 */
	private int threshold;

	/**
	 * An array of custom weights to be used in the sum
	 */
	private int[] customWeights;

	@Override
	public void initialize(Mod11Check constraintAnnotation) {
		initialize(
				constraintAnnotation.startIndex(),
				constraintAnnotation.endIndex(),
				constraintAnnotation.checkDigitIndex(),
				constraintAnnotation.ignoreNonDigitCharacters(),
				constraintAnnotation.threshold(),
				constraintAnnotation.treatCheck10As(),
				constraintAnnotation.treatCheck11As(),
				constraintAnnotation.processingDirection()
		);
	}

	public void initialize(
			int startIndex,
			int endIndex,
			int checkDigitIndex,
			boolean ignoreNonDigitCharacters,
			int threshold,
			char treatCheck10As,
			char treatCheck11As,
			ProcessingDirection direction,
			int... customWeights
	) {

		super.initialize(
				startIndex,
				endIndex,
				checkDigitIndex,
				ignoreNonDigitCharacters
		);
		this.threshold = threshold;
		this.reverseOrder = direction == ProcessingDirection.LEFT_TO_RIGHT;

		this.treatCheck10As = treatCheck10As;
		this.treatCheck11As = treatCheck11As;

		this.customWeights = customWeights;

		if ( !Character.isLetterOrDigit( this.treatCheck10As ) ) {
			throw LOG.getTreatCheckAsIsNotADigitNorALetterException( this.treatCheck10As );
		}

		if ( !Character.isLetterOrDigit( this.treatCheck11As ) ) {
			throw LOG.getTreatCheckAsIsNotADigitNorALetterException( this.treatCheck11As );
		}
	}

	/**
	 * Validate check digit using Mod11 checksum
	 *
	 * @param digits The digits over which to calculate the checksum
	 * @param checkDigit the check digit
	 *
	 * @return {@code true} if the mod11 result matches the check digit, {@code false} otherwise
	 */
	@Override
	public boolean isCheckDigitValid(List<Integer> digits, char checkDigit) {
		if ( reverseOrder ) {
			Collections.reverse( digits );
		}

		int modResult = ModUtil.calculateModXCheckWithWeights( digits, 11, this.threshold, customWeights );
		switch ( modResult ) {
			case 10:
				return checkDigit == this.treatCheck10As;
			case 11:
				return checkDigit == this.treatCheck11As;
			default:
				return Character.isDigit( checkDigit ) && modResult == extractDigit( checkDigit );
		}
	}

}
