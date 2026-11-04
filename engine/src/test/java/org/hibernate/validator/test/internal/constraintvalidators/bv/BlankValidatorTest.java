/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.constraintvalidators.bv;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.hibernate.validator.testutils.ValidatorUtil.getValidator;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.stream.Stream;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotBlank;

import org.hibernate.validator.internal.constraintvalidators.bv.NotBlankValidator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * @author Hardy Ferentschik
 */
public class BlankValidatorTest {
	@ParameterizedTest
	@MethodSource("testConstraintValidatorValidData")
	public void testConstraintValidatorValid(String value) {
		NotBlankValidator constraintValidator = new NotBlankValidator();

		assertTrue( constraintValidator.isValid( value, null ) );
	}

	private static Stream<Arguments> testConstraintValidatorValidData() {
		return Stream.of(
				Arguments.of( "a" )
		);
	}

	@ParameterizedTest
	@MethodSource("testConstraintValidatorInvalidData")
	public void testConstraintValidatorInvalid(String value) {
		NotBlankValidator constraintValidator = new NotBlankValidator();

		assertFalse( constraintValidator.isValid( value, null ) );
	}

	private static Stream<Arguments> testConstraintValidatorInvalidData() {
		return Stream.of(
				Arguments.of( (String) null ),
				Arguments.of( "" ),
				Arguments.of( " " ),
				Arguments.of( "\t" ),
				Arguments.of( "\n" )
		);
	}

	@Test
	public void testNotBlank() {
		Validator validator = getValidator();
		Foo foo = new Foo();

		Set<ConstraintViolation<Foo>> constraintViolations = validator.validate( foo );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( NotBlank.class )
		);

		foo.name = "";
		constraintViolations = validator.validate( foo );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( NotBlank.class )
		);

		foo.name = " ";
		constraintViolations = validator.validate( foo );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( NotBlank.class )
		);

		foo.name = "\t";
		constraintViolations = validator.validate( foo );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( NotBlank.class )
		);

		foo.name = "\n";
		constraintViolations = validator.validate( foo );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( NotBlank.class )
		);

		foo.name = "john doe";
		constraintViolations = validator.validate( foo );
		assertNoViolations( constraintViolations );
	}

	class Foo {
		@NotBlank
		String name;
	}
}
