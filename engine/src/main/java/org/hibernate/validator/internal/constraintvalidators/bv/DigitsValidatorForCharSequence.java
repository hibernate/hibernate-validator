/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv;

import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraints.Digits;

import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * Validates that the character sequence (e.g. string) being validated consists of digits,
 * and matches the pattern defined in the constraint.
 *
 * @author Alaa Nassef
 * @author Hardy Ferentschik
 */
public class DigitsValidatorForCharSequence implements ConstraintValidator<Digits, CharSequence> {

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
	public boolean isValid(CharSequence charSequence, ConstraintValidatorContext constraintValidatorContext) {
		//null values are valid
		if ( charSequence == null ) {
			return true;
		}

		BigDecimal bigNum = getBigDecimalValue( charSequence );
		if ( bigNum == null ) {
			return false;
		}

		int integerPartLength = bigNum.precision() - bigNum.scale();
		int fractionPartLength = bigNum.scale() < 0 ? 0 : bigNum.scale();

		return ( maxIntegerLength >= integerPartLength && maxFractionLength >= fractionPartLength );
	}

	private BigDecimal getBigDecimalValue(CharSequence charSequence) {
		BigDecimal bd;
		try {
			bd = new BigDecimal( charSequence.toString() );
		}
		catch (NumberFormatException nfe) {
			return null;
		}
		return bd;
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
