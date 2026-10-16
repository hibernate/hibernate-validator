/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.constraints.annotations.hv.br;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import jakarta.validation.ConstraintViolation;

import org.hibernate.validator.constraints.br.CPF;
import org.hibernate.validator.test.constraints.annotations.AbstractConstrainedTest;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutils.Mod11TestUtil;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class CPFValidatorTest extends AbstractConstrainedTest {
	private String[] invalidCPFs = {
			"000.000.000-00",
			"111.111.111-11",
			"222.222.222-22",
			"333.333.333-33",
			"444.444.444-44",
			"555.555.555-55",
			"666.666.666-66",
			"777.777.777-77",
			"888.888.888-88",
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
	@TestForIssue(jiraKey = "HV-1923")
	public void incorrect_cpf_with_dash_only_separator_is_invalid() {
		Set<ConstraintViolation<Person>> violations = validator.validate( new Person( "134241313-00" ) );
		assertThat( violations ).containsOnlyViolations(
				violationOf( CPF.class ).withProperty( "cpf" )
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-1923")
	public void incorrect_cpf_with_partial_dots_is_invalid() {
		// Test various incorrect formats with partial dots
		Set<ConstraintViolation<Person>> violations = validator.validate( new Person( "134.241313-00" ) );
		assertThat( violations ).containsOnlyViolations(
				violationOf( CPF.class ).withProperty( "cpf" )
		);

		violations = validator.validate( new Person( "134241.313-00" ) );
		assertThat( violations ).containsOnlyViolations(
				violationOf( CPF.class ).withProperty( "cpf" )
		);
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

	@Test
	public void null_cpf_is_valid() {
		assertNoViolations( validator.validate( new Person( null ) ) );
	}

	@Test
	public void cpf_is_a_standalone_constraint() {
		assertTrue( validator.getConstraintsForClass( Person.class )
				.getConstraintsForProperty( "cpf" ).getConstraintDescriptors().iterator().next()
				.getComposingConstraints().isEmpty() );
	}

	@Test(dataProvider = "malformedCPFs")
	public void malformed_cpf_is_invalid(CharSequence cpf) {
		assertThat( validator.validate( new Person( cpf ) ) ).containsOnlyViolations(
				violationOf( CPF.class ).withProperty( "cpf" )
		);
	}

	@DataProvider
	public Object[][] malformedCPFs() {
		return new Object[][] {
				{ "" },
				{ "1342413130" },
				{ "134241313000" },
				{ "134.241.313.00" },
				{ "134-241.313-00" },
				{ "134.241-313-00" },
				{ "134 241 313 00" },
				{ "13424131300 " },
				{ " 13424131300" },
				{ "13424131300\n" },
				{ "134.241.313-00\n" },
				{ "1342413130a" },
				{ "１３４２４１３１３００" },
				{ "١٣٤٢٤١٣١٣٠٠" },
				{ "1342413130０" },
				{ new StringBuilder( "134.241.313-0a" ) },
				{ "0".repeat( 100_000 ) }
		};
	}

	@Test
	public void zero_and_one_checksum_remainders_map_to_zero() {
		// The first checksum remainder is zero for 000000014 and one for 123456789.
		assertNoViolations( validator.validate( new Person( "00000001406" ) ) );
		assertNoViolations( validator.validate( new Person( "000.000.014-06" ) ) );
		assertNoViolations( validator.validate( new Person( "12345678909" ) ) );
		assertNoViolations( validator.validate( new Person( "123.456.789-09" ) ) );
		// This identifier has a leading zero and a second checksum remainder of one.
		assertNoViolations( validator.validate( new Person( "01234567890" ) ) );
		assertNoViolations( validator.validate( new Person( "012.345.678-90" ) ) );
	}

	@Test
	public void generated_cpfs_match_mod11_checks() {
		Random random = new Random( 491 );
		for ( int sample = 0; sample < 100; sample++ ) {
			List<Integer> digits = new ArrayList<>();
			StringBuilder cpf = new StringBuilder();
			for ( int i = 0; i < 9; i++ ) {
				// Include leading-zero identifiers, whose numeric value must not affect validation.
				int digit = i == 0 ? 0 : random.nextInt( 10 );
				digits.add( digit );
				cpf.append( digit );
			}
			for ( int i = 0; i < 2; i++ ) {
				int checkDigit = Mod11TestUtil.calculateMod11CheckLegacy( digits, Integer.MAX_VALUE );
				checkDigit = checkDigit >= 10 ? 0 : checkDigit;
				digits.add( checkDigit );
				cpf.append( checkDigit );
			}

			assertNoViolations( validator.validate( new Person( cpf ) ) );
			assertNoViolations( validator.validate( new Person( format( cpf ) ) ) );

			for ( int i = 9; i < 11; i++ ) {
				char original = cpf.charAt( i );
				cpf.setCharAt( i, (char) ( '0' + ( original - '0' + 1 ) % 10 ) );
				assertThat( validator.validate( new Person( cpf ) ) ).containsOnlyViolations(
						violationOf( CPF.class ).withProperty( "cpf" )
				);
				assertThat( validator.validate( new Person( format( cpf ) ) ) ).containsOnlyViolations(
						violationOf( CPF.class ).withProperty( "cpf" )
				);
				cpf.setCharAt( i, original );
			}
		}
	}

	private static String format(CharSequence cpf) {
		return cpf.subSequence( 0, 3 ) + "." + cpf.subSequence( 3, 6 ) + "."
				+ cpf.subSequence( 6, 9 ) + "-" + cpf.subSequence( 9, 11 );
	}

	public static class Person {
		@CPF
		private CharSequence cpf;

		public Person(CharSequence cpf) {
			this.cpf = cpf;
		}
	}
}
