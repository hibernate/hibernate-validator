/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.constraintvalidators.hv;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.defs.PortDef;
import org.hibernate.validator.constraints.Port;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutils.ValidatorUtil;

import org.testng.annotations.Test;

/**
 * Tests for the {@link Port} constraint.
 *
 * @author Koen Aers
 */
@TestForIssue(jiraKey = "HV-2220")
public class PortConstraintTest {

	private final Validator validator = ValidatorUtil.getValidator();

	@Test
	public void nullIsValid() {
		Set<ConstraintViolation<Foo>> violations =
				validator.validate( new Foo( null ) );
		assertNoViolations( violations );
	}

	@Test
	public void validPorts() {
		assertNoViolations( validator.validate( new Foo( 0 ) ) );
		assertNoViolations( validator.validate( new Foo( 80 ) ) );
		assertNoViolations( validator.validate( new Foo( 443 ) ) );
		assertNoViolations( validator.validate( new Foo( 8080 ) ) );
		assertNoViolations( validator.validate( new Foo( 65535 ) ) );
	}

	@Test
	public void invalidPorts() {
		assertThat( validator.validate( new Foo( -1 ) ) ).containsOnlyViolations(
				violationOf( Port.class )
		);
		assertThat( validator.validate( new Foo( 65536 ) ) ).containsOnlyViolations(
				violationOf( Port.class )
		);
		assertThat( validator.validate( new Foo( 100000 ) ) ).containsOnlyViolations(
				violationOf( Port.class )
		);
	}

	@Test
	public void testProgrammaticDefinition() throws Exception {
		HibernateValidatorConfiguration config = ValidatorUtil.getConfiguration( HibernateValidator.class );
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( Bar.class )
				.field( "port" )
				.constraint( new PortDef() );
		config.addMapping( mapping );
		Validator programmaticValidator = config.buildValidatorFactory().getValidator();

		Set<ConstraintViolation<Bar>> violations = programmaticValidator.validate( new Bar( 8080 ) );
		assertNoViolations( violations );

		violations = programmaticValidator.validate( new Bar( null ) );
		assertNoViolations( violations );

		violations = programmaticValidator.validate( new Bar( 70000 ) );
		assertThat( violations ).containsOnlyViolations(
				violationOf( Port.class )
		);
	}

	private static class Foo {

		@Port
		private final Integer port;

		public Foo(Integer port) {
			this.port = port;
		}
	}

	private static class Bar {

		private final Integer port;

		public Bar(Integer port) {
			this.port = port;
		}
	}
}
