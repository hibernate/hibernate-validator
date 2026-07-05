/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.constraints.annotations.hv.ar;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import org.hibernate.validator.constraints.ar.CUIL;
import org.hibernate.validator.test.constraints.annotations.AbstractConstrainedTest;

import org.testng.annotations.Test;

public class CUILValidatorTest extends AbstractConstrainedTest {

	@Test
	public void valid_cuil_without_separator_validates() {
		assertNoViolations( validator.validate( new Person( "20123456786" ) ) );
		assertNoViolations( validator.validate( new Person( "23123456785" ) ) );
		assertNoViolations( validator.validate( new Person( "24123456781" ) ) );
		assertNoViolations( validator.validate( new Person( "25123456788" ) ) );
		assertNoViolations( validator.validate( new Person( "26123456784" ) ) );
		assertNoViolations( validator.validate( new Person( "27123456780" ) ) );
	}

	@Test
	public void valid_cuil_with_separator_validates() {
		assertNoViolations( validator.validate( new Person( "20-12345678-6" ) ) );
		assertNoViolations( validator.validate( new Person( "23-12345678-5" ) ) );
		assertNoViolations( validator.validate( new Person( "24-12345678-1" ) ) );
		assertNoViolations( validator.validate( new Person( "25-12345678-8" ) ) );
		assertNoViolations( validator.validate( new Person( "26-12345678-4" ) ) );
		assertNoViolations( validator.validate( new Person( "27-12345678-0" ) ) );
	}

	@Test
	public void null_cuil_validates() {
		assertNoViolations( validator.validate( new Person( null ) ) );
	}

	@Test
	public void invalid_cuil_creates_constraint_violation() {
		assertThat( validator.validate( new Person( "20-12345678-7" ) ) )
				.containsOnlyViolations(
						violationOf( CUIL.class ).withProperty( "cuil" )
				);
		assertThat( validator.validate( new Person( "30-12345678-1" ) ) )
				.containsOnlyViolations(
						violationOf( CUIL.class ).withProperty( "cuil" )
				);
		assertThat( validator.validate( new Person( "2012345678" ) ) )
				.containsOnlyViolations(
						violationOf( CUIL.class ).withProperty( "cuil" )
				);
		assertThat( validator.validate( new Person( "20.12345678.6" ) ) )
				.containsOnlyViolations(
						violationOf( CUIL.class ).withProperty( "cuil" )
				);
		assertThat( validator.validate( new Person( "20-00000001-0" ) ) )
				.containsOnlyViolations(
						violationOf( CUIL.class ).withProperty( "cuil" )
				);
	}

	public static class Person {

		@CUIL
		private String cuil;

		public Person(String cuil) {
			this.cuil = cuil;
		}
	}
}
