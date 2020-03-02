/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.constraintvalidators.bv.money;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import javax.money.MonetaryAmount;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.constraints.PositiveOrZero;

import org.hibernate.validator.internal.constraintvalidators.bv.money.PositiveOrZeroValidatorForMonetaryAmount;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;

import org.javamoney.moneta.Money;
import org.testng.annotations.Test;

/**
 * @author Marko Bekhta
 * @author Guillaume Smet
 */
public class PositiveOrZeroValidatorForMonetaryAmountTest {

	private final ConstraintValidator<PositiveOrZero, MonetaryAmount> constraintValidator = new PositiveOrZeroValidatorForMonetaryAmount();

	@Test
	public void nullIsValid() {
		constraintValidator.initialize( positiveOrZero() );

		assertTrue( constraintValidator.isValid( null, null ) );
	}

	@Test
	public void invalidIfNegative() {
		constraintValidator.initialize( positiveOrZero() );

		assertFalse( constraintValidator.isValid( Money.of( -1, "EUR" ), null ) );
	}

	@Test
	public void validIfPositive() {
		constraintValidator.initialize( positiveOrZero() );

		assertTrue( constraintValidator.isValid( Money.of( 1, "EUR" ), null ) );
	}

	@Test
	public void validIfZero() {
		constraintValidator.initialize( positiveOrZero() );

		assertTrue( constraintValidator.isValid( Money.of( 0, "EUR" ), null ) );
	}

	private PositiveOrZero positiveOrZero() {
		ConstraintAnnotationDescriptor.Builder<PositiveOrZero> descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( PositiveOrZero.class );
		return descriptorBuilder.build().getAnnotation();
	}
}
