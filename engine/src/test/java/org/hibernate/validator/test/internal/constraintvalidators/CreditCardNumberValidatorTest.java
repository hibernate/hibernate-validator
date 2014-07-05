/*
* JBoss, Home of Professional Open Source
* Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual contributors
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.hibernate.validator.test.internal.constraintvalidators;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.hibernate.validator.constraints.CreditCardNumber;
import org.hibernate.validator.testutil.TestForIssue;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNumberOfViolations;
import static org.hibernate.validator.testutil.ValidatorUtil.getValidator;

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
		assertNumberOfViolations( constraintViolations, 1 );
	}

	@Test
	@TestForIssue(jiraKey = "HV-502")
	public void testInvalidCreditCardNumberAsCharSequence() throws Exception {
		CreditCard card = new CreditCard();
		card.setCreditCardNumberAsCharSequence( new MyCustomStringImpl( "1234567890123456" ) );
		Set<ConstraintViolation<CreditCard>> constraintViolations = validator.validate( card );
		assertNumberOfViolations( constraintViolations, 1 );
	}

	@Test
	public void testValidCreditCardNumber() throws Exception {
		CreditCard card = new CreditCard();
		card.setCreditCardNumber( "541234567890125" );
		Set<ConstraintViolation<CreditCard>> constraintViolations = validator.validate( card );
		assertNumberOfViolations( constraintViolations, 0 );
	}

	@Test
	@TestForIssue(jiraKey = "HV-502")
	public void testValidCreditCardNumberAsCharSequence() throws Exception {
		CreditCard card = new CreditCard();
		card.setCreditCardNumberAsCharSequence( new MyCustomStringImpl( "541234567890125" ) );
		Set<ConstraintViolation<CreditCard>> constraintViolations = validator.validate( card );
		assertNumberOfViolations( constraintViolations, 0 );
	}

	@Test
	@TestForIssue(jiraKey = "HV-901")
	public void testValidCreditCardNumberWithZeroCheckDigit() throws Exception {
		CreditCard card = new CreditCard();
		card.setCreditCardNumber( "5105105105105100" );
		Set<ConstraintViolation<CreditCard>> constraintViolations = validator.validate( card );
		assertNumberOfViolations( constraintViolations, 0 );
	}

	@Test
	public void testNullValue() throws Exception {
		Set<ConstraintViolation<CreditCard>> constraintViolations = validator.validate( new CreditCard() );
		assertNumberOfViolations( constraintViolations, 0 );
	}

	@Test
	@TestForIssue(jiraKey = "HV-906")
	public void testCharactersAreNotAllowedTest() {
		CreditCard card = new CreditCard();
		card.setCreditCardNumber( "text not numbers" );
		Set<ConstraintViolation<CreditCard>> constraintViolations = validator.validate( card );
		assertNumberOfViolations( constraintViolations, 1 );
	}

	@Test
	@TestForIssue(jiraKey = "HV-906")
	public void testValidCreditCardNumberWithSpaces() throws Exception {
		CreditCard card = new CreditCard();
		card.setCreditCardNumberWithNonDigits( "5412 3456 7890 125" );
		Set<ConstraintViolation<CreditCard>> constraintViolations = validator.validate( card );
		assertNumberOfViolations( constraintViolations, 0 );
	}

	@Test
	@TestForIssue(jiraKey = "HV-906")
	public void testValidCreditCardNumberWithDashes() throws Exception {
		CreditCard card = new CreditCard();
		card.setCreditCardNumberWithNonDigits( "5412-3456-7890-125" );
		Set<ConstraintViolation<CreditCard>> constraintViolations = validator.validate( card );
		assertNumberOfViolations( constraintViolations, 0 );
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
