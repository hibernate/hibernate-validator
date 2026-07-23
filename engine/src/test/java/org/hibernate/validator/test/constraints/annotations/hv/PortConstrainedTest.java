/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.constraints.annotations.hv;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import java.util.Set;

import jakarta.validation.ConstraintViolation;

import org.hibernate.validator.constraints.Port;
import org.hibernate.validator.test.constraints.annotations.AbstractConstrainedTest;
import org.hibernate.validator.testutil.TestForIssue;

import org.junit.jupiter.api.Test;

/**
 * @author Koen Aers
 */
@TestForIssue(jiraKey = "HV-2220")
public class PortConstrainedTest extends AbstractConstrainedTest {

	@Test
	public void nullIsValid() {
		Set<ConstraintViolation<Foo>> violations =
				validator.validate( new Foo( null ) );
		assertNoViolations( violations );
	}

	@Test
	public void validPortIsValid() {
		Set<ConstraintViolation<Foo>> violations =
				validator.validate( new Foo( 8080 ) );
		assertNoViolations( violations );
	}

	@Test
	public void zeroIsValid() {
		Set<ConstraintViolation<Foo>> violations =
				validator.validate( new Foo( 0 ) );
		assertNoViolations( violations );
	}

	@Test
	public void maxPortIsValid() {
		Set<ConstraintViolation<Foo>> violations =
				validator.validate( new Foo( 65535 ) );
		assertNoViolations( violations );
	}

	@Test
	public void negativePortIsInvalid() {
		Set<ConstraintViolation<Foo>> violations =
				validator.validate( new Foo( -1 ) );
		assertThat( violations ).containsOnlyViolations(
				violationOf( Port.class )
		);
	}

	@Test
	public void portAboveMaxIsInvalid() {
		Set<ConstraintViolation<Foo>> violations =
				validator.validate( new Foo( 65536 ) );
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
}
