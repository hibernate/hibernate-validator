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
import javax.validation.ConstraintValidator;
import javax.validation.constraints.Negative;

import org.hibernate.validator.internal.constraintvalidators.bv.money.NegativeValidatorForMonetaryAmount;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationDescriptor;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationFactory;

import org.javamoney.moneta.Money;
import org.testng.annotations.Test;

/**
 * @author Marko Bekhta
 */
public class NegativeValidatorForMonetaryAmountTest {

	private final ConstraintValidator<Negative, MonetaryAmount> constraintValidator = new NegativeValidatorForMonetaryAmount();

	@Test
	public void nullIsValid() {
		constraintValidator.initialize( negative( true ) );

		assertTrue( constraintValidator.isValid( null, null ) );
	}

	@Test
	public void nullIsValidStrict() {
		constraintValidator.initialize( negative( false ) );

		assertTrue( constraintValidator.isValid( null, null ) );
	}

	@Test
	public void validIfNegative() {
		constraintValidator.initialize( negative( false ) );

		assertTrue( constraintValidator.isValid( Money.of( -1, "EUR" ), null ) );
		assertTrue( constraintValidator.isValid( Money.of( 0, "EUR" ), null ) );
	}

	@Test
	public void validIfNegativeStrict() {
		constraintValidator.initialize( negative( true ) );

		assertTrue( constraintValidator.isValid( Money.of( -1, "EUR" ), null ) );
	}

	@Test
	public void invalidIfPositive() {
		constraintValidator.initialize( negative( false ) );

		assertFalse( constraintValidator.isValid( Money.of( 1, "EUR" ), null ) );
	}

	@Test
	public void invalidIfPositiveStrict() {
		constraintValidator.initialize( negative( true ) );

		assertFalse( constraintValidator.isValid( Money.of( 1, "EUR" ), null ) );
		assertFalse( constraintValidator.isValid( Money.of( 0, "EUR" ), null ) );
	}

	private Negative negative(final boolean strict) {
		AnnotationDescriptor<Negative> descriptor = new AnnotationDescriptor<>( Negative.class );
		descriptor.setValue( "strict", strict );
		return AnnotationFactory.create( descriptor );
	}

}
