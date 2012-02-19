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
import javax.validation.constraints.Min;

/**
 * Check that the character sequence (e.g. string) being validated represents a number, and has a value
 * more than or equal to the minimum value specified.
 *
 * @author Alaa Nassef
 * @author Kevin Pollet <kevin.pollet@serli.com> (C) 2011 SERLI
 */
public class MinValidatorForCharSequence implements ConstraintValidator<Min, CharSequence> {

	private BigDecimal minValue;

	public void initialize(Min minValue) {
		this.minValue = BigDecimal.valueOf( minValue.value() );
	}

	public boolean isValid(CharSequence value, ConstraintValidatorContext constraintValidatorContext) {
		//null values are valid
		if ( value == null ) {
			return true;
		}
		try {
			return new BigDecimal( value.toString() ).compareTo( minValue ) != -1;
		}
		catch ( NumberFormatException nfe ) {
			return false;
		}
	}
}
