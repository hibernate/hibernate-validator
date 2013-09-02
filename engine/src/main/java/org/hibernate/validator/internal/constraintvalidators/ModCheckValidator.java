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

import java.util.List;
import javax.validation.ConstraintValidator;

import org.hibernate.validator.constraints.ModCheck;
import org.hibernate.validator.constraints.ModCheck.ModType;
import org.hibernate.validator.internal.util.ModUtil;

/**
 * Mod check validator for MOD10 and MOD11 algorithms
 *
 * http://en.wikipedia.org/wiki/Luhn_algorithm
 * http://en.wikipedia.org/wiki/Check_digit
 *
 * @author George Gastaldi
 * @author Hardy Ferentschik
 * @deprecated As of release 5.1.0, replaced by {@link Mod10CheckValidator} and {@link Mod11CheckValidator}
 */
@Deprecated
public class ModCheckValidator extends ModCheckBase implements ConstraintValidator<ModCheck, CharSequence> {
	/**
	 * Multiplier used by the mod algorithms
	 */
	private int multiplier;

	/**
	 * The type of checksum algorithm
	 */
	private ModType modType;

	@Override
	public void initialize(ModCheck constraintAnnotation) {
		super.initialize(
				constraintAnnotation.startIndex(),
				constraintAnnotation.endIndex(),
				constraintAnnotation.checkDigitPosition(),
				constraintAnnotation.ignoreNonDigitCharacters()
		);

		this.modType = constraintAnnotation.modType();
		this.multiplier = constraintAnnotation.multiplier();
	}

	/**
	 * Check if the input passes the Mod10 (Luhn algorithm implementation only) or Mod11 test
	 *
	 * @param digits the digits over which to calculate the Mod10 or Mod11 checksum
	 * @param checkDigit the check digit
	 *
	 * @return {@code true} if the mod 10/11 result matches the check digit, {@code false} otherwise
	 */
	@Override
	public boolean isCheckDigitValid(List<Integer> digits, char checkDigit) {
		int modResult = -1;
		int checkValue = extractDigit( checkDigit );

		if ( modType.equals( ModType.MOD11 ) ) {
			modResult = ModUtil.calculateMod11Check( digits, multiplier );

			if ( modResult == 10 || modResult == 11 ) {
				modResult = 0;
			}
		}
		else {
			modResult = ModUtil.calculateLuhnMod10Check( digits );
		}

		return checkValue == modResult;
	}

}
