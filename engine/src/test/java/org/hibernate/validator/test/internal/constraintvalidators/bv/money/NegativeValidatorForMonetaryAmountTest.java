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
import jakarta.validation.constraints.Negative;

import org.hibernate.validator.internal.constraintvalidators.bv.money.NegativeValidatorForMonetaryAmount;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;

import org.javamoney.moneta.Money;
import org.testng.annotations.Test;

/**
 * @author Marko Bekhta
 * @author Guillaume Smet
 */
public class NegativeValidatorForMonetaryAmountTest {

	private final ConstraintValidator<Negative, MonetaryAmount> constraintValidator = new NegativeValidatorForMonetaryAmount();

	@Test
	public void nullIsValid() {
		constraintValidator.initialize( negative() );

		assertTrue( constraintValidator.isValid( null, null ) );
	}

	@Test
	public void validIfNegative() {
		constraintValidator.initialize( negative() );

		assertTrue( constraintValidator.isValid( Money.of( -1, "EUR" ), null ) );
	}

	@Test
	public void invalidIfPositive() {
		constraintValidator.initialize( negative() );

		assertFalse( constraintValidator.isValid( Money.of( 1, "EUR" ), null ) );
	}

	@Test
	public void invalidIfZero() {
		constraintValidator.initialize( negative() );

		assertFalse( constraintValidator.isValid( Money.of( 0, "EUR" ), null ) );
	}

	private Negative negative() {
		ConstraintAnnotationDescriptor.Builder<Negative> descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( Negative.class );
		return descriptorBuilder.build().getAnnotation();
	}

}
