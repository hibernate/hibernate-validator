/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.constraintvalidators.hv;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.internal.constraintvalidators.hv.LengthValidator;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;
import org.hibernate.validator.testutil.MyCustomStringImpl;
import org.hibernate.validator.testutil.TestForIssue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests the {@code LengthConstraint}.
 *
 * @author Hardy Ferentschik
 */
public class LengthValidatorTest {

	@ParameterizedTest
	@MethodSource("testValidData")
	public void testValid(String value) {
		ConstraintAnnotationDescriptor.Builder<Length> descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( Length.class );
		descriptorBuilder.setAttribute( "min", 1 );
		descriptorBuilder.setAttribute( "max", 3 );
		descriptorBuilder.setMessage( "{validator.length}" );
		Length l = descriptorBuilder.build().getAnnotation();
		LengthValidator constraint = new LengthValidator();
		constraint.initialize( l );
		assertTrue( constraint.isValid( value, null ) );
	}

	private static Stream<Arguments> testValidData() {
		return Stream.of(
				Arguments.of( (String) null ),
				Arguments.of( "f" ),
				Arguments.of( "fo" ),
				Arguments.of( "foo" )
		);
	}

	@ParameterizedTest
	@MethodSource("testInvalidData")
	public void testInvalid(String value) {
		ConstraintAnnotationDescriptor.Builder<Length> descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( Length.class );
		descriptorBuilder.setAttribute( "min", 1 );
		descriptorBuilder.setAttribute( "max", 3 );
		descriptorBuilder.setMessage( "{validator.length}" );
		Length l = descriptorBuilder.build().getAnnotation();
		LengthValidator constraint = new LengthValidator();
		constraint.initialize( l );
		assertFalse( constraint.isValid( value, null ) );
	}

	private static Stream<Arguments> testInvalidData() {
		return Stream.of(
				Arguments.of( "" ),
				Arguments.of( "foobar" )
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-502")
	public void testIsValidCharSequence() {
		ConstraintAnnotationDescriptor.Builder<Length> descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( Length.class );
		descriptorBuilder.setAttribute( "min", 1 );
		descriptorBuilder.setAttribute( "max", 3 );
		Length l = descriptorBuilder.build().getAnnotation();
		LengthValidator constraint = new LengthValidator();
		constraint.initialize( l );
		assertTrue( constraint.isValid( new MyCustomStringImpl( "foo" ), null ) );
		assertFalse( constraint.isValid( new MyCustomStringImpl( "foobar" ), null ) );
	}

	@Test
	public void testNegativeMinValue() {
		ConstraintAnnotationDescriptor.Builder<Length> descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( Length.class );
		descriptorBuilder.setAttribute( "min", -1 );
		descriptorBuilder.setAttribute( "max", 1 );
		descriptorBuilder.setMessage( "{validator.length}" );
		Length p = descriptorBuilder.build().getAnnotation();

		LengthValidator constraint = new LengthValidator();
		assertThatThrownBy( () -> constraint.initialize( p ) )
				.isInstanceOf( IllegalArgumentException.class );
	}

	@Test
	public void testNegativeMaxValue() {
		ConstraintAnnotationDescriptor.Builder<Length> descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( Length.class );
		descriptorBuilder.setAttribute( "min", 1 );
		descriptorBuilder.setAttribute( "max", -1 );
		descriptorBuilder.setMessage( "{validator.length}" );
		Length p = descriptorBuilder.build().getAnnotation();

		LengthValidator constraint = new LengthValidator();
		assertThatThrownBy( () -> constraint.initialize( p ) )
				.isInstanceOf( IllegalArgumentException.class );
	}

	@Test
	public void testNegativeLength() {
		ConstraintAnnotationDescriptor.Builder<Length> descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( Length.class );
		descriptorBuilder.setAttribute( "min", 5 );
		descriptorBuilder.setAttribute( "max", 4 );
		descriptorBuilder.setMessage( "{validator.length}" );
		Length p = descriptorBuilder.build().getAnnotation();

		LengthValidator constraint = new LengthValidator();
		assertThatThrownBy( () -> constraint.initialize( p ) )
				.isInstanceOf( IllegalArgumentException.class );
	}
}
