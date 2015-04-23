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

import org.hibernate.validator.constraints.br.CPF;
import org.hibernate.validator.testutil.TestForIssue;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNumberOfViolations;
import static org.hibernate.validator.testutil.ValidatorUtil.getValidator;

public class CPFValidatorTest {
	private String[] invalidCPFs = {
			"000.000.000-00", "111.111.111-11", "222.222.222-22",
			"333.333.333-33", "444.444.444-44", "555.555.555-55",
			"666.666.666-66", "777.777.777-77", "888.888.888-88",
			"999.999.999-99"
	};

	private Validator validator;

	@BeforeMethod
	public void setUp() {
		validator = getValidator();
	}

	@Test
	@TestForIssue(jiraKey = "HV-491")
	public void correct_cpf_with_separator_validates() {
		Set<ConstraintViolation<Person>> violations = validator.validate( new Person( "134.241.313-00" ) );
		assertNumberOfViolations( violations, 0 );
	}

	@Test
	@TestForIssue(jiraKey = "HV-933")
	public void correct_cpf_without_separator_validates() {
		Set<ConstraintViolation<Person>> violations = validator.validate( new Person( "13424131300" ) );
		assertNumberOfViolations( violations, 0 );
	}

	@Test
	@TestForIssue(jiraKey = "HV-491")
	public void incorrect_formatted_cpf_is_invalid() {
		Set<ConstraintViolation<Person>> violations = validator.validate( new Person( "48255-77" ) );
		assertNumberOfViolations( violations, 1 );
	}

	@Test
	@TestForIssue(jiraKey = "HV-808")
	public void any_same_digit_cpf_with_separator_is_invalid() {
		for ( String cpf : invalidCPFs ) {
			Set<ConstraintViolation<Person>> violations = validator.validate( new Person( cpf ) );
			assertNumberOfViolations( violations, 1 );
		}
	}

	@Test
	@TestForIssue(jiraKey = "HV-933")
	public void any_same_digit_cpf_without_separator_is_invalid() {
		for ( String cpf : invalidCPFs ) {
			String cpfWithoutseparator = cpf.replaceAll( "[^0-9]", "" );
			Set<ConstraintViolation<Person>> violations = validator.validate( new Person( cpfWithoutseparator ) );
			assertNumberOfViolations( violations, 1 );
		}
	}

	@Test
	@TestForIssue(jiraKey = "HV-808")
	public void valid_cpfs_with_separator_validate() {
		Set<ConstraintViolation<Person>> violations = validator.validate( new Person( "378.796.950-01" ) );
		assertNumberOfViolations( violations, 0 );

		violations = validator.validate( new Person( "331.814.296-43" ) );
		assertNumberOfViolations( violations, 0 );
	}

	@Test
	@TestForIssue(jiraKey = "HV-933")
	public void valid_cpf_without_separator_validates() {
		Set<ConstraintViolation<Person>> violations = validator.validate( new Person( "37879695001" ) );
		assertNumberOfViolations( violations, 0 );

		violations = validator.validate( new Person( "33181429643" ) );
		assertNumberOfViolations( violations, 0 );
	}

	@Test
	@TestForIssue(jiraKey = "HV-979")
	public void correct_cpf_with_dash_only_separator_validates() {
		Set<ConstraintViolation<Person>> violations = validator.validate( new Person( "134241313-00" ) );
		assertNumberOfViolations( violations, 0 );
	}

	@Test
	@TestForIssue(jiraKey = "HV-808")
	public void invalid_cpf_with_separator_creates_constraint_violation() {
		Set<ConstraintViolation<Person>> violations = validator.validate( new Person( "378.796.950-02" ) );
		assertNumberOfViolations( violations, 1 );

		violations = validator.validate( new Person( "331.814.296-52" ) );
		assertNumberOfViolations( violations, 1 );

		violations = validator.validate( new Person( "331.814.296-51" ) );
		assertNumberOfViolations( violations, 1 );
	}

	@Test
	@TestForIssue(jiraKey = "HV-933")
	public void invalid_cpf_without_separator_creates_constraint_violation() {
		Set<ConstraintViolation<Person>> violations = validator.validate( new Person( "37879695002" ) );
		assertNumberOfViolations( violations, 1 );

		violations = validator.validate( new Person( "33181429652" ) );
		assertNumberOfViolations( violations, 1 );

		violations = validator.validate( new Person( "33181429651" ) );
		assertNumberOfViolations( violations, 1 );
	}

	public static class Person {
		@CPF
		private String cpf;

		public Person(String cpf) {
			this.cpf = cpf;
		}
	}
}
