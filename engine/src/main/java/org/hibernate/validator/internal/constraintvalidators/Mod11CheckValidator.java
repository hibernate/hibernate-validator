/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and/or its affiliates, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hibernate.validator.internal.constraintvalidators;

import java.util.Collections;
import java.util.List;
import javax.validation.ConstraintValidator;

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

	private static final Log log = LoggerFactory.make();

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
	 * @return The threshold for the algorithm multiplier multiplier growth
	 */
	private int threshold;

	@Override
	public void initialize(Mod11Check constraintAnnotation) {
		super.initialize(
				constraintAnnotation.startIndex(),
				constraintAnnotation.endIndex(),
				constraintAnnotation.checkDigitIndex(),
				constraintAnnotation.ignoreNonDigitCharacters()
		);
		this.threshold = constraintAnnotation.threshold();

		this.reverseOrder = constraintAnnotation.processingDirection() == ProcessingDirection.LEFT_TO_RIGHT;

		this.treatCheck10As = constraintAnnotation.treatCheck10As();
		this.treatCheck11As = constraintAnnotation.treatCheck11As();

		if ( !Character.isLetterOrDigit( this.treatCheck10As ) ) {
			throw log.getTreatCheckAsIsNotADigitNorALetterException( this.treatCheck10As );
		}

		if ( !Character.isLetterOrDigit( this.treatCheck11As ) ) {
			throw log.getTreatCheckAsIsNotADigitNorALetterException( this.treatCheck11As );
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

		int modResult = ModUtil.calculateMod11Check( digits, this.threshold );
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
