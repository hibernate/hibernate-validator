/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv;

import java.math.BigDecimal;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.DecimalMin;

import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * @author Hardy Ferentschik
 */
public class DecimalMinValidatorForCharSequence implements ConstraintValidator<DecimalMin, CharSequence> {

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

	public boolean isValid(CharSequence value, ConstraintValidatorContext constraintValidatorContext) {
		//null values are valid
		if ( value == null ) {
			return true;
		}
		try {
			int comparisonResult = new BigDecimal( value.toString() ).compareTo( minValue );
			return inclusive ? comparisonResult >= 0 : comparisonResult > 0;
		}
		catch ( NumberFormatException nfe ) {
			return false;
		}
	}
}
