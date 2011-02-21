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
package org.hibernate.validator.constraints.impl;

import java.math.BigDecimal;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.Digits;

/**
 * Validates that the <code>String</code> being validated consists of digits,
 * and matches the pattern defined in the constraint.
 *
 * @author Alaa Nassef
 * @author Hardy Ferentschik
 */
public class DigitsValidatorForString implements ConstraintValidator<Digits, String> {

	private int maxIntegerLength;
	private int maxFractionLength;

	public void initialize(Digits constraintAnnotation) {
		this.maxIntegerLength = constraintAnnotation.integer();
		this.maxFractionLength = constraintAnnotation.fraction();
		validateParameters();
	}

	public boolean isValid(String str, ConstraintValidatorContext constraintValidatorContext) {
		//null values are valid
		if ( str == null ) {
			return true;
		}

		BigDecimal bigNum = getBigDecimalValue( str );
		if ( bigNum == null ) {
			return false;
		}

		int integerPartLength = bigNum.precision() - bigNum.scale();
		int fractionPartLength = bigNum.scale() < 0 ? 0 : bigNum.scale();

		return ( maxIntegerLength >= integerPartLength && maxFractionLength >= fractionPartLength );
	}

	private BigDecimal getBigDecimalValue(String str) {
		BigDecimal bd;
		try {
			bd = new BigDecimal( str );
		}
		catch ( NumberFormatException nfe ) {
			return null;
		}
		return bd;
	}

	private void validateParameters() {
		if ( maxIntegerLength < 0 ) {
			throw new IllegalArgumentException( "The length of the integer part cannot be negative." );
		}
		if ( maxFractionLength < 0 ) {
			throw new IllegalArgumentException( "The length of the fraction part cannot be negative." );
		}
	}
}
