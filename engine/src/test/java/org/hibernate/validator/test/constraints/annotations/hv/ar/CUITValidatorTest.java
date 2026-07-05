/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.constraints.annotations.hv.ar;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import org.hibernate.validator.constraints.ar.CUIT;
import org.hibernate.validator.test.constraints.annotations.AbstractConstrainedTest;

import org.testng.annotations.Test;

public class CUITValidatorTest extends AbstractConstrainedTest {

	@Test
	public void valid_cuit_without_separator_validates() {
		assertNoViolations( validator.validate( new Person( "20123456786" ) ) );
		assertNoViolations( validator.validate( new Person( "23123456785" ) ) );
		assertNoViolations( validator.validate( new Person( "24123456781" ) ) );
		assertNoViolations( validator.validate( new Person( "25123456788" ) ) );
		assertNoViolations( validator.validate( new Person( "26123456784" ) ) );
		assertNoViolations( validator.validate( new Person( "27123456780" ) ) );
		assertNoViolations( validator.validate( new Person( "30123456781" ) ) );
		assertNoViolations( validator.validate( new Person( "33123456780" ) ) );
		assertNoViolations( validator.validate( new Person( "34123456787" ) ) );
	}

	@Test
	public void valid_cuit_with_separator_validates() {
		assertNoViolations( validator.validate( new Person( "20-12345678-6" ) ) );
		assertNoViolations( validator.validate( new Person( "23-12345678-5" ) ) );
		assertNoViolations( validator.validate( new Person( "24-12345678-1" ) ) );
		assertNoViolations( validator.validate( new Person( "25-12345678-8" ) ) );
		assertNoViolations( validator.validate( new Person( "26-12345678-4" ) ) );
		assertNoViolations( validator.validate( new Person( "27-12345678-0" ) ) );
		assertNoViolations( validator.validate( new Person( "30-12345678-1" ) ) );
		assertNoViolations( validator.validate( new Person( "33-12345678-0" ) ) );
		assertNoViolations( validator.validate( new Person( "34-12345678-7" ) ) );
	}

	@Test
	public void null_cuit_validates() {
		assertNoViolations( validator.validate( new Person( null ) ) );
	}

	@Test
	public void invalid_cuit_creates_constraint_violation() {
		assertThat( validator.validate( new Person( "20-12345678-7" ) ) )
				.containsOnlyViolations(
						violationOf( CUIT.class ).withProperty( "cuit" )
				);
		assertThat( validator.validate( new Person( "21-12345678-2" ) ) )
				.containsOnlyViolations(
						violationOf( CUIT.class ).withProperty( "cuit" )
				);
		assertThat( validator.validate( new Person( "3012345678" ) ) )
				.containsOnlyViolations(
						violationOf( CUIT.class ).withProperty( "cuit" )
				);
		assertThat( validator.validate( new Person( "30.12345678.1" ) ) )
				.containsOnlyViolations(
						violationOf( CUIT.class ).withProperty( "cuit" )
				);
	}

	public static class Person {

		@CUIT
		private String cuit;

		public Person(String cuit) {
			this.cuit = cuit;
		}
	}
}
