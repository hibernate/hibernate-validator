/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.constraints.annotations.hv;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import java.util.Set;

import jakarta.validation.ConstraintViolation;

import org.hibernate.validator.constraints.CreditCardNumber;
import org.hibernate.validator.test.constraints.annotations.AbstractConstrainedTest;

import org.testng.annotations.Test;

/**
 * @author Marko Bekhta
 */
public class CreditCardNumberConstrainedTest extends AbstractConstrainedTest {

	@Test
	public void testCreditCardNumber() {
		Foo foo = new Foo( "5412 3456 7890 125", "5412 3456 7890 125" );
		Set<ConstraintViolation<Foo>> violations = validator.validate( foo );
		assertNoViolations( violations );
	}

	@Test
	public void testCreditCardNumberInvalid() {
		Foo foo = new Foo( "5412 3456 7890", "5412 3456 7890" );
		Set<ConstraintViolation<Foo>> violations = validator.validate( foo );
		assertThat( violations ).containsOnlyViolations(
				violationOf( CreditCardNumber.class ), violationOf( CreditCardNumber.class )
		);
	}

	private static class Foo {

		@CreditCardNumber(ignoreNonDigitCharacters = true)
		private final String number;
		@CreditCardNumber(ignoreNonDigitCharacters = true)
		private final CharSequence anotherNumber;

		public Foo(String number, CharSequence anotherNumber) {
			this.number = number;
			this.anotherNumber = anotherNumber;
		}
	}
}
