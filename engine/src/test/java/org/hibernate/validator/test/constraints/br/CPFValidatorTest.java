/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.constraints.br;

import java.util.Set;
import javax.validation.ConstraintViolation;

import org.testng.annotations.Test;

import org.hibernate.validator.constraints.br.CPF;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutil.ValidatorUtil;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNumberOfViolations;

public class CPFValidatorTest {
	@Test
	@TestForIssue(jiraKey = "HV-491")
	public void testCorrectFormattedCPFWithReportAsSingleViolation() {
		Set<ConstraintViolation<Person>> violations = ValidatorUtil.getValidator().validate(
				new Person( "134.241.313-00" )
		);
		assertNumberOfViolations( violations, 0 );

		violations =  ValidatorUtil.getValidator().validate(
				new Person( "13424131300" )
		);
		assertNumberOfViolations(violations, 0 );
	}

	@Test
	@TestForIssue(jiraKey = "HV-491")
	public void testCPFBoundaryConditions() {
		Set<ConstraintViolation<Person>> violations = ValidatorUtil.getValidator().validate(
				new Person( "000.000.000-00" )
		);
		assertNumberOfViolations( violations, 1 );

		violations = ValidatorUtil.getValidator().validate(
				new Person( "999.999.999-99" )
		);
		assertNumberOfViolations( violations, 1 );

		violations = ValidatorUtil.getValidator().validate(
				new Person( "00000000000" )
		);
		assertNumberOfViolations( violations, 1 );

		violations = ValidatorUtil.getValidator().validate(
				new Person( "99999999999" )
		);
		assertNumberOfViolations( violations, 1 );

	}

	@Test
	@TestForIssue(jiraKey = "HV-491")
	public void testIncorrectFormattedCPFWithReportAsSingleViolation() {
		Set<ConstraintViolation<Person>> violations = ValidatorUtil.getValidator().validate(
				new Person( "48255-77" )
		);
		assertNumberOfViolations( violations, 1 );
	}

	@Test
	@TestForIssue(jiraKey = "HV-808")
	public void testCPFBoundaryConditionsForAllSameDigitValidMod11() {
		String[] invalidCPFs = {
				"000.000.000-00", "111.111.111-11", "222.222.222-22",
				"333.333.333-33", "444.444.444-44", "555.555.555-55",
				"666.666.666-66", "777.777.777-77", "888.888.888-88",
				"999.999.999-99"
		};

		Set<ConstraintViolation<Person>> violations = null;

		for ( String cpf : invalidCPFs ) {
			violations = ValidatorUtil.getValidator().validate(
					new Person( cpf )
			);

			assertNumberOfViolations( violations, 1 );

			violations = ValidatorUtil.getValidator().validate(
					new Person( cpf.replaceAll( "[^0-9]", "" ) )
			);
			assertNumberOfViolations( violations, 1 );
		}
	}

	@Test
	@TestForIssue(jiraKey = "HV-808")
	public void testCorrectFormattedCPFWithReportAsSingleViolationForCheckDigitSelfValidation() {
		Set<ConstraintViolation<Person>> violations = ValidatorUtil.getValidator().validate(
				new Person( "378.796.950-01" )
		);
		assertNumberOfViolations( violations, 0 );

		violations = ValidatorUtil.getValidator().validate(
				new Person( "378.796.950-02" )
		);
		assertNumberOfViolations( violations, 1 );

		violations = ValidatorUtil.getValidator().validate(
				new Person( "331.814.296-43" )
		);
		assertNumberOfViolations( violations, 0 );

		violations = ValidatorUtil.getValidator().validate(
				new Person( "331.814.296-52" )
		);
		assertNumberOfViolations( violations, 1 );

		violations = ValidatorUtil.getValidator().validate(
				new Person( "331.814.296-51" )
		);
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
