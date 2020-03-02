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

import org.hibernate.validator.constraints.br.CPF;
import org.hibernate.validator.test.constraints.annotations.AbstractConstrainedTest;
import org.hibernate.validator.testutil.TestForIssue;

import org.testng.annotations.Test;

public class CPFValidatorTest extends AbstractConstrainedTest {
	private String[] invalidCPFs = {
			"000.000.000-00", "111.111.111-11", "222.222.222-22",
			"333.333.333-33", "444.444.444-44", "555.555.555-55",
			"666.666.666-66", "777.777.777-77", "888.888.888-88",
			"999.999.999-99"
	};

	@Test
	@TestForIssue(jiraKey = "HV-491")
	public void correct_cpf_with_separator_validates() {
		Set<ConstraintViolation<Person>> violations = validator.validate( new Person( "134.241.313-00" ) );
		assertNoViolations( violations );
	}

	@Test
	@TestForIssue(jiraKey = "HV-933")
	public void correct_cpf_without_separator_validates() {
		Set<ConstraintViolation<Person>> violations = validator.validate( new Person( "13424131300" ) );
		assertNoViolations( violations );
	}

	@Test
	@TestForIssue(jiraKey = "HV-491")
	public void incorrect_formatted_cpf_is_invalid() {
		Set<ConstraintViolation<Person>> violations = validator.validate( new Person( "48255-77" ) );
		assertThat( violations ).containsOnlyViolations(
				violationOf( CPF.class ).withProperty( "cpf" )
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-808")
	public void any_same_digit_cpf_with_separator_is_invalid() {
		for ( String cpf : invalidCPFs ) {
			Set<ConstraintViolation<Person>> violations = validator.validate( new Person( cpf ) );
			assertThat( violations ).containsOnlyViolations(
					violationOf( CPF.class ).withProperty( "cpf" )
			);
		}
	}

	@Test
	@TestForIssue(jiraKey = "HV-933")
	public void any_same_digit_cpf_without_separator_is_invalid() {
		for ( String cpf : invalidCPFs ) {
			String cpfWithoutseparator = cpf.replaceAll( "[^0-9]", "" );
			Set<ConstraintViolation<Person>> violations = validator.validate( new Person( cpfWithoutseparator ) );
			assertThat( violations ).containsOnlyViolations(
					violationOf( CPF.class ).withProperty( "cpf" )
			);
		}
	}

	@Test
	@TestForIssue(jiraKey = "HV-808")
	public void valid_cpfs_with_separator_validate() {
		Set<ConstraintViolation<Person>> violations = validator.validate( new Person( "378.796.950-01" ) );
		assertNoViolations( violations );

		violations = validator.validate( new Person( "331.814.296-43" ) );
		assertNoViolations( violations );
	}

	@Test
	@TestForIssue(jiraKey = "HV-933")
	public void valid_cpf_without_separator_validates() {
		Set<ConstraintViolation<Person>> violations = validator.validate( new Person( "37879695001" ) );
		assertNoViolations( violations );

		violations = validator.validate( new Person( "33181429643" ) );
		assertNoViolations( violations );
	}

	@Test
	@TestForIssue(jiraKey = "HV-979")
	public void correct_cpf_with_dash_only_separator_validates() {
		Set<ConstraintViolation<Person>> violations = validator.validate( new Person( "134241313-00" ) );
		assertNoViolations( violations );
	}

	@Test
	@TestForIssue(jiraKey = "HV-808")
	public void invalid_cpf_with_separator_creates_constraint_violation() {
		Set<ConstraintViolation<Person>> violations = validator.validate( new Person( "378.796.950-02" ) );
		assertThat( violations ).containsOnlyViolations(
				violationOf( CPF.class ).withProperty( "cpf" )
		);

		violations = validator.validate( new Person( "331.814.296-52" ) );
		assertThat( violations ).containsOnlyViolations(
				violationOf( CPF.class ).withProperty( "cpf" )
		);

		violations = validator.validate( new Person( "331.814.296-51" ) );
		assertThat( violations ).containsOnlyViolations(
				violationOf( CPF.class ).withProperty( "cpf" )
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-933")
	public void invalid_cpf_without_separator_creates_constraint_violation() {
		Set<ConstraintViolation<Person>> violations = validator.validate( new Person( "37879695002" ) );
		assertThat( violations ).containsOnlyViolations(
				violationOf( CPF.class ).withProperty( "cpf" )
		);

		violations = validator.validate( new Person( "33181429652" ) );
		assertThat( violations ).containsOnlyViolations(
				violationOf( CPF.class ).withProperty( "cpf" )
		);

		violations = validator.validate( new Person( "33181429651" ) );
		assertThat( violations ).containsOnlyViolations(
				violationOf( CPF.class ).withProperty( "cpf" )
		);
	}

	public static class Person {
		@CPF
		private String cpf;

		public Person(String cpf) {
			this.cpf = cpf;
		}
	}
}
