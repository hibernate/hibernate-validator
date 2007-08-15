//$Id: $
package org.hibernate.validator.test.validators;

import org.hibernate.validator.EAN;

/**
 * @author Emmanuel Bernard
 */
public class CreditCard {
	@org.hibernate.validator.CreditCardNumber
	public String number;
	@EAN
	public String ean;
}
