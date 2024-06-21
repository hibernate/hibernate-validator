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

	private String[] invalidCNPJs = {"00.000.000/0000-00", "11.111.111/1111-11",
			"22.222.222/2222-22", "33.333.333/3333-33",
			"44.444.444/4444-44", "55.555.555/5555-55",
			"66.666.666/6666-66", "77.777.777/7777-77",
			"88.888.888/8888-88", "99.999.999/9999-99"
	};

	private String[] validCNPJs = {"41.348.630/0001-39", "47.673.240/0001-10",
			"65.627.745/0001-20", "81.110.141/0001-69", "68.321.178/0001-78",
			"47.235.630/0001-09", "52.583.338/0001-17", "48.560.263/0001-81",
			"16.468.665/0001-64", "11.720.867/0001-38", "00.000.000/0001-91"
	};

	private String[] validCNPJsWithLetters = {
			"70B0XZ010UTA84", "3Y59DJD8484J90", "84JNG734MJKD82",
			"UU3UCXJCUDEM68", "ABCDEFGHIJKL80", "11AA22BB33CC06"
	};

	private String[] invalidCNPJsWithLetters = {
			"70B0XZ010UTA83", "3Y59DJD8484J80", "84JNG734MJKD00",
			"UU3UCXJCUDEM11", "ABCDEFGHIJKLAA", "11AA22BB33CC07"
	};

	@Test
	@TestForIssue(jiraKey = "HV-1971")
	public void any_length_less_then_14_is_invalid() {
		String[] invalidLengthCNPJs = {"1", "123", "0000000000019"};
		for ( String cnpj : invalidLengthCNPJs ) {
			Set<ConstraintViolation<Company>> violations = validator.validate( new Company( cnpj ) );
			assertThat( violations ).containsOnlyViolations(
					violationOf( CNPJ.class ).withProperty( "cnpj" )
			);
		}
	}

	@Test
	@TestForIssue(jiraKey = "HV-1971")
	public void any_same_digit_cnpj_with_separator_is_invalid() {
		for ( String cnpj : invalidCNPJs ) {
			Set<ConstraintViolation<Company>> violations = validator.validate( new Company( cnpj ) );
			assertThat( violations ).containsOnlyViolations(
					violationOf( CNPJ.class ).withProperty( "cnpj" )
			);
		}
	}

	@Test
	@TestForIssue(jiraKey = "HV-1971")
	public void any_same_digit_cnpj_without_separator_is_invalid() {
		for ( String cnpj : invalidCNPJs ) {
			String cnpjWithoutseparator = cnpj.replaceAll( "\\D+", "" );
			Set<ConstraintViolation<Company>> violations = validator.validate( new Company( cnpjWithoutseparator ) );
			assertThat( violations ).containsOnlyViolations(
					violationOf( CNPJ.class ).withProperty( "cnpj" )
			);
		}
	}

	@Test
	@TestForIssue(jiraKey = "HV-1971")
	public void correct_list_of_cnpj_with_separator_validates() {
		for ( String cnpj : validCNPJs ) {
			Set<ConstraintViolation<Company>> violations = validator.validate( new Company( cnpj ) );
			assertNoViolations( violations );
		}
	}

	@Test
	@TestForIssue(jiraKey = "HV-1971")
	public void correct_list_of_cnpj_without_separator_validates() {
		for ( String cnpj : validCNPJs ) {
			String cnpjWithoutseparator = cnpj.replaceAll( "\\D+", "" );
			Set<ConstraintViolation<Company>> violations = validator.validate( new Company( cnpjWithoutseparator ) );
			assertNoViolations( violations );
		}
	}

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

	@Test
	@TestForIssue(jiraKey = "HV-1999")
	public void incorrect_cnpj_with_letters_creates_constraint_violation() {
		Set<ConstraintViolation<Company>> violations = validator.validate( new Company( "9A50A90A000A66" ) );
		assertThat( violations ).containsOnlyViolations(
				violationOf( CNPJ.class ).withProperty( "cnpj" )
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-1999")
	public void incorrect_cnpj_with_letters_separators_creates_constraint_violation() {
		Set<ConstraintViolation<Company>> violations = validator.validate( new Company( "9A.50A.90A/000A-66" ) );
		assertThat( violations ).containsOnlyViolations(
				violationOf( CNPJ.class ).withProperty( "cnpj" )
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-1999")
	public void correct_new_cnpj_with_letters_validates() {
		for ( String validCNPJ : validCNPJsWithLetters ) {
			Set<ConstraintViolation<CompanyCNPJWithLetters>> violations = validator.validate( new CompanyCNPJWithLetters( validCNPJ ) );
			assertNoViolations( violations );
		}
	}

	@Test
	@TestForIssue(jiraKey = "HV-1999")
	public void correct_new_cnpj_with_letters_separators_validates() {
		Set<ConstraintViolation<CompanyCNPJWithLetters>> violations = validator.validate( new CompanyCNPJWithLetters( "9A.50A.90A/000A-66" ) );
		assertNoViolations( violations );
	}

	@Test
	@TestForIssue(jiraKey = "HV-1999")
	public void incorrect_new_cnpj_with_letters_creates_constraint_violation() {
		Set<ConstraintViolation<CompanyCNPJWithLetters>> violations = validator.validate( new CompanyCNPJWithLetters( "9A.50A.90A/000A-67" ) );
		assertThat( violations ).containsOnlyViolations(
				violationOf( CNPJ.class ).withProperty( "cnpj" )
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-1999")
	public void incorrect_new_cnpj_with_letters_separators_creates_constraint_violation() {
		for ( String invalidCNPJ : invalidCNPJsWithLetters ) {
			Set<ConstraintViolation<CompanyCNPJWithLetters>> violations = validator.validate( new CompanyCNPJWithLetters( invalidCNPJ ) );
			assertThat( violations ).containsOnlyViolations(
					violationOf( CNPJ.class ).withProperty( "cnpj" )
			);
		}
	}

	public static class Company {
		@CNPJ
		private String cnpj;

		public Company(String cnpj) {
			this.cnpj = cnpj;
		}
	}

	public static class CompanyCNPJWithLetters {
		@CNPJ(alphanumeric = true)
		private String cnpj;

		public CompanyCNPJWithLetters(String cnpj) {
			this.cnpj = cnpj;
		}
	}
}
