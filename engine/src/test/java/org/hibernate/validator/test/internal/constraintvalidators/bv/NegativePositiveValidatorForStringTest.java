/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.constraintvalidators.bv;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;

import jakarta.validation.constraints.Negative;
import jakarta.validation.constraints.NegativeOrZero;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.NegativeOrZeroValidatorForCharSequence;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.NegativeValidatorForCharSequence;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.PositiveOrZeroValidatorForCharSequence;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.PositiveValidatorForCharSequence;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * @author Guillaume Smet
 */
public class NegativePositiveValidatorForStringTest {

	@ParameterizedTest
	@MethodSource("testValidPositiveValidatorData")
	public void testValidPositiveValidator(String value) {
		ConstraintAnnotationDescriptor.Builder<Positive> descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( Positive.class );
		descriptorBuilder.setMessage( "{validator.positive}" );
		Positive m = descriptorBuilder.build().getAnnotation();

		PositiveValidatorForCharSequence constraint = new PositiveValidatorForCharSequence();
		constraint.initialize( m );

		assertTrue( constraint.isValid( value, null ) );
	}

	private static Stream<Arguments> testValidPositiveValidatorData() {
		return Stream.of(
				Arguments.of( (String) null ),
				Arguments.of( "15" ),
				Arguments.of( "15.0" )
		);
	}

	@ParameterizedTest
	@MethodSource("testInvalidPositiveValidatorData")
	public void testInvalidPositiveValidator(String value) {
		ConstraintAnnotationDescriptor.Builder<Positive> descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( Positive.class );
		descriptorBuilder.setMessage( "{validator.positive}" );
		Positive m = descriptorBuilder.build().getAnnotation();

		PositiveValidatorForCharSequence constraint = new PositiveValidatorForCharSequence();
		constraint.initialize( m );

		assertFalse( constraint.isValid( value, null ) );
	}

	private static Stream<Arguments> testInvalidPositiveValidatorData() {
		return Stream.of(
				Arguments.of( "0" ),
				Arguments.of( "-10" ),
				Arguments.of( "-14.99" ),
				Arguments.of( "15l" )
		);
	}

	@ParameterizedTest
	@MethodSource("testValidPositiveOrZeroValidatorData")
	public void testValidPositiveOrZeroValidator(String value) {
		ConstraintAnnotationDescriptor.Builder<PositiveOrZero> descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( PositiveOrZero.class );
		descriptorBuilder.setMessage( "{validator.positiveOrZero}" );
		PositiveOrZero m = descriptorBuilder.build().getAnnotation();

		PositiveOrZeroValidatorForCharSequence constraint = new PositiveOrZeroValidatorForCharSequence();
		constraint.initialize( m );

		assertTrue( constraint.isValid( value, null ) );
	}

	private static Stream<Arguments> testValidPositiveOrZeroValidatorData() {
		return Stream.of(
				Arguments.of( (String) null ),
				Arguments.of( "15" ),
				Arguments.of( "15.0" ),
				Arguments.of( "0" )
		);
	}

	@ParameterizedTest
	@MethodSource("testInvalidPositiveOrZeroValidatorData")
	public void testInvalidPositiveOrZeroValidator(String value) {
		ConstraintAnnotationDescriptor.Builder<PositiveOrZero> descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( PositiveOrZero.class );
		descriptorBuilder.setMessage( "{validator.positiveOrZero}" );
		PositiveOrZero m = descriptorBuilder.build().getAnnotation();

		PositiveOrZeroValidatorForCharSequence constraint = new PositiveOrZeroValidatorForCharSequence();
		constraint.initialize( m );

		assertFalse( constraint.isValid( value, null ) );
	}

	private static Stream<Arguments> testInvalidPositiveOrZeroValidatorData() {
		return Stream.of(
				Arguments.of( "-10" ),
				Arguments.of( "-14.99" ),
				Arguments.of( "15l" )
		);
	}

	@ParameterizedTest
	@MethodSource("testValidNegativeValidatorData")
	public void testValidNegativeValidator(String value) {
		ConstraintAnnotationDescriptor.Builder<Negative> descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( Negative.class );
		descriptorBuilder.setMessage( "{validator.negative}" );
		Negative m = descriptorBuilder.build().getAnnotation();

		NegativeValidatorForCharSequence constraint = new NegativeValidatorForCharSequence();
		constraint.initialize( m );

		assertTrue( constraint.isValid( value, null ) );
	}

	private static Stream<Arguments> testValidNegativeValidatorData() {
		return Stream.of(
				Arguments.of( (String) null ),
				Arguments.of( "-10" ),
				Arguments.of( "-14.99" )
		);
	}

	@ParameterizedTest
	@MethodSource("testInvalidNegativeValidatorData")
	public void testInvalidNegativeValidator(String value) {
		ConstraintAnnotationDescriptor.Builder<Negative> descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( Negative.class );
		descriptorBuilder.setMessage( "{validator.negative}" );
		Negative m = descriptorBuilder.build().getAnnotation();

		NegativeValidatorForCharSequence constraint = new NegativeValidatorForCharSequence();
		constraint.initialize( m );

		assertFalse( constraint.isValid( value, null ) );
	}

	private static Stream<Arguments> testInvalidNegativeValidatorData() {
		return Stream.of(
				Arguments.of( "15" ),
				Arguments.of( "15.0" ),
				Arguments.of( "0" ),
				Arguments.of( "15l" )
		);
	}

	@ParameterizedTest
	@MethodSource("testValidNegativeOrZeroValidatorData")
	public void testValidNegativeOrZeroValidator(String value) {
		ConstraintAnnotationDescriptor.Builder<NegativeOrZero> descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( NegativeOrZero.class );
		descriptorBuilder.setMessage( "{validator.negativeOrZero}" );
		NegativeOrZero m = descriptorBuilder.build().getAnnotation();

		NegativeOrZeroValidatorForCharSequence constraint = new NegativeOrZeroValidatorForCharSequence();
		constraint.initialize( m );

		assertTrue( constraint.isValid( value, null ) );
	}

	private static Stream<Arguments> testValidNegativeOrZeroValidatorData() {
		return Stream.of(
				Arguments.of( (String) null ),
				Arguments.of( "0" ),
				Arguments.of( "-10" ),
				Arguments.of( "-14.99" )
		);
	}

	@ParameterizedTest
	@MethodSource("testInvalidNegativeOrZeroValidatorData")
	public void testInvalidNegativeOrZeroValidator(String value) {
		ConstraintAnnotationDescriptor.Builder<NegativeOrZero> descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( NegativeOrZero.class );
		descriptorBuilder.setMessage( "{validator.negativeOrZero}" );
		NegativeOrZero m = descriptorBuilder.build().getAnnotation();

		NegativeOrZeroValidatorForCharSequence constraint = new NegativeOrZeroValidatorForCharSequence();
		constraint.initialize( m );

		assertFalse( constraint.isValid( value, null ) );
	}

	private static Stream<Arguments> testInvalidNegativeOrZeroValidatorData() {
		return Stream.of(
				Arguments.of( "15" ),
				Arguments.of( "15.0" ),
				// number format exception
				Arguments.of( "15l" )
		);
	}
}
