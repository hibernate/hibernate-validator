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
import javax.validation.constraints.Min;

/**
 * Check that the character sequence (e.g. string) being validated represents a number, and has a value
 * more than or equal to the minimum value specified.
 *
 * @author Alaa Nassef
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 */
public class MinValidatorForCharSequence implements ConstraintValidator<Min, CharSequence> {

	private BigDecimal minValue;

	@Override
	public void initialize(Min minValue) {
		this.minValue = BigDecimal.valueOf( minValue.value() );
	}

	@Override
	public boolean isValid(CharSequence value, ConstraintValidatorContext constraintValidatorContext) {
		//null values are valid
		if ( value == null ) {
			return true;
		}
		try {
			return new BigDecimal( value.toString() ).compareTo( minValue ) != -1;
		}
		catch (NumberFormatException nfe) {
			return false;
		}
	}
}
