/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.constraintvalidators.bv.money;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;

import javax.money.MonetaryAmount;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.constraints.DecimalMin;

import org.hibernate.validator.internal.constraintvalidators.bv.money.DecimalMinValidatorForMonetaryAmount;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.javamoney.moneta.Money;

/**
 * @author Lukas Niemeier
 * @author Willi Schönborn
 */
public class DecimalMinValidatorForMonetaryAmountTest {

	private final ConstraintValidator<DecimalMin, MonetaryAmount> unit = new DecimalMinValidatorForMonetaryAmount();

	@ParameterizedTest
	@MethodSource("decimalMinValidTestData")
	public void testDecimalMinValid(String value, boolean inclusive, MonetaryAmount amount) {
		unit.initialize( decimalMin( value, inclusive ) );

		assertTrue( unit.isValid( amount, null ) );
	}

	private static Stream<Arguments> decimalMinValidTestData() {
		return Stream.of(
				Arguments.of( "0", true, null ),
				Arguments.of( "0", true, Money.of( 1, "EUR" ) ),
				Arguments.of( "0", true, Money.of( 0, "EUR" ) ),
				Arguments.of( "0", false, Money.of( 1, "EUR" ) )
		);
	}

	@ParameterizedTest
	@MethodSource("decimalMinInvalidTestData")
	public void testDecimalMinInvalid(String value, boolean inclusive, MonetaryAmount amount) {
		unit.initialize( decimalMin( value, inclusive ) );

		assertFalse( unit.isValid( amount, null ) );
	}

	private static Stream<Arguments> decimalMinInvalidTestData() {
		return Stream.of(
				Arguments.of( "0", true, Money.of( -1, "EUR" ) ),
				Arguments.of( "0", false, Money.of( 0, "EUR" ) )
		);
	}

	private DecimalMin decimalMin(final String value, final boolean inclusive) {
		ConstraintAnnotationDescriptor.Builder<DecimalMin> descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( DecimalMin.class );
		descriptorBuilder.setAttribute( "value", value );
		descriptorBuilder.setAttribute( "inclusive", inclusive );
		return descriptorBuilder.build().getAnnotation();
	}

}
