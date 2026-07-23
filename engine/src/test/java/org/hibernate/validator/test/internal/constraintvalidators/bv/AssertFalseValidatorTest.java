/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.constraintvalidators.bv;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;

import org.hibernate.validator.internal.constraintvalidators.bv.AssertFalseValidator;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * @author Alaa Nassef
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AssertFalseValidatorTest {

	private static AssertFalseValidator constraint;

	@BeforeAll
	public static void init() {
		constraint = new AssertFalseValidator();
	}

	@ParameterizedTest
	@MethodSource("testValidData")
	public void testValid(Boolean value) {
		assertTrue( constraint.isValid( value, null ) );
	}

	private static Stream<Arguments> testValidData() {
		return Stream.of(
				Arguments.of( (Boolean) null ),
				Arguments.of( false ),
				Arguments.of( Boolean.FALSE )
		);
	}

	@ParameterizedTest
	@MethodSource("testInvalidData")
	public void testInvalid(Boolean value) {
		assertFalse( constraint.isValid( value, null ) );
	}

	private static Stream<Arguments> testInvalidData() {
		return Stream.of(
				Arguments.of( true ),
				Arguments.of( Boolean.TRUE )
		);
	}
}
