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
import org.hibernate.validator.cfg.defs.UpperCaseDef;
import org.hibernate.validator.constraints.UpperCase;
import org.hibernate.validator.internal.constraintvalidators.hv.UpperCaseValidator;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutils.ValidatorUtil;

import org.testng.annotations.Test;

/**
 * Tests the {@link UpperCaseValidator} constraint validator.
 *
 * @author Andrea Boriero
 */
@TestForIssue(jiraKey = "HV-2258")
public class UpperCaseValidatorTest {

	private final UpperCaseValidator validator = new UpperCaseValidator();

	@Test
	public void nullIsValid() {
		assertTrue( validator.isValid( null, null ) );
	}

	@Test
	public void emptyIsValid() {
		assertTrue( validator.isValid( "", null ) );
	}

	@Test
	public void allUppercaseIsValid() {
		assertTrue( validator.isValid( "HELLO", null ) );
		assertTrue( validator.isValid( "WORLD", null ) );
		assertTrue( validator.isValid( "ABC", null ) );
	}

	@Test
	public void uppercaseWithDigitsIsValid() {
		assertTrue( validator.isValid( "HELLO123", null ) );
		assertTrue( validator.isValid( "ABC123DEF", null ) );
		assertTrue( validator.isValid( "123", null ) );
	}

	@Test
	public void uppercaseWithPunctuationIsValid() {
		assertTrue( validator.isValid( "HELLO-WORLD", null ) );
		assertTrue( validator.isValid( "HELLO, WORLD!", null ) );
		assertTrue( validator.isValid( "HELLO_WORLD", null ) );
		assertTrue( validator.isValid( "---", null ) );
	}

	@Test
	public void uppercaseWithWhitespaceIsValid() {
		assertTrue( validator.isValid( "HELLO WORLD", null ) );
		assertTrue( validator.isValid( "A B C", null ) );
		assertTrue( validator.isValid( "   ", null ) );
	}

	@Test
	public void nonAsciiUppercaseIsValid() {
		// German umlauts
		assertTrue( validator.isValid( "ÄÖÜ", null ) );
		// Greek uppercase
		assertTrue( validator.isValid( "ΑΒΓ", null ) );
		// Turkish capital I with dot
		assertTrue( validator.isValid( "İ", null ) );
	}

	@Test
	public void mixedCaseIsInvalid() {
		assertFalse( validator.isValid( "Hello", null ) );
		assertFalse( validator.isValid( "HELLO world", null ) );
		assertFalse( validator.isValid( "HeLLo", null ) );
	}

	@Test
	public void lowercaseIsInvalid() {
		assertFalse( validator.isValid( "hello", null ) );
		assertFalse( validator.isValid( "world", null ) );
		assertFalse( validator.isValid( "abc", null ) );
	}

	@Test
	public void lowercaseWithDigitsIsInvalid() {
		assertFalse( validator.isValid( "hello123", null ) );
		assertFalse( validator.isValid( "123abc", null ) );
	}

	@Test
	public void nonAsciiLowercaseIsInvalid() {
		// German umlauts lowercase
		assertFalse( validator.isValid( "äöü", null ) );
		// Greek lowercase
		assertFalse( validator.isValid( "αβγ", null ) );
		// Turkish lowercase i
		assertFalse( validator.isValid( "i", null ) );
		// German sharp s (has no uppercase equivalent in traditional German)
		assertFalse( validator.isValid( "ß", null ) );
	}

	@Test
	public void testProgrammaticDefinition() throws Exception {
		HibernateValidatorConfiguration config = ValidatorUtil.getConfiguration( HibernateValidator.class );
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( Foo.class )
				.field( "string" )
				.constraint( new UpperCaseDef() );
		config.addMapping( mapping );
		Validator programmaticValidator = config.buildValidatorFactory().getValidator();

		Set<ConstraintViolation<Foo>> violations = programmaticValidator.validate( new Foo( "HELLO" ) );
		assertNoViolations( violations );

		violations = programmaticValidator.validate( new Foo( null ) );
		assertNoViolations( violations );

		violations = programmaticValidator.validate( new Foo( "hello" ) );
		assertThat( violations ).containsOnlyViolations(
				violationOf( UpperCase.class )
		);
	}

	private static class Foo {

		private final String string;

		public Foo(String string) {
			this.string = string;
		}
	}
}
