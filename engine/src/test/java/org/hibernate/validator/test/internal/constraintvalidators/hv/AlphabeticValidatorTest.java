/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.constraintvalidators.hv;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.defs.AlphabeticDef;
import org.hibernate.validator.constraints.Alphabetic;
import org.hibernate.validator.internal.constraintvalidators.hv.AlphabeticValidator;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutils.ValidatorUtil;

import org.testng.annotations.Test;

/**
 * Tests the {@link AlphabeticValidator} constraint validator.
 *
 * @author Andrea Boriero
 */
@TestForIssue(jiraKey = "HV-2261")
public class AlphabeticValidatorTest {

	private final AlphabeticValidator validator = new AlphabeticValidator();

	@Test
	public void nullIsValid() {
		assertTrue( validator.isValid( null, null ) );
	}

	@Test
	public void emptyIsValid() {
		assertTrue( validator.isValid( "", null ) );
	}

	@Test
	public void lettersOnlyIsValid() {
		assertTrue( validator.isValid( "hello", null ) );
		assertTrue( validator.isValid( "WORLD", null ) );
		assertTrue( validator.isValid( "HelloWorld", null ) );
	}

	@Test
	public void nonAsciiLettersAreValid() {
		// German umlauts
		assertTrue( validator.isValid( "äöüÄÖÜ", null ) );
		// Greek
		assertTrue( validator.isValid( "αβγΑΒΓ", null ) );
		// Accented Latin
		assertTrue( validator.isValid( "café", null ) );
	}

	@Test
	public void digitsAreInvalid() {
		assertFalse( validator.isValid( "hello123", null ) );
		assertFalse( validator.isValid( "123", null ) );
		assertFalse( validator.isValid( "abc123def", null ) );
	}

	@Test
	public void whitespaceIsInvalid() {
		assertFalse( validator.isValid( "hello world", null ) );
		assertFalse( validator.isValid( " ", null ) );
		assertFalse( validator.isValid( "a b", null ) );
	}

	@Test
	public void punctuationIsInvalid() {
		assertFalse( validator.isValid( "hello-world", null ) );
		assertFalse( validator.isValid( "hello!", null ) );
		assertFalse( validator.isValid( "hello, world", null ) );
		assertFalse( validator.isValid( "O'Brien", null ) );
	}

	@Test
	public void testProgrammaticDefinition() throws Exception {
		HibernateValidatorConfiguration config = ValidatorUtil.getConfiguration( HibernateValidator.class );
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( Foo.class )
				.field( "string" )
				.constraint( new AlphabeticDef() );
		config.addMapping( mapping );
		Validator programmaticValidator = config.buildValidatorFactory().getValidator();

		Set<ConstraintViolation<Foo>> violations = programmaticValidator.validate( new Foo( "hello" ) );
		assertNoViolations( violations );

		violations = programmaticValidator.validate( new Foo( null ) );
		assertNoViolations( violations );

		violations = programmaticValidator.validate( new Foo( "hello123" ) );
		assertThat( violations ).containsOnlyViolations( violationOf( Alphabetic.class ) );
	}

	private static class Foo {

		private final String string;

		public Foo(String string) {
			this.string = string;
		}
	}
}
