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

import org.testng.annotations.Test;

import org.hibernate.validator.constraints.br.TituloEleitoral;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutil.ValidatorUtil;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNumberOfViolations;

public class TituloEleitoralValidatorTest {
	@Test
	@TestForIssue(jiraKey = "HV-491")
	public void testCorrectFormattedCPFWithReportAsSingleViolation() {
		Validator validator = ValidatorUtil.getValidator();
		assertNumberOfViolations( validator.validate( new Person( "040806680957" ) ), 0 );
		assertNumberOfViolations( validator.validate( new Person( "038763000914" ) ), 0 );
	}

	@Test
	@TestForIssue(jiraKey = "HV-491")
	public void testIncorrectFormattedCPFWithReportAsSingleViolation() {
		Set<ConstraintViolation<Person>> violations = ValidatorUtil.getValidator().validate( new Person( "48255-77" ) );
		assertNumberOfViolations( violations, 1 );
	}

	public static class Person {
		@TituloEleitoral
		private String tituloEleitor;

		public Person(String cpf) {
			this.tituloEleitor = cpf;
		}

		public String getTituloEleitor() {
			return tituloEleitor;
		}
	}
}
