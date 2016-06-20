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
import javax.validation.constraints.Max;

/**
 * Check that the character sequence (e.g. string) validated represents a number, and has a value
 * less than or equal to the maximum value specified.
 *
 * @author Alaa Nassef
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 */
public class MaxValidatorForCharSequence implements ConstraintValidator<Max, CharSequence> {

	private BigDecimal maxValue;

	@Override
	public void initialize(Max maxValue) {
		this.maxValue = BigDecimal.valueOf( maxValue.value() );
	}

	@Override
	public boolean isValid(CharSequence value, ConstraintValidatorContext constraintValidatorContext) {
		//null values are valid
		if ( value == null ) {
			return true;
		}
		try {
			return new BigDecimal( value.toString() ).compareTo( maxValue ) != 1;
		}
		catch (NumberFormatException nfe) {
			return false;
		}
	}
}
