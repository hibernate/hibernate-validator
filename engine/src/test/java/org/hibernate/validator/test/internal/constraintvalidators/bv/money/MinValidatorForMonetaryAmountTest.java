/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.constraintvalidators.bv.money;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.money.MonetaryAmount;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.constraints.Min;

import org.hibernate.validator.internal.constraintvalidators.bv.money.MinValidatorForMonetaryAmount;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;

import org.junit.jupiter.api.Test;

import org.javamoney.moneta.Money;

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
		ConstraintAnnotationDescriptor.Builder<Min> descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( Min.class );
		descriptorBuilder.setAttribute( "value", value );
		return descriptorBuilder.build().getAnnotation();
	}

}
