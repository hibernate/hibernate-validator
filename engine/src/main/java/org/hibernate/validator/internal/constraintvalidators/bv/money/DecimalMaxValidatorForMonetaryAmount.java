/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv.money;

import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import javax.money.MonetaryAmount;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraints.DecimalMax;

import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * Check that the monetary amount being validated is less than or equal to the maximum
 * value specified.
 *
 * @author Lukas Niemeier
 * @author Willi Schönborn
 */
public class DecimalMaxValidatorForMonetaryAmount implements ConstraintValidator<DecimalMax, MonetaryAmount> {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private BigDecimal maxValue;
	private boolean inclusive;

	@Override
	public void initialize(DecimalMax maxValue) {
		try {
			this.maxValue = new BigDecimal( maxValue.value() );
		}
		catch (NumberFormatException nfe) {
			throw LOG.getInvalidBigDecimalFormatException( maxValue.value(), nfe );
		}
		this.inclusive = maxValue.inclusive();
	}

	@Override
	public boolean isValid(MonetaryAmount value, ConstraintValidatorContext context) {
		// null values are valid
		if ( value == null ) {
			return true;
		}

		int comparisonResult = value.getNumber().numberValueExact( BigDecimal.class ).compareTo( maxValue );
		return inclusive ? comparisonResult <= 0 : comparisonResult < 0;
	}

}
