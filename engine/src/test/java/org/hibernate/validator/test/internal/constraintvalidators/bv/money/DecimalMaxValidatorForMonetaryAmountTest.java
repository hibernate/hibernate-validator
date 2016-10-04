/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.constraintvalidators.bv.money;

import javax.money.MonetaryAmount;
import javax.validation.ConstraintValidator;
import javax.validation.constraints.DecimalMax;

import org.javamoney.moneta.Money;
import org.testng.annotations.Test;

import org.hibernate.validator.internal.constraintvalidators.bv.money.DecimalMaxValidatorForMonetaryAmount;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationDescriptor;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationFactory;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author Lukas Niemeier
 * @author Willi Schönborn
 */
public class DecimalMaxValidatorForMonetaryAmountTest {

	private final ConstraintValidator<DecimalMax, MonetaryAmount> unit = new DecimalMaxValidatorForMonetaryAmount();

	@Test
	public void nullIsValid() {
		unit.initialize( decimalMax( "0", true ) );

		assertTrue( unit.isValid( null, null ) );
	}

	@Test
	public void validIfLess() {
		unit.initialize( decimalMax( "0", true ) );

		assertTrue( unit.isValid( Money.of( -1, "EUR" ), null ) );
	}

	@Test
	public void invalidIfGreater() {
		unit.initialize( decimalMax( "0", true ) );

		assertFalse( unit.isValid( Money.of( 1, "EUR" ), null ) );
	}

	@Test
	public void validIfInclude() {
		unit.initialize( decimalMax( "0", true ) );

		assertTrue( unit.isValid( Money.of( 0, "EUR" ), null ) );
	}

	@Test
	public void invalidIfNotInclude() {
		unit.initialize( decimalMax( "0", false ) );

		assertFalse( unit.isValid( Money.of( 0, "EUR" ), null ) );
	}

	@Test
	public void validIfLessAndNotIncluded() {
		unit.initialize( decimalMax( "0", false ) );

		assertTrue( unit.isValid( Money.of( -1, "EUR" ), null ) );
	}

	private DecimalMax decimalMax(final String value, final boolean inclusive) {
		AnnotationDescriptor<DecimalMax> descriptor = new AnnotationDescriptor<DecimalMax>( DecimalMax.class );
		descriptor.setValue( "value", value );
		descriptor.setValue( "inclusive", inclusive );
		return AnnotationFactory.create( descriptor );
	}

}
