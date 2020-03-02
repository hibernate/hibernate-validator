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

import org.hibernate.validator.constraints.br.TituloEleitoral;
import org.hibernate.validator.test.constraints.annotations.AbstractConstrainedTest;
import org.hibernate.validator.testutil.TestForIssue;

import org.testng.annotations.Test;

public class TituloEleitoralValidatorTest extends AbstractConstrainedTest {
	@Test
	@TestForIssue(jiraKey = "HV-491")
	public void testCorrectFormattedCPFWithReportAsSingleViolation() {
		assertNoViolations( validator.validate( new Person( "040806680957" ) ) );
		assertNoViolations( validator.validate( new Person( "038763000914" ) ) );
	}

	@Test
	@TestForIssue(jiraKey = "HV-491")
	public void testIncorrectFormattedCPFWithReportAsSingleViolation() {
		Set<ConstraintViolation<Person>> violations = validator.validate( new Person( "48255-77" ) );
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
