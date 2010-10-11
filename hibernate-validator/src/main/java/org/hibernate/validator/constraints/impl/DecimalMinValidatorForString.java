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
import javax.validation.constraints.DecimalMin;

/**
 * @author Hardy Ferentschik
 */
public class DecimalMinValidatorForString implements ConstraintValidator<DecimalMin, String> {

	private BigDecimal minValue;

	public void initialize(DecimalMin minValue) {
		try {
			this.minValue = new BigDecimal( minValue.value() );
		}
		catch ( NumberFormatException nfe ) {
			throw new IllegalArgumentException(
					minValue.value() + " does not represent a valid BigDecimal format", nfe
			);
		}
	}

	public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
		//null values are valid
		if ( value == null ) {
			return true;
		}
		try {
			return new BigDecimal( value ).compareTo( minValue ) != -1;
		}
		catch ( NumberFormatException nfe ) {
			return false;
		}
	}
}
