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
import org.hibernate.validator.cfg.defs.LowerCaseDef;
import org.hibernate.validator.constraints.LowerCase;
import org.hibernate.validator.internal.constraintvalidators.hv.LowerCaseValidator;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutils.ValidatorUtil;

import org.testng.annotations.Test;

/**
 * Tests the {@link LowerCaseValidator} constraint validator.
 *
 * @author Andrea Boriero
 */
@TestForIssue(jiraKey = "HV-2258")
public class LowerCaseValidatorTest {

	private final LowerCaseValidator validator = new LowerCaseValidator();

	@Test
	public void nullIsValid() {
		assertTrue( validator.isValid( null, null ) );
	}

	@Test
	public void emptyIsValid() {
		assertTrue( validator.isValid( "", null ) );
	}

	@Test
	public void allLowercaseIsValid() {
		assertTrue( validator.isValid( "hello", null ) );
		assertTrue( validator.isValid( "world", null ) );
		assertTrue( validator.isValid( "abc", null ) );
	}

	@Test
	public void lowercaseWithDigitsIsValid() {
		assertTrue( validator.isValid( "hello123", null ) );
		assertTrue( validator.isValid( "abc123def", null ) );
		assertTrue( validator.isValid( "123", null ) );
	}

	@Test
	public void lowercaseWithPunctuationIsValid() {
		assertTrue( validator.isValid( "hello-world", null ) );
		assertTrue( validator.isValid( "hello, world!", null ) );
		assertTrue( validator.isValid( "hello_world", null ) );
		assertTrue( validator.isValid( "---", null ) );
	}

	@Test
	public void lowercaseWithWhitespaceIsValid() {
		assertTrue( validator.isValid( "hello world", null ) );
		assertTrue( validator.isValid( "a b c", null ) );
		assertTrue( validator.isValid( "   ", null ) );
	}

	@Test
	public void nonAsciiLowercaseIsValid() {
		// German umlauts lowercase
		assertTrue( validator.isValid( "äöü", null ) );
		// Greek lowercase
		assertTrue( validator.isValid( "αβγ", null ) );
		// Turkish lowercase i
		assertTrue( validator.isValid( "i", null ) );
	}

	@Test
	public void mixedCaseIsInvalid() {
		assertFalse( validator.isValid( "Hello", null ) );
		assertFalse( validator.isValid( "hello WORLD", null ) );
		assertFalse( validator.isValid( "HeLLo", null ) );
	}

	@Test
	public void uppercaseIsInvalid() {
		assertFalse( validator.isValid( "HELLO", null ) );
		assertFalse( validator.isValid( "WORLD", null ) );
		assertFalse( validator.isValid( "ABC", null ) );
	}

	@Test
	public void uppercaseWithDigitsIsInvalid() {
		assertFalse( validator.isValid( "HELLO123", null ) );
		assertFalse( validator.isValid( "123ABC", null ) );
	}

	@Test
	public void nonAsciiUppercaseIsInvalid() {
		// German umlauts uppercase
		assertFalse( validator.isValid( "ÄÖÜ", null ) );
		// Greek uppercase
		assertFalse( validator.isValid( "ΑΒΓ", null ) );
		// Turkish capital I with dot
		assertFalse( validator.isValid( "İ", null ) );
	}

	@Test
	public void testProgrammaticDefinition() throws Exception {
		HibernateValidatorConfiguration config = ValidatorUtil.getConfiguration( HibernateValidator.class );
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( Foo.class )
				.field( "string" )
				.constraint( new LowerCaseDef() );
		config.addMapping( mapping );
		Validator programmaticValidator = config.buildValidatorFactory().getValidator();

		Set<ConstraintViolation<Foo>> violations = programmaticValidator.validate( new Foo( "hello" ) );
		assertNoViolations( violations );

		violations = programmaticValidator.validate( new Foo( null ) );
		assertNoViolations( violations );

		violations = programmaticValidator.validate( new Foo( "HELLO" ) );
		assertThat( violations ).containsOnlyViolations(
				violationOf( LowerCase.class )
		);
	}

	private static class Foo {

		private final String string;

		public Foo(String string) {
			this.string = string;
		}
	}
}
