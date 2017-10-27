/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv;

import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.DecimalMin;

import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * Check that the character sequence (e.g. string) being validated represents a number, and has a value
 * greater than or equal to the minimum value specified.
 *
 * @author Hardy Ferentschik
 */
public class DecimalMinValidatorForCharSequence implements ConstraintValidator<DecimalMin, CharSequence> {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private BigDecimal minValue;
	private boolean inclusive;

	@Override
	public void initialize(DecimalMin minValue) {
		try {
			this.minValue = new BigDecimal( minValue.value() );
		}
		catch (NumberFormatException nfe) {
			throw LOG.getInvalidBigDecimalFormatException( minValue.value(), nfe );
		}
		this.inclusive = minValue.inclusive();
	}

	@Override
	public boolean isValid(CharSequence value, ConstraintValidatorContext constraintValidatorContext) {
		//null values are valid
		if ( value == null ) {
			return true;
		}
		try {
			int comparisonResult = new BigDecimal( value.toString() ).compareTo( minValue );
			return inclusive ? comparisonResult >= 0 : comparisonResult > 0;
		}
		catch (NumberFormatException nfe) {
			return false;
		}
	}
}
