/*
* JBoss, Home of Professional Open Source
* Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual contributors
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

import java.math.BigDecimal;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.Digits;

import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * Validates that the character sequence (e.g. string) being validated consists of digits,
 * and matches the pattern defined in the constraint.
 *
 * @author Alaa Nassef
 * @author Hardy Ferentschik
 */
public class DigitsValidatorForCharSequence implements ConstraintValidator<Digits, CharSequence> {

	private static final Log log = LoggerFactory.make();

	private int maxIntegerLength;
	private int maxFractionLength;

	public void initialize(Digits constraintAnnotation) {
		this.maxIntegerLength = constraintAnnotation.integer();
		this.maxFractionLength = constraintAnnotation.fraction();
		validateParameters();
	}

	public boolean isValid(CharSequence charSequence, ConstraintValidatorContext constraintValidatorContext) {
		//null values are valid
		if ( charSequence == null ) {
			return true;
		}

		BigDecimal bigNum = getBigDecimalValue( charSequence );
		if ( bigNum == null ) {
			return false;
		}

		int integerPartLength = bigNum.precision() - bigNum.scale();
		int fractionPartLength = bigNum.scale() < 0 ? 0 : bigNum.scale();

		return ( maxIntegerLength >= integerPartLength && maxFractionLength >= fractionPartLength );
	}

	private BigDecimal getBigDecimalValue(CharSequence charSequence) {
		BigDecimal bd;
		try {
			bd = new BigDecimal( charSequence.toString() );
		}
		catch ( NumberFormatException nfe ) {
			return null;
		}
		return bd;
	}

	private void validateParameters() {
		if ( maxIntegerLength < 0 ) {
			throw log.getInvalidLengthForIntegerPartException();
		}
		if ( maxFractionLength < 0 ) {
			throw log.getInvalidLengthForFractionPartException();
		}
	}
}
