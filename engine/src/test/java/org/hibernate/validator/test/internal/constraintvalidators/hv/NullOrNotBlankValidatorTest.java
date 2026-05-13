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
import org.hibernate.validator.cfg.defs.NullOrNotBlankDef;
import org.hibernate.validator.constraints.NullOrNotBlank;
import org.hibernate.validator.internal.constraintvalidators.hv.NullOrNotBlankValidator;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutils.ValidatorUtil;

import org.testng.annotations.Test;

/**
 * Tests the {@link NullOrNotBlankValidator} constraint validator.
 *
 * @author Koen Aers
 */
@TestForIssue(jiraKey = "HV-2193")
public class NullOrNotBlankValidatorTest {

	private final NullOrNotBlankValidator validator = new NullOrNotBlankValidator();

	@Test
	public void nullIsValid() {
		assertTrue( validator.isValid( null, null ) );
	}

	@Test
	public void notBlankIsValid() {
		assertTrue( validator.isValid( "a", null ) );
		assertTrue( validator.isValid( "foobar", null ) );
		assertTrue( validator.isValid( " a ", null ) );
	}

	@Test
	public void emptyIsInvalid() {
		assertFalse( validator.isValid( "", null ) );
	}

	@Test
	public void blankIsInvalid() {
		assertFalse( validator.isValid( " ", null ) );
		assertFalse( validator.isValid( "\t", null ) );
		assertFalse( validator.isValid( "\n", null ) );
		assertFalse( validator.isValid( "   \t\n  ", null ) );
	}

	@Test
	public void testProgrammaticDefinition() throws Exception {
		HibernateValidatorConfiguration config = ValidatorUtil.getConfiguration( HibernateValidator.class );
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( Foo.class )
				.field( "string" )
				.constraint( new NullOrNotBlankDef() );
		config.addMapping( mapping );
		Validator programmaticValidator = config.buildValidatorFactory().getValidator();

		Set<ConstraintViolation<Foo>> violations = programmaticValidator.validate( new Foo( "foobar" ) );
		assertNoViolations( violations );

		violations = programmaticValidator.validate( new Foo( null ) );
		assertNoViolations( violations );

		violations = programmaticValidator.validate( new Foo( "   " ) );
		assertThat( violations ).containsOnlyViolations(
				violationOf( NullOrNotBlank.class )
		);
	}

	private static class Foo {

		private final String string;

		public Foo(String string) {
			this.string = string;
		}
	}
}
