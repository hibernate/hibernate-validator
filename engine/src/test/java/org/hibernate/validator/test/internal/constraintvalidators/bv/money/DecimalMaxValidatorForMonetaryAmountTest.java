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
import jakarta.validation.constraints.DecimalMax;

import org.hibernate.validator.internal.constraintvalidators.bv.money.DecimalMaxValidatorForMonetaryAmount;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.javamoney.moneta.Money;

/**
 * @author Lukas Niemeier
 * @author Willi Schönborn
 */
public class DecimalMaxValidatorForMonetaryAmountTest {

	private final ConstraintValidator<DecimalMax, MonetaryAmount> unit = new DecimalMaxValidatorForMonetaryAmount();

	@ParameterizedTest
	@MethodSource("decimalMaxValidTestData")
	public void testDecimalMaxValid(String value, boolean inclusive, MonetaryAmount amount) {
		unit.initialize( decimalMax( value, inclusive ) );

		assertTrue( unit.isValid( amount, null ) );
	}

	private static Stream<Arguments> decimalMaxValidTestData() {
		return Stream.of(
				Arguments.of( "0", true, null ),
				Arguments.of( "0", true, Money.of( -1, "EUR" ) ),
				Arguments.of( "0", true, Money.of( 0, "EUR" ) ),
				Arguments.of( "0", false, Money.of( -1, "EUR" ) )
		);
	}

	@ParameterizedTest
	@MethodSource("decimalMaxInvalidTestData")
	public void testDecimalMaxInvalid(String value, boolean inclusive, MonetaryAmount amount) {
		unit.initialize( decimalMax( value, inclusive ) );

		assertFalse( unit.isValid( amount, null ) );
	}

	private static Stream<Arguments> decimalMaxInvalidTestData() {
		return Stream.of(
				Arguments.of( "0", true, Money.of( 1, "EUR" ) ),
				Arguments.of( "0", false, Money.of( 0, "EUR" ) )
		);
	}

	private DecimalMax decimalMax(final String value, final boolean inclusive) {
		ConstraintAnnotationDescriptor.Builder<DecimalMax> descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( DecimalMax.class );
		descriptorBuilder.setAttribute( "value", value );
		descriptorBuilder.setAttribute( "inclusive", inclusive );
		return descriptorBuilder.build().getAnnotation();
	}

}
