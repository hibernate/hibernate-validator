//$Id: $
package org.hibernate.validator;

import java.io.Serializable;

/**
 * Check a credit card number through the Luhn algorithm
 *
 * @author Emmanuel Bernard
 */
public class CreditCardNumberValidator extends AbstractLuhnValidator implements Validator<CreditCardNumber>, Serializable {

	public void initialize(CreditCardNumber parameters) {
	}

	int multiplicator() {
		return 2;
	}
}
