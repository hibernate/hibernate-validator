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

import org.hibernate.validator.constraints.br.CNPJ;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutil.ValidatorUtil;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNumberOfViolations;

public class CNPJValidatorTest {
	@Test
	@TestForIssue(jiraKey = "HV-491")
	public void testCorrectFormattedCNPJWithReportAsSingleViolation() {
		Set<ConstraintViolation<Company>> violations = ValidatorUtil.getValidator().validate(
				new Company( "91.509.901/0001-69" )
		);
		assertNumberOfViolations( violations, 0 );
	}

	@Test
	@TestForIssue(jiraKey = "HV-491")
	public void testIncorrectFormattedCNPJWithReportAsSingleViolation() {
		Set<ConstraintViolation<Company>> violations = ValidatorUtil.getValidator().validate(
				new Company( "ABCDEF" )
		);
		assertNumberOfViolations( violations, 1 );
	}

	public static class Company {
		@CNPJ
		private String cnpj;

		public Company(String cnpj) {
			this.cnpj = cnpj;
		}
	}
}
