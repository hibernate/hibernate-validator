/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.constraintvalidators.bv.money;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import javax.money.MonetaryAmount;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.constraints.Max;

import org.hibernate.validator.internal.constraintvalidators.bv.money.MaxValidatorForMonetaryAmount;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;

import org.javamoney.moneta.Money;
import org.testng.annotations.Test;

/**
 * @author Lukas Niemeier
 * @author Willi Schönborn
 */
public class MaxValidatorForMonetaryAmountTest {

	private final ConstraintValidator<Max, MonetaryAmount> unit = new MaxValidatorForMonetaryAmount();

	@Test
	public void nullIsValid() {
		unit.initialize( max( 0 ) );

		assertTrue( unit.isValid( null, null ) );
	}

	@Test
	public void validIfLess() {
		unit.initialize( max( 0 ) );

		assertTrue( unit.isValid( Money.of( -1, "EUR" ), null ) );
	}

	@Test
	public void invalidIfGreater() {
		unit.initialize( max( 0 ) );

		assertFalse( unit.isValid( Money.of( 1, "EUR" ), null ) );
	}

	@Test
	public void validIfInclude() {
		unit.initialize( max( 0 ) );

		assertTrue( unit.isValid( Money.of( 0, "EUR" ), null ) );
	}

	private Max max(final long value) {
		ConstraintAnnotationDescriptor.Builder<Max> descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( Max.class );
		descriptorBuilder.setAttribute( "value", value );
		return descriptorBuilder.build().getAnnotation();
	}

}
