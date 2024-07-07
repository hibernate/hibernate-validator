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
	public void testCorrectFormattedTituloEleitoralWithReportAsSingleViolation() {
		assertNoViolations( validator.validate( new Person( "040806680957" ) ) );
		assertNoViolations( validator.validate( new Person( "038763000914" ) ) );
		assertNoViolations( validator.validate( new Person( "975993331007" ) ) );
		assertNoViolations( validator.validate( new Person( "524384240701" ) ) );
		assertNoViolations( validator.validate( new Person( "311533282500" ) ) );
		assertNoViolations( validator.validate( new Person( "083578481406" ) ) );
		assertNoViolations( validator.validate( new Person( "233838490205" ) ) );
		assertNoViolations( validator.validate( new Person( "585052440116" ) ) );
		assertNoViolations( validator.validate( new Person( "650648840264" ) ) );
		assertNoViolations( validator.validate( new Person( "357866370213" ) ) );
		assertNoViolations( validator.validate( new Person( "074447240264" ) ) );
		assertNoViolations( validator.validate( new Person( "164284280213" ) ) );
		assertNoViolations( validator.validate( new Person( "465667341619" ) ) );
		assertNoViolations( validator.validate( new Person( "362133720779" ) ) );
	}

	@Test
	@TestForIssue(jiraKey = "HV-491")
	public void testIncorrectFormattedTituloEleitoralWithReportAsSingleViolation() {
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
