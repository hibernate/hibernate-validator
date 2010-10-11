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
import javax.validation.constraints.Max;

/**
 * Check that the String being validated represents a number, and has a value
 * less than or equal to the maximum value specified.
 *
 * @author Alaa Nassef
 */
public class MaxValidatorForString implements ConstraintValidator<Max, String> {

	private long maxValue;

	public void initialize(Max maxValue) {
		this.maxValue = maxValue.value();
	}

	public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
		//null values are valid
		if ( value == null ) {
			return true;
		}
		try {
			return new BigDecimal( value ).compareTo( BigDecimal.valueOf( maxValue ) ) != 1;
		}
		catch ( NumberFormatException nfe ) {
			return false;
		}
	}
}
