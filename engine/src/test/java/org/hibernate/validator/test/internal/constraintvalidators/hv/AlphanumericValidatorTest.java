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
import org.hibernate.validator.cfg.defs.AlphanumericDef;
import org.hibernate.validator.constraints.Alphanumeric;
import org.hibernate.validator.internal.constraintvalidators.hv.AlphanumericValidator;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutils.ValidatorUtil;

import org.testng.annotations.Test;

/**
 * Tests the {@link AlphanumericValidator} constraint validator.
 *
 * @author Andrea Boriero
 */
@TestForIssue(jiraKey = "HV-2269")
public class AlphanumericValidatorTest {

	private final AlphanumericValidator validator = new AlphanumericValidator();

	@Test
	public void nullIsValid() {
		assertTrue( validator.isValid( null, null ) );
	}

	@Test
	public void emptyIsValid() {
		assertTrue( validator.isValid( "", null ) );
	}

	@Test
	public void lettersAndDigitsOnlyIsValid() {
		assertTrue( validator.isValid( "hello", null ) );
		assertTrue( validator.isValid( "123", null ) );
		assertTrue( validator.isValid( "hello123", null ) );
		assertTrue( validator.isValid( "HelloWorld42", null ) );
	}

	@Test
	public void nonAsciiLettersAreValid() {
		// German umlauts
		assertTrue( validator.isValid( "äöüÄÖÜ123", null ) );
		// Greek
		assertTrue( validator.isValid( "αβγΑΒΓ123", null ) );
		// Accented Latin
		assertTrue( validator.isValid( "café123", null ) );
	}

	@Test
	public void whitespaceIsInvalid() {
		assertFalse( validator.isValid( "hello world", null ) );
		assertFalse( validator.isValid( " ", null ) );
		assertFalse( validator.isValid( "a b 1", null ) );
	}

	@Test
	public void punctuationIsInvalid() {
		assertFalse( validator.isValid( "hello-world123", null ) );
		assertFalse( validator.isValid( "hello!", null ) );
		assertFalse( validator.isValid( "hello, world123", null ) );
		assertFalse( validator.isValid( "O'Brien123", null ) );
	}

	@Test
	public void testProgrammaticDefinition() throws Exception {
		HibernateValidatorConfiguration config = ValidatorUtil.getConfiguration( HibernateValidator.class );
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( Foo.class )
				.field( "string" )
				.constraint( new AlphanumericDef() );
		config.addMapping( mapping );
		Validator programmaticValidator = config.buildValidatorFactory().getValidator();

		Set<ConstraintViolation<Foo>> violations = programmaticValidator.validate( new Foo( "hello123" ) );
		assertNoViolations( violations );

		violations = programmaticValidator.validate( new Foo( null ) );
		assertNoViolations( violations );

		violations = programmaticValidator.validate( new Foo( "hello world" ) );
		assertThat( violations ).containsOnlyViolations( violationOf( Alphanumeric.class ) );
	}

	private static class Foo {

		private final String string;

		public Foo(String string) {
			this.string = string;
		}
	}
}
