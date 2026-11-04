/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.constraintvalidators.bv.money;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.stream.Stream;

import javax.money.CurrencyUnit;
import javax.money.Monetary;
import javax.money.MonetaryAmount;

import jakarta.validation.constraints.Digits;

import org.hibernate.validator.internal.constraintvalidators.bv.money.DigitsValidatorForMonetaryAmount;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;
import org.hibernate.validator.testutil.TestForIssue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.javamoney.moneta.Money;

/**
 * @author Dario Seidl
 */
@TestForIssue(jiraKey = "HV-1723")
public class DigitsValidatorForMonetaryAmountTest {

	private ConstraintAnnotationDescriptor.Builder<Digits> descriptorBuilder;

	@BeforeEach
	public void setUp() {
		descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( Digits.class );
		descriptorBuilder.setMessage( "{validator.digits}" );
	}

	@ParameterizedTest
	@MethodSource("testIsValidValidData")
	public void testIsValidValid(MonetaryAmount value) {
		descriptorBuilder.setAttribute( "integer", 5 );
		descriptorBuilder.setAttribute( "fraction", 2 );
		Digits p = descriptorBuilder.build().getAnnotation();

		DigitsValidatorForMonetaryAmount constraint = new DigitsValidatorForMonetaryAmount();
		constraint.initialize( p );

		assertTrue( constraint.isValid( value, null ) );
	}

	private static Stream<Arguments> testIsValidValidData() {
		CurrencyUnit currency = Monetary.getCurrency( "EUR" );
		return Stream.of(
				Arguments.of( (MonetaryAmount) null ),
				Arguments.of( Money.of( Byte.valueOf( "0" ), currency ) ),
				Arguments.of( Money.of( Double.valueOf( "500.2" ), currency ) ),
				Arguments.of( Money.of( new BigDecimal( "-12345.12" ), currency ) ),
				Arguments.of( Money.of( Float.valueOf( "-000000000.22" ), currency ) )
		);
	}

	@ParameterizedTest
	@MethodSource("testIsValidInvalidData")
	public void testIsValidInvalid(MonetaryAmount value) {
		descriptorBuilder.setAttribute( "integer", 5 );
		descriptorBuilder.setAttribute( "fraction", 2 );
		Digits p = descriptorBuilder.build().getAnnotation();

		DigitsValidatorForMonetaryAmount constraint = new DigitsValidatorForMonetaryAmount();
		constraint.initialize( p );

		assertFalse( constraint.isValid( value, null ) );
	}

	private static Stream<Arguments> testIsValidInvalidData() {
		CurrencyUnit currency = Monetary.getCurrency( "EUR" );
		return Stream.of(
				Arguments.of( Money.of( new BigDecimal( "-123456.12" ), currency ) ),
				Arguments.of( Money.of( new BigDecimal( "-123456.123" ), currency ) ),
				Arguments.of( Money.of( new BigDecimal( "-12345.123" ), currency ) ),
				Arguments.of( Money.of( new BigDecimal( "12345.123" ), currency ) ),
				Arguments.of( Money.of( Integer.valueOf( "256874" ), currency ) ),
				Arguments.of( Money.of( Double.valueOf( "12.0001" ), currency ) )
		);
	}

	@Test
	public void testIsValidZeroLength() {
		descriptorBuilder.setAttribute( "integer", 0 );
		descriptorBuilder.setAttribute( "fraction", 0 );
		Digits p = descriptorBuilder.build().getAnnotation();

		DigitsValidatorForMonetaryAmount constraint = new DigitsValidatorForMonetaryAmount();
		constraint.initialize( p );

		CurrencyUnit currency = Monetary.getCurrency( "EUR" );

		assertTrue( constraint.isValid( null, null ) );
		assertFalse( constraint.isValid( Money.of( Byte.valueOf( "0" ), currency ), null ) );
		assertFalse( constraint.isValid( Money.of( Double.valueOf( "500.2" ), currency ), null ) );
	}

	@Test
	public void testNegativeIntegerLength() {
		descriptorBuilder.setAttribute( "integer", -1 );
		descriptorBuilder.setAttribute( "fraction", 1 );
		Digits p = descriptorBuilder.build().getAnnotation();

		DigitsValidatorForMonetaryAmount constraint = new DigitsValidatorForMonetaryAmount();
		assertThatThrownBy( () -> constraint.initialize( p ) )
				.isInstanceOf( IllegalArgumentException.class );
	}

	@Test
	public void testNegativeFractionLength() {
		descriptorBuilder.setAttribute( "integer", 1 );
		descriptorBuilder.setAttribute( "fraction", -1 );
		Digits p = descriptorBuilder.build().getAnnotation();

		DigitsValidatorForMonetaryAmount constraint = new DigitsValidatorForMonetaryAmount();
		assertThatThrownBy( () -> constraint.initialize( p ) )
				.isInstanceOf( IllegalArgumentException.class );
	}

	@Test
	public void testTrailingZerosAreTrimmed() {
		descriptorBuilder.setAttribute( "integer", 12 );
		descriptorBuilder.setAttribute( "fraction", 3 );
		Digits p = descriptorBuilder.build().getAnnotation();

		DigitsValidatorForMonetaryAmount constraint = new DigitsValidatorForMonetaryAmount();
		constraint.initialize( p );

		CurrencyUnit currency = Monetary.getCurrency( "EUR" );

		assertTrue( constraint.isValid( Money.of( 0.001d, currency ), null ) );
		assertTrue( constraint.isValid( Money.of( 0.00100d, currency ), null ) );
		assertFalse( constraint.isValid( Money.of( 0.0001d, currency ), null ) );
	}

}
