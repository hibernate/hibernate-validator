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
import jakarta.validation.constraints.NegativeOrZero;

import org.hibernate.validator.internal.constraintvalidators.bv.money.NegativeOrZeroValidatorForMonetaryAmount;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;
import org.javamoney.moneta.Money;
import org.testng.annotations.Test;

/**
 * @author Marko Bekhta
 * @author Guillaume Smet
 */
public class NegativeOrZeroValidatorForMonetaryAmountTest {

	private final ConstraintValidator<NegativeOrZero, MonetaryAmount> constraintValidator = new NegativeOrZeroValidatorForMonetaryAmount();

	@Test
	public void nullIsValid() {
		constraintValidator.initialize( negativeOrZero() );

		assertTrue( constraintValidator.isValid( null, null ) );
	}

	@Test
	public void validIfNegative() {
		constraintValidator.initialize( negativeOrZero() );

		assertTrue( constraintValidator.isValid( Money.of( -1, "EUR" ), null ) );
	}

	@Test
	public void invalidIfPositive() {
		constraintValidator.initialize( negativeOrZero() );

		assertFalse( constraintValidator.isValid( Money.of( 1, "EUR" ), null ) );
	}

	@Test
	public void validIfZero() {
		constraintValidator.initialize( negativeOrZero() );

		assertTrue( constraintValidator.isValid( Money.of( 0, "EUR" ), null ) );
	}

	private NegativeOrZero negativeOrZero() {
		ConstraintAnnotationDescriptor.Builder<NegativeOrZero> descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( NegativeOrZero.class );
		return descriptorBuilder.build().getAnnotation();
	}

}
