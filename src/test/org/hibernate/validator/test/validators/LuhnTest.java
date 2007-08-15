//$Id: $
package org.hibernate.validator.test.validators;

import junit.framework.TestCase;
import org.hibernate.validator.ClassValidator;
import org.hibernate.validator.InvalidValue;

/**
 * @author Emmanuel Bernard
 */
public class LuhnTest extends TestCase {
	public void testCreditCard() {
		CreditCard card = new CreditCard();
		card.number = "1234567890123456";
		ClassValidator<CreditCard> classValidator = new ClassValidator<CreditCard>( CreditCard.class );
		InvalidValue[] invalidValues = classValidator.getInvalidValues( card );
		assertEquals( 1, invalidValues.length );
		card.number = "541234567890125"; //right CC (luhn compliant)
		invalidValues = classValidator.getInvalidValues( card );
		assertEquals( 0, invalidValues.length );
		card.ean = "9782266156066"; //right EAN
		invalidValues = classValidator.getInvalidValues( card );
		assertEquals( 0, invalidValues.length );
		card.ean = "9782266156067"; //wrong EAN
		invalidValues = classValidator.getInvalidValues( card );
		assertEquals( 1, invalidValues.length );
	}
}
