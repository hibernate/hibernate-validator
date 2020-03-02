/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.constraints.annotations.hv.br;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import java.util.Set;

import jakarta.validation.ConstraintViolation;

import org.hibernate.validator.constraints.br.CNPJ;
import org.hibernate.validator.test.constraints.annotations.AbstractConstrainedTest;
import org.hibernate.validator.testutil.TestForIssue;

import org.testng.annotations.Test;

public class CNPJValidatorTest extends AbstractConstrainedTest {

	@Test
	@TestForIssue(jiraKey = "HV-491")
	public void correct_cnpj_with_separator_validates() {
		Set<ConstraintViolation<Company>> violations = validator.validate( new Company( "91.509.901/0001-69" ) );
		assertNoViolations( violations );
	}

	@Test
	@TestForIssue(jiraKey = "HV-933")
	public void correct_cnpj_without_separator_validates() {
		Set<ConstraintViolation<Company>> violations = validator.validate( new Company( "91509901000169" ) );
		assertNoViolations( violations );
	}

	@Test
	@TestForIssue(jiraKey = "HV-491")
	public void incorrect_cnpj_with_separator_creates_constraint_violation() {
		Set<ConstraintViolation<Company>> violations = validator.validate( new Company( "91.509.901/0001-60" ) );
		assertThat( violations ).containsOnlyViolations(
				violationOf( CNPJ.class ).withProperty( "cnpj" )
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-933")
	public void incorrect_cnpj_without_separator_creates_constraint_violation() {
		Set<ConstraintViolation<Company>> violations = validator.validate( new Company( "91509901000160" ) );
		assertThat( violations ).containsOnlyViolations(
				violationOf( CNPJ.class ).withProperty( "cnpj" )
		);
	}

	@Test
	public void correct_cnpj_with_check_digit_zero_validates() {
		Set<ConstraintViolation<Company>> violations = validator.validate( new Company( "07755311000100" ) );
		assertNoViolations( violations );
	}

	public static class Company {
		@CNPJ
		private String cnpj;

		public Company(String cnpj) {
			this.cnpj = cnpj;
		}
	}
}
