/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.constraintvalidators.bv.money;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectConstraintViolationMessages;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNumberOfViolations;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Set;

import javax.money.MonetaryAmount;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.hibernate.validator.constraints.Currency;
import org.hibernate.validator.internal.constraintvalidators.bv.money.CurrencyValidatorForMonetaryAmount;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationDescriptor;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationFactory;
import org.javamoney.moneta.Money;
import org.testng.annotations.Test;

/**
 * @author Guillaume Smet
 */
public class CurrencyValidatorForMonetaryAmountTest {

	private final ConstraintValidator<Currency, MonetaryAmount> constraintValidator = new CurrencyValidatorForMonetaryAmount();

	@Test
	public void nullIsValid() {
		constraintValidator.initialize( currency( "EUR" ) );

		assertTrue( constraintValidator.isValid( null, null ) );
	}

	@Test
	public void valid() {
		constraintValidator.initialize( currency( "EUR", "USD" ) );

		assertTrue( constraintValidator.isValid( Money.of( 100, "EUR" ), null ) );
	}

	@Test
	public void invalid() {
		constraintValidator.initialize( currency( "EUR", "USD" ) );

		assertFalse( constraintValidator.isValid( Money.of( 100, "GBP" ), null ) );
	}

	@Test
	public void testMessage() {
		Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

		Set<ConstraintViolation<Order>> violations = validator.validate( new Order( Money.of( 100, "GBP" ) ) );
		assertNumberOfViolations( violations, 1 );
		assertCorrectConstraintViolationMessages( violations, "invalid currency (must be one of [EUR, USD])" );
	}

	private Currency currency(String... acceptedCurrencies) {
		AnnotationDescriptor<Currency> descriptor = new AnnotationDescriptor<>( Currency.class );
		descriptor.setValue( "value", acceptedCurrencies );
		return AnnotationFactory.create( descriptor );
	}

	private static class Order {

		@Currency({ "EUR", "USD" })
		private MonetaryAmount amount;

		private Order(MonetaryAmount amount) {
			this.amount = amount;
		}
	}

}
