/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.constraints.annotations.hv.py;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import org.hibernate.validator.constraints.py.RUC;
import org.hibernate.validator.test.constraints.annotations.AbstractConstrainedTest;

import org.testng.annotations.Test;

public class RUCValidatorTest extends AbstractConstrainedTest {

	@Test
	public void valid_ruc_without_separator_validates() {
		assertNoViolations( validator.validate( new Taxpayer( "800097351" ) ) );
		assertNoViolations( validator.validate( new Taxpayer( "1234567A2" ) ) );
		assertNoViolations( validator.validate( new Taxpayer( "A1234565" ) ) );
		assertNoViolations( validator.validate( new Taxpayer( "123A0" ) ) );
	}

	@Test
	public void valid_ruc_with_separator_validates() {
		assertNoViolations( validator.validate( new Taxpayer( "80009735-1" ) ) );
		assertNoViolations( validator.validate( new Taxpayer( "1234567A-2" ) ) );
		assertNoViolations( validator.validate( new Taxpayer( "a123456-5" ) ) );
		assertNoViolations( validator.validate( new Taxpayer( "123A-0" ) ) );
	}

	@Test
	public void null_ruc_validates() {
		assertNoViolations( validator.validate( new Taxpayer( null ) ) );
	}

	@Test
	public void invalid_ruc_creates_constraint_violation() {
		assertThat( validator.validate( new Taxpayer( "80009735-2" ) ) )
				.containsOnlyViolations(
						violationOf( RUC.class ).withProperty( "ruc" )
				);
		assertThat( validator.validate( new Taxpayer( "1234567A-3" ) ) )
				.containsOnlyViolations(
						violationOf( RUC.class ).withProperty( "ruc" )
				);
		assertThat( validator.validate( new Taxpayer( "80009735-A" ) ) )
				.containsOnlyViolations(
						violationOf( RUC.class ).withProperty( "ruc" )
				);
		assertThat( validator.validate( new Taxpayer( "80009735/1" ) ) )
				.containsOnlyViolations(
						violationOf( RUC.class ).withProperty( "ruc" )
				);
		assertThat( validator.validate( new Taxpayer( "12345678901-5" ) ) )
				.containsOnlyViolations(
						violationOf( RUC.class ).withProperty( "ruc" )
				);
		assertThat( validator.validate( new Taxpayer( "123456789012-1" ) ) )
				.containsOnlyViolations(
						violationOf( RUC.class ).withProperty( "ruc" )
				);
		assertThat( validator.validate( new Taxpayer( "1234567890121" ) ) )
				.containsOnlyViolations(
						violationOf( RUC.class ).withProperty( "ruc" )
				);
	}

	public static class Taxpayer {

		@RUC
		private String ruc;

		public Taxpayer(String ruc) {
			this.ruc = ruc;
		}
	}
}
