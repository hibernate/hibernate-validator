/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.constraintvalidators.bv;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.hibernate.validator.testutils.ValidatorUtil.getValidator;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotBlank;

import org.hibernate.validator.internal.constraintvalidators.bv.NotBlankValidator;

import org.testng.annotations.Test;

/**
 * @author Hardy Ferentschik
 */
public class BlankValidatorTest {
	@Test
	public void testConstraintValidator() {
		NotBlankValidator constraintValidator = new NotBlankValidator();

		assertTrue( constraintValidator.isValid( "a", null ) );
		assertFalse( constraintValidator.isValid( null, null ) );
		assertFalse( constraintValidator.isValid( "", null ) );
		assertFalse( constraintValidator.isValid( " ", null ) );
		assertFalse( constraintValidator.isValid( "\t", null ) );
		assertFalse( constraintValidator.isValid( "\n", null ) );
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
