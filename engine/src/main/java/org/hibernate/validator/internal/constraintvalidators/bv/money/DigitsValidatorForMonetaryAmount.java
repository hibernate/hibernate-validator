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
import jakarta.validation.constraints.Digits;

import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * Validates that the number part of the {@code MonetaryAmount} being validated matches the pattern
 * defined in the constraint.
 *
 * @author Dario Seidl
 */
public class DigitsValidatorForMonetaryAmount implements ConstraintValidator<Digits, MonetaryAmount> {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private int maxIntegerLength;
	private int maxFractionLength;

	@Override
	public void initialize(Digits constraintAnnotation) {
		this.maxIntegerLength = constraintAnnotation.integer();
		this.maxFractionLength = constraintAnnotation.fraction();
		validateParameters();
	}

	@Override
	public boolean isValid(MonetaryAmount monetaryAmount, ConstraintValidatorContext constraintValidatorContext) {
		//null values are valid
		if ( monetaryAmount == null ) {
			return true;
		}

		BigDecimal bigNum = monetaryAmount.getNumber().numberValueExact( BigDecimal.class );

		int integerPartLength = bigNum.precision() - bigNum.scale();
		int fractionPartLength = bigNum.scale() < 0 ? 0 : bigNum.scale();

		return ( maxIntegerLength >= integerPartLength && maxFractionLength >= fractionPartLength );
	}

	private void validateParameters() {
		if ( maxIntegerLength < 0 ) {
			throw LOG.getInvalidLengthForIntegerPartException();
		}
		if ( maxFractionLength < 0 ) {
			throw LOG.getInvalidLengthForFractionPartException();
		}
	}
}
