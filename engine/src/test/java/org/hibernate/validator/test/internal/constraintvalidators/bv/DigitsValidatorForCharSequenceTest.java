/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.constraintvalidators.bv;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;

import jakarta.validation.constraints.Digits;

import org.hibernate.validator.internal.constraintvalidators.bv.DigitsValidatorForCharSequence;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;
import org.hibernate.validator.testutil.MyCustomStringImpl;
import org.hibernate.validator.testutil.TestForIssue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * @author Alaa Nassef
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DigitsValidatorForCharSequenceTest {

	private static DigitsValidatorForCharSequence constraint;
	private ConstraintAnnotationDescriptor.Builder<Digits> descriptorBuilder;

	@BeforeAll
	public static void init() {
		ConstraintAnnotationDescriptor.Builder<Digits> descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( Digits.class );
		descriptorBuilder.setAttribute( "integer", 5 );
		descriptorBuilder.setAttribute( "fraction", 2 );
		descriptorBuilder.setMessage( "{validator.digits}" );
		Digits p = descriptorBuilder.build().getAnnotation();

		constraint = new DigitsValidatorForCharSequence();
		constraint.initialize( p );
	}

	@BeforeEach
	public void setUp() throws Exception {
		descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( Digits.class );
	}

	@ParameterizedTest
	@MethodSource("testValidData")
	public void testValid(String value) {
		assertTrue( constraint.isValid( value, null ) );
	}

	private static Stream<Arguments> testValidData() {
		return Stream.of(
				Arguments.of( (String) null ),
				Arguments.of( "0" ),
				Arguments.of( "500.2" ),
				Arguments.of( "-12456.22" ),
				Arguments.of( "-000000000.22" )
		);
	}

	@ParameterizedTest
	@MethodSource("testInvalidData")
	public void testInvalid(String value) {
		assertFalse( constraint.isValid( value, null ) );
	}

	private static Stream<Arguments> testInvalidData() {
		return Stream.of(
				Arguments.of( "" ),
				Arguments.of( "256874.0" ),
				Arguments.of( "12.0001" )
		);
	}

	@Test
	public void testNegativeIntegerLength() {
		assertThatThrownBy( () -> {
			descriptorBuilder.setAttribute( "integer", -1 );
			descriptorBuilder.setAttribute( "fraction", 1 );
			Digits p = descriptorBuilder.build().getAnnotation();

			DigitsValidatorForCharSequence constraint = new DigitsValidatorForCharSequence();
			constraint.initialize( p );
		} ).isInstanceOf( IllegalArgumentException.class );
	}

	@Test
	public void testNegativeFractionLength() {
		assertThatThrownBy( () -> {
			descriptorBuilder.setAttribute( "integer", 1 );
			descriptorBuilder.setAttribute( "fraction", -1 );
			Digits p = descriptorBuilder.build().getAnnotation();

			DigitsValidatorForCharSequence constraint = new DigitsValidatorForCharSequence();
			constraint.initialize( p );
		} ).isInstanceOf( IllegalArgumentException.class );
	}

	@Test
	@TestForIssue(jiraKey = "HV-502")
	public void testIsValidCharSequence() {
		assertTrue( constraint.isValid( new MyCustomStringImpl( "500.2" ), null ) );
	}
}
