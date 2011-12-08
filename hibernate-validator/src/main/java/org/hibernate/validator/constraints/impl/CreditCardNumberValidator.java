package org.hibernate.validator.constraints.impl;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.CreditCardNumber;

/**
 * Check a credit card number through the Luhn algorithm.
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 */
public class CreditCardNumberValidator implements ConstraintValidator<CreditCardNumber, CharSequence> {
	private LuhnValidator luhnValidator;

	public CreditCardNumberValidator() {
		luhnValidator = new LuhnValidator( 2 );
	}

	public void initialize(CreditCardNumber annotation) {
	}

	public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
		if ( value == null ) {
			return true;
		}
		return luhnValidator.passesLuhnTest( value.toString() );
	}
}
