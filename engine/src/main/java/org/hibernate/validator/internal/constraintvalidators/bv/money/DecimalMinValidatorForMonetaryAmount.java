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
import jakarta.validation.constraints.DecimalMin;

import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * Check that the number being validated is less than or equal to the maximum
 * value specified.
 *
 * @author Lukas Niemeier
 * @author Willi Schönborn
 */
public class DecimalMinValidatorForMonetaryAmount implements ConstraintValidator<DecimalMin, MonetaryAmount> {

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
	public boolean isValid(MonetaryAmount value, ConstraintValidatorContext context) {
		// null values are valid
		if ( value == null ) {
			return true;
		}

		int comparisonResult = value.getNumber().numberValueExact( BigDecimal.class ).compareTo( minValue );
		return inclusive ? comparisonResult >= 0 : comparisonResult > 0;
	}

}
