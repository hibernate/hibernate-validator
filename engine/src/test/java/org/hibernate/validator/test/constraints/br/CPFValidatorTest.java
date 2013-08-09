/*
* JBoss, Home of Professional Open Source
* Copyright 2011, Red Hat, Inc. and/or its affiliates, and individual contributors
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
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
