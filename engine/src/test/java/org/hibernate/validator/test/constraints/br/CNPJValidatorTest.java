/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.constraints.br;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import org.hibernate.validator.constraints.br.CNPJ;
import org.hibernate.validator.testutil.TestForIssue;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNumberOfViolations;
import static org.hibernate.validator.testutil.ValidatorUtil.getValidator;

public class CNPJValidatorTest {
	private Validator validator;

	@BeforeMethod
	public void setUp() {
		validator = getValidator();
	}

	@Test
	@TestForIssue(jiraKey = "HV-491")
	public void correct_cnpj_with_separator_validates() {
		Set<ConstraintViolation<Company>> violations = validator.validate( new Company( "91.509.901/0001-69" ) );
		assertNumberOfViolations( violations, 0 );
	}

	@Test
	@TestForIssue(jiraKey = "HV-933")
	public void correct_cnpj_without_separator_validates() {
		Set<ConstraintViolation<Company>> violations = validator.validate( new Company( "91509901000169" ) );
		assertNumberOfViolations( violations, 0 );
	}

	@Test
	@TestForIssue(jiraKey = "HV-491")
	public void incorrect_cnpj_with_separator_creates_constraint_violation() {
		Set<ConstraintViolation<Company>> violations = validator.validate( new Company( "91.509.901/0001-60" ) );
		assertNumberOfViolations( violations, 1 );
	}

	@Test
	@TestForIssue(jiraKey = "HV-933")
	public void incorrect_cnpj_without_separator_creates_constraint_violation() {
		Set<ConstraintViolation<Company>> violations = validator.validate( new Company( "91509901000160" ) );
		assertNumberOfViolations( violations, 1 );
	}

	@Test
	public void correct_cnpj_with_check_digit_zero_validates() {
		Set<ConstraintViolation<Company>> violations = validator.validate( new Company( "07755311000100" ) );
		assertNumberOfViolations( violations, 0 );
	}

	public static class Company {
		@CNPJ
		private String cnpj;

		public Company(String cnpj) {
			this.cnpj = cnpj;
		}
	}
}
