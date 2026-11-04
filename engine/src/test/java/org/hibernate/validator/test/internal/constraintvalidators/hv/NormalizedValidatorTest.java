/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.constraintvalidators.hv;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.Normalizer;
import java.util.stream.Stream;

import org.hibernate.validator.constraints.Normalized;
import org.hibernate.validator.internal.constraintvalidators.hv.NormalizedValidator;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;
import org.hibernate.validator.testutil.MyCustomStringImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests the {@link Normalized} constraint.
 *
 * @author Kazuki Shimizu
 */
public class NormalizedValidatorTest {

	private ConstraintAnnotationDescriptor.Builder<Normalized> descriptorBuilder;

	@BeforeEach
	public void setUp() throws Exception {
		descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( Normalized.class );
	}

	@ParameterizedTest
	@MethodSource("testIsValidData")
	public void testIsValid(String value) {
		descriptorBuilder.setMessage( "{validator.Normalized}" );
		Normalized l = descriptorBuilder.build().getAnnotation();
		NormalizedValidator constraint = new NormalizedValidator();
		constraint.initialize( l );
		assertTrue( constraint.isValid( value, null ) );
	}

	private static Stream<Arguments> testIsValidData() {
		return Stream.of(
				Arguments.of( (String) null ),
				Arguments.of( "" ),
				Arguments.of( "foobar" ),
				Arguments.of( "\uFE64script\uFE65" )
		);
	}

	@Test
	public void testIsValidCharSequence() {
		Normalized l = descriptorBuilder.build().getAnnotation();
		NormalizedValidator constraint = new NormalizedValidator();
		constraint.initialize( l );
		assertTrue( constraint.isValid( new MyCustomStringImpl( "foobar" ), null ) );
		assertTrue( constraint.isValid( new MyCustomStringImpl( "\uFE64script\uFE65" ), null ) );
	}

	@Test
	public void testIsValidNormalizationStrategyIsNfc() {
		descriptorBuilder.setAttribute( "form", Normalizer.Form.NFC );
		Normalized l = descriptorBuilder.build().getAnnotation();
		NormalizedValidator constraint = new NormalizedValidator();
		constraint.initialize( l );
		assertTrue( constraint.isValid( "foobar", null ) );
		assertTrue( constraint.isValid( "\uFE64script\uFE65", null ) );
	}

	@Test
	public void testIsValidNormalizationStrategyIsNfkc() {
		descriptorBuilder.setAttribute( "form", Normalizer.Form.NFKC );
		Normalized l = descriptorBuilder.build().getAnnotation();
		NormalizedValidator constraint = new NormalizedValidator();
		constraint.initialize( l );
		assertTrue( constraint.isValid( "foobar", null ) );
		assertFalse( constraint.isValid( "\uFE64script\uFE65", null ) );
	}

	@Test
	public void testIsValidNormalizationStrategyIsNfd() {
		descriptorBuilder.setAttribute( "form", Normalizer.Form.NFD );
		Normalized l = descriptorBuilder.build().getAnnotation();
		NormalizedValidator constraint = new NormalizedValidator();
		constraint.initialize( l );
		assertTrue( constraint.isValid( "foobar", null ) );
		assertTrue( constraint.isValid( "\uFE64script\uFE65", null ) );
	}

	@Test
	public void testIsValidNormalizationStrategyIsNfkd() {
		descriptorBuilder.setAttribute( "form", Normalizer.Form.NFKD );
		Normalized l = descriptorBuilder.build().getAnnotation();
		NormalizedValidator constraint = new NormalizedValidator();
		constraint.initialize( l );
		assertTrue( constraint.isValid( "foobar", null ) );
		assertFalse( constraint.isValid( "\uFE64script\uFE65", null ) );
	}
}
