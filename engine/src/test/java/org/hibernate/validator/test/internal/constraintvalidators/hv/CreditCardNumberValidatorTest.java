/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.constraintvalidators.hv;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.hibernate.validator.testutils.ValidatorUtil.getValidator;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import org.hibernate.validator.constraints.CreditCardNumber;
import org.hibernate.validator.testutil.MyCustomStringImpl;
import org.hibernate.validator.testutil.TestForIssue;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Hardy Ferentschik
 */
public class CreditCardNumberValidatorTest {
	private static Validator validator;

	@BeforeClass
	public static void init() {
		validator = getValidator();
	}

	@Test
	public void testInvalidCreditCardNumber() throws Exception {
		CreditCard card = new CreditCard();
		card.setCreditCardNumber( "1234567890123456" );
		Set<ConstraintViolation<CreditCard>> constraintViolations = validator.validate( card );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( CreditCardNumber.class )
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-502")
	public void testInvalidCreditCardNumberAsCharSequence() throws Exception {
		CreditCard card = new CreditCard();
		card.setCreditCardNumberAsCharSequence( new MyCustomStringImpl( "1234567890123456" ) );
		Set<ConstraintViolation<CreditCard>> constraintViolations = validator.validate( card );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( CreditCardNumber.class )
		);
	}

	@Test
	public void testValidCreditCardNumber() throws Exception {
		CreditCard card = new CreditCard();
		card.setCreditCardNumber( "541234567890125" );
		Set<ConstraintViolation<CreditCard>> constraintViolations = validator.validate( card );
		assertNoViolations( constraintViolations );
	}

	@Test
	@TestForIssue(jiraKey = "HV-502")
	public void testValidCreditCardNumberAsCharSequence() throws Exception {
		CreditCard card = new CreditCard();
		card.setCreditCardNumberAsCharSequence( new MyCustomStringImpl( "541234567890125" ) );
		Set<ConstraintViolation<CreditCard>> constraintViolations = validator.validate( card );
		assertNoViolations( constraintViolations );
	}

	@Test
	@TestForIssue(jiraKey = "HV-901")
	public void testValidCreditCardNumberWithZeroCheckDigit() throws Exception {
		CreditCard card = new CreditCard();
		card.setCreditCardNumber( "5105105105105100" );
		Set<ConstraintViolation<CreditCard>> constraintViolations = validator.validate( card );
		assertNoViolations( constraintViolations );
	}

	@Test
	public void testNullValue() throws Exception {
		Set<ConstraintViolation<CreditCard>> constraintViolations = validator.validate( new CreditCard() );
		assertNoViolations( constraintViolations );
	}

	@Test
	@TestForIssue(jiraKey = "HV-906")
	public void testCharactersAreNotAllowedTest() {
		CreditCard card = new CreditCard();
		card.setCreditCardNumber( "text not numbers" );
		Set<ConstraintViolation<CreditCard>> constraintViolations = validator.validate( card );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( CreditCardNumber.class )
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-906")
	public void testValidCreditCardNumberWithSpaces() throws Exception {
		CreditCard card = new CreditCard();
		card.setCreditCardNumberWithNonDigits( "5412 3456 7890 125" );
		Set<ConstraintViolation<CreditCard>> constraintViolations = validator.validate( card );
		assertNoViolations( constraintViolations );
	}

	@Test
	@TestForIssue(jiraKey = "HV-906")
	public void testValidCreditCardNumberWithDashes() throws Exception {
		CreditCard card = new CreditCard();
		card.setCreditCardNumberWithNonDigits( "5412-3456-7890-125" );
		Set<ConstraintViolation<CreditCard>> constraintViolations = validator.validate( card );
		assertNoViolations( constraintViolations );
	}

	public static class CreditCard {
		@CreditCardNumber
		String creditCardNumber;

		@CreditCardNumber(ignoreNonDigitCharacters = true)
		String creditCardNumberWithNonDigits;

		@CreditCardNumber
		CharSequence creditCardNumberAsCharSequence;

		public void setCreditCardNumber(String creditCardNumber) {
			this.creditCardNumber = creditCardNumber;
		}

		public void setCreditCardNumberAsCharSequence(CharSequence creditCardNumberAsCharSequence) {
			this.creditCardNumberAsCharSequence = creditCardNumberAsCharSequence;
		}

		public void setCreditCardNumberWithNonDigits(String creditCardNumberWithNonDigits) {
			this.creditCardNumberWithNonDigits = creditCardNumberWithNonDigits;
		}
	}
}
