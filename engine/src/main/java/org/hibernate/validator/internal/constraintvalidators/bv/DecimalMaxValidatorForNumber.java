/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv;

import java.math.BigDecimal;
import java.math.BigInteger;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.DecimalMax;

import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * Check that the number being validated is less than or equal to the maximum
 * value specified.
 *
 * @author Hardy Ferentschik
 * @author Xavier Sosnovsky
 */
public class DecimalMaxValidatorForNumber implements ConstraintValidator<DecimalMax, Number> {

	private static final Log log = LoggerFactory.make();

	private BigDecimal maxValue;
	private boolean inclusive;

	@Override
	public void initialize(DecimalMax maxValue) {
		try {
			this.maxValue = new BigDecimal( maxValue.value() );
		}
		catch (NumberFormatException nfe) {
			throw log.getInvalidBigDecimalFormatException( maxValue.value(), nfe );
		}
		this.inclusive = maxValue.inclusive();
	}

	@Override
	public boolean isValid(Number value, ConstraintValidatorContext constraintValidatorContext) {
		// null values are valid
		if ( value == null ) {
			return true;
		}

		// handling of NaN, positive infinity and negative infinity
		else if ( value instanceof Double ) {
			if ( (Double) value == Double.NEGATIVE_INFINITY ) {
				return true;
			}
			else if ( Double.isNaN( (Double) value ) || (Double) value == Double.POSITIVE_INFINITY ) {
				return false;
			}
		}
		else if ( value instanceof Float ) {
			if ( (Float) value == Float.NEGATIVE_INFINITY ) {
				return true;
			}
			else if ( Float.isNaN( (Float) value ) || (Float) value == Float.POSITIVE_INFINITY ) {
				return false;
			}
		}

		int comparisonResult;
		if ( value instanceof BigDecimal ) {
			comparisonResult = ( (BigDecimal) value ).compareTo( maxValue );
		}
		else if ( value instanceof BigInteger ) {
			comparisonResult = ( new BigDecimal( (BigInteger) value ) ).compareTo( maxValue );
		}
		else if ( value instanceof Long ) {
			comparisonResult = ( BigDecimal.valueOf( value.longValue() ).compareTo( maxValue ) );
		}
		else {
			comparisonResult = ( BigDecimal.valueOf( value.doubleValue() ).compareTo( maxValue ) );
		}
		return inclusive ? comparisonResult <= 0 : comparisonResult < 0;
	}
}
