/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.constraints.br;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.hibernate.validator.constraints.br.TituloEleitoral;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutils.ValidatorUtil;

import org.testng.annotations.Test;

public class TituloEleitoralValidatorTest {
	@Test
	@TestForIssue(jiraKey = "HV-491")
	public void testCorrectFormattedCPFWithReportAsSingleViolation() {
		Validator validator = ValidatorUtil.getValidator();
		assertNoViolations( validator.validate( new Person( "040806680957" ) ) );
		assertNoViolations( validator.validate( new Person( "038763000914" ) ) );
	}

	@Test
	@TestForIssue(jiraKey = "HV-491")
	public void testIncorrectFormattedCPFWithReportAsSingleViolation() {
		Set<ConstraintViolation<Person>> violations = ValidatorUtil.getValidator().validate( new Person( "48255-77" ) );
		assertThat( violations ).containsOnlyViolations(
				violationOf( TituloEleitoral.class ).withProperty( "tituloEleitor" )
		);
	}

	public static class Person {
		@TituloEleitoral
		private String tituloEleitor;

		public Person(String tituloEleitor) {
			this.tituloEleitor = tituloEleitor;
		}

		public String getTituloEleitor() {
			return tituloEleitor;
		}
	}
}
