/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.constraintvalidators.bv;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.stream.Stream;

import jakarta.validation.constraints.Digits;

import org.hibernate.validator.internal.constraintvalidators.bv.DigitsValidatorForNumber;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * @author Alaa Nassef
 * @author Hardy Ferentschik
 */
public class DigitsValidatorForNumberTest {

	private ConstraintAnnotationDescriptor.Builder<Digits> descriptorBuilder;

	@BeforeEach
	public void setUp() throws Exception {
		descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( Digits.class );
		descriptorBuilder.setMessage( "{validator.digits}" );
	}

	@ParameterizedTest
	@MethodSource("testIsValidValidData")
	public void testIsValidValid(Number value) {
		descriptorBuilder.setAttribute( "integer", 5 );
		descriptorBuilder.setAttribute( "fraction", 2 );
		Digits p = descriptorBuilder.build().getAnnotation();

		DigitsValidatorForNumber constraint = new DigitsValidatorForNumber();
		constraint.initialize( p );

		assertTrue( constraint.isValid( value, null ) );
	}

	private static Stream<Arguments> testIsValidValidData() {
		return Stream.of(
				Arguments.of( (Number) null ),
				Arguments.of( Byte.valueOf( "0" ) ),
				Arguments.of( Double.valueOf( "500.2" ) ),
				Arguments.of( new BigDecimal( "-12345.12" ) ),
				Arguments.of( Float.valueOf( "-000000000.22" ) )
		);
	}

	@ParameterizedTest
	@MethodSource("testIsValidInvalidData")
	public void testIsValidInvalid(Number value) {
		descriptorBuilder.setAttribute( "integer", 5 );
		descriptorBuilder.setAttribute( "fraction", 2 );
		Digits p = descriptorBuilder.build().getAnnotation();

		DigitsValidatorForNumber constraint = new DigitsValidatorForNumber();
		constraint.initialize( p );

		assertFalse( constraint.isValid( value, null ) );
	}

	private static Stream<Arguments> testIsValidInvalidData() {
		return Stream.of(
				Arguments.of( new BigDecimal( "-123456.12" ) ),
				Arguments.of( new BigDecimal( "-123456.123" ) ),
				Arguments.of( new BigDecimal( "-12345.123" ) ),
				Arguments.of( new BigDecimal( "12345.123" ) ),
				Arguments.of( Integer.valueOf( "256874" ) ),
				Arguments.of( Double.valueOf( "12.0001" ) )
		);
	}

	@Test
	public void testIsValidZeroLength() {
		descriptorBuilder.setAttribute( "integer", 0 );
		descriptorBuilder.setAttribute( "fraction", 0 );
		Digits p = descriptorBuilder.build().getAnnotation();

		DigitsValidatorForNumber constraint = new DigitsValidatorForNumber();
		constraint.initialize( p );


		assertTrue( constraint.isValid( null, null ) );
		assertFalse( constraint.isValid( Byte.valueOf( "0" ), null ) );
		assertFalse( constraint.isValid( Double.valueOf( "500.2" ), null ) );
	}

	@Test
	public void testNegativeIntegerLength() {
		assertThatThrownBy( () -> {
			descriptorBuilder.setAttribute( "integer", -1 );
			descriptorBuilder.setAttribute( "fraction", 1 );
			Digits p = descriptorBuilder.build().getAnnotation();

			DigitsValidatorForNumber constraint = new DigitsValidatorForNumber();
			constraint.initialize( p );
		} ).isInstanceOf( IllegalArgumentException.class );
	}

	@Test
	public void testNegativeFractionLength() {
		assertThatThrownBy( () -> {
			descriptorBuilder.setAttribute( "integer", 1 );
			descriptorBuilder.setAttribute( "fraction", -1 );
			Digits p = descriptorBuilder.build().getAnnotation();

			DigitsValidatorForNumber constraint = new DigitsValidatorForNumber();
			constraint.initialize( p );
		} ).isInstanceOf( IllegalArgumentException.class );
	}

	@ParameterizedTest
	@MethodSource("testTrailingZerosAreTrimmedValidData")
	public void testTrailingZerosAreTrimmedValid(Number value) {
		descriptorBuilder.setAttribute( "integer", 12 );
		descriptorBuilder.setAttribute( "fraction", 3 );
		Digits p = descriptorBuilder.build().getAnnotation();

		DigitsValidatorForNumber constraint = new DigitsValidatorForNumber();
		constraint.initialize( p );

		assertTrue( constraint.isValid( value, null ) );
	}

	private static Stream<Arguments> testTrailingZerosAreTrimmedValidData() {
		return Stream.of(
				Arguments.of( 0.001d ),
				Arguments.of( 0.00100d )
		);
	}

	@ParameterizedTest
	@MethodSource("testTrailingZerosAreTrimmedInvalidData")
	public void testTrailingZerosAreTrimmedInvalid(Number value) {
		descriptorBuilder.setAttribute( "integer", 12 );
		descriptorBuilder.setAttribute( "fraction", 3 );
		Digits p = descriptorBuilder.build().getAnnotation();

		DigitsValidatorForNumber constraint = new DigitsValidatorForNumber();
		constraint.initialize( p );

		assertFalse( constraint.isValid( value, null ) );
	}

	private static Stream<Arguments> testTrailingZerosAreTrimmedInvalidData() {
		return Stream.of(
				Arguments.of( 0.0001d )
		);
	}

}
