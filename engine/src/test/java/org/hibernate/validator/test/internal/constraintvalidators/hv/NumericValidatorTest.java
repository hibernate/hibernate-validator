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
import org.hibernate.validator.cfg.defs.NumericDef;
import org.hibernate.validator.constraints.Numeric;
import org.hibernate.validator.internal.constraintvalidators.hv.NumericValidator;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutils.ValidatorUtil;

import org.testng.annotations.Test;

/**
 * Tests the {@link NumericValidator} constraint validator.
 *
 * @author Andrea Boriero
 */
@TestForIssue(jiraKey = "HV-2285")
public class NumericValidatorTest {

	private final NumericValidator validator = new NumericValidator();

	@Test
	public void nullIsValid() {
		assertTrue( validator.isValid( null, null ) );
	}

	@Test
	public void emptyIsValid() {
		assertTrue( validator.isValid( "", null ) );
	}

	@Test
	public void digitsOnlyIsValid() {
		assertTrue( validator.isValid( "0", null ) );
		assertTrue( validator.isValid( "123", null ) );
		assertTrue( validator.isValid( "0123456789", null ) );
		assertTrue( validator.isValid( "007", null ) );
	}

	@Test
	public void nonAsciiDigitsAreValid() {
		// Arabic-Indic digits
		assertTrue( validator.isValid( "\u0661\u0662\u0663", null ) );
		// Fullwidth digits
		assertTrue( validator.isValid( "\uFF11\uFF12\uFF13", null ) );
		// Devanagari digits mixed with ASCII digits
		assertTrue( validator.isValid( "\u0967\u0968" + "3", null ) );
	}

	@Test
	public void supplementaryDigitsAreInvalid() {
		// MATHEMATICAL BOLD DIGIT ZERO (U+1D7CE) is a surrogate pair; each char on its own is not a digit
		assertFalse( validator.isValid( "\uD835\uDFCE", null ) );
		assertFalse( validator.isValid( "1\uD835\uDFCE2", null ) );
	}

	@Test
	public void lettersAreInvalid() {
		assertFalse( validator.isValid( "abc", null ) );
		assertFalse( validator.isValid( "123abc", null ) );
		assertFalse( validator.isValid( "1e5", null ) );
		assertFalse( validator.isValid( "0x1F", null ) );
	}

	@Test
	public void whitespaceIsInvalid() {
		assertFalse( validator.isValid( " ", null ) );
		assertFalse( validator.isValid( "123 456", null ) );
		assertFalse( validator.isValid( " 123", null ) );
		assertFalse( validator.isValid( "123\n", null ) );
	}

	@Test
	public void signsAndSeparatorsAreInvalid() {
		assertFalse( validator.isValid( "-1", null ) );
		assertFalse( validator.isValid( "+1", null ) );
		assertFalse( validator.isValid( "1.5", null ) );
		assertFalse( validator.isValid( "1,000", null ) );
		assertFalse( validator.isValid( "1_000", null ) );
	}

	@Test
	public void testProgrammaticDefinition() throws Exception {
		HibernateValidatorConfiguration config = ValidatorUtil.getConfiguration( HibernateValidator.class );
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( Foo.class )
				.field( "string" )
				.constraint( new NumericDef() );
		config.addMapping( mapping );
		Validator programmaticValidator = config.buildValidatorFactory().getValidator();

		Set<ConstraintViolation<Foo>> violations = programmaticValidator.validate( new Foo( "123" ) );
		assertNoViolations( violations );

		violations = programmaticValidator.validate( new Foo( null ) );
		assertNoViolations( violations );

		violations = programmaticValidator.validate( new Foo( "12a" ) );
		assertThat( violations ).containsOnlyViolations( violationOf( Numeric.class ) );
	}

	private static class Foo {

		private final String string;

		public Foo(String string) {
			this.string = string;
		}
	}
}
