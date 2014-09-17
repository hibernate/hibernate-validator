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
import java.math.BigInteger;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.DecimalMin;

import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * Check that the number being validated is less than or equal to the maximum
 * value specified.
 *
 * @author Hardy Ferentschik
 * @author Xavier Sosnovsky
 */
public class DecimalMinValidatorForNumber implements ConstraintValidator<DecimalMin, Number> {

	private static final Log log = LoggerFactory.make();

	private BigDecimal minValue;
	private boolean inclusive;

	public void initialize(DecimalMin minValue) {
		try {
			this.minValue = new BigDecimal( minValue.value() );
		}
		catch ( NumberFormatException nfe ) {
			throw log.getInvalidBigDecimalFormatException( minValue.value(), nfe );
		}
		this.inclusive = minValue.inclusive();
	}

	public boolean isValid(Number value, ConstraintValidatorContext constraintValidatorContext) {

		// null values are valid
		if ( value == null ) {
			return true;
		}

		// handling of NaN, positive infinity and negative infinity
		else if ( value instanceof Double ) {
			if ( (Double) value == Double.POSITIVE_INFINITY ) {
				return true;
			}
			else if ( Double.isNaN( (Double) value ) || (Double) value == Double.NEGATIVE_INFINITY ) {
				return false;
			}
		}
		else if ( value instanceof Float ) {
			if ( (Float) value == Float.POSITIVE_INFINITY ) {
				return true;
			}
			else if ( Float.isNaN( (Float) value ) || (Float) value == Float.NEGATIVE_INFINITY ) {
				return false;
			}
		}

		int comparisonResult;
		if ( value instanceof BigDecimal ) {
			comparisonResult = ( (BigDecimal) value ).compareTo( minValue );
		}
		else if ( value instanceof BigInteger ) {
			comparisonResult = ( new BigDecimal( (BigInteger) value ) ).compareTo( minValue );
		}
		else if ( value instanceof Long ) {
			comparisonResult = ( BigDecimal.valueOf( value.longValue() ).compareTo( minValue ) );
		}
		else {
			comparisonResult = ( BigDecimal.valueOf( value.doubleValue() ).compareTo( minValue ) );
		}
		return inclusive ? comparisonResult >= 0 : comparisonResult > 0;
	}
}
