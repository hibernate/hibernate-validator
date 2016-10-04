/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.constraintvalidators.bv.money;

import javax.money.MonetaryAmount;
import javax.validation.ConstraintValidator;
import javax.validation.constraints.Min;

import org.javamoney.moneta.Money;
import org.testng.annotations.Test;

import org.hibernate.validator.internal.constraintvalidators.bv.money.MinValidatorForMonetaryAmount;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationDescriptor;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationFactory;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author Lukas Niemeier
 * @author Willi Schönborn
 */
public class MinValidatorForMonetaryAmountTest {

	private final ConstraintValidator<Min, MonetaryAmount> unit = new MinValidatorForMonetaryAmount();

	@Test
	public void nullIsValid() {
		unit.initialize( min( 0 ) );

		assertTrue( unit.isValid( null, null ) );
	}

	@Test
	public void invalidIfLess() {
		unit.initialize( min( 0 ) );

		assertFalse( unit.isValid( Money.of( -1, "EUR" ), null ) );
	}

	@Test
	public void validIfGreater() {
		unit.initialize( min( 0 ) );

		assertTrue( unit.isValid( Money.of( 1, "EUR" ), null ) );
	}

	@Test
	public void validIfInclude() {
		unit.initialize( min( 0 ) );

		assertTrue( unit.isValid( Money.of( 0, "EUR" ), null ) );
	}

	private Min min(final long value) {
		AnnotationDescriptor<Min> descriptor = new AnnotationDescriptor<>( Min.class );
		descriptor.setValue( "value", value );
		return AnnotationFactory.create( descriptor );
	}

}
