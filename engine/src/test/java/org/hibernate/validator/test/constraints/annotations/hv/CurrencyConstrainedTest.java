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

import javax.money.MonetaryAmount;
import jakarta.validation.ConstraintViolation;

import org.hibernate.validator.constraints.Currency;
import org.hibernate.validator.test.constraints.annotations.AbstractConstrainedTest;

import org.javamoney.moneta.Money;
import org.testng.annotations.Test;

/**
 * @author Marko Bekhta
 */
public class CurrencyConstrainedTest extends AbstractConstrainedTest {

	@Test
	public void testCurrencyNumber() {
		Foo foo = new Foo( Money.of( 1, "USD" ) );
		Set<ConstraintViolation<Foo>> violations = validator.validate( foo );
		assertNoViolations( violations );
	}

	@Test
	public void testCurrencyInvalid() {
		Foo foo = new Foo( Money.of( 1, "UAH" ) );
		Set<ConstraintViolation<Foo>> violations = validator.validate( foo );
		assertThat( violations ).containsOnlyViolations(
				violationOf( Currency.class )
		);
	}

	private static class Foo {

		@Currency("USD")
		private final MonetaryAmount amount;

		public Foo(MonetaryAmount amount) {
			this.amount = amount;
		}
	}
}
