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

import org.hibernate.validator.constraints.DateTimeFormat;
import org.hibernate.validator.test.constraints.annotations.AbstractConstrainedTest;

import org.junit.jupiter.api.Test;

/**
 * @author Sean Okafor
 */
public class DateTimeFormatConstrainedTest extends AbstractConstrainedTest {
	@Test
	public void testDateTimeFormatConstraints() {
		Foo foo = new Foo( "07-03-2014" );
		Set<ConstraintViolation<Foo>> violations = validator.validate( foo );
		assertNoViolations( violations );
	}

	@Test
	public void testDateTimeFormatConstraintsInvalid() {
		Foo foo = new Foo( "07-03-33" );
		Set<ConstraintViolation<Foo>> violations = validator.validate( foo );
		assertThat( violations ).containsOnlyViolations(
				violationOf( DateTimeFormat.class )
		);
	}

	private static class Foo {
		@DateTimeFormat(pattern = "dd-MM-yyyy")
		private final String date;

		public Foo(String date) {
			this.date = date;
		}
	}
}
