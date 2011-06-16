package org.hibernate.validator.test.constraints.br;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNumberOfViolations;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.hibernate.validator.constraints.br.TituloEleitoral;
import org.hibernate.validator.testutil.ValidatorUtil;
import org.testng.annotations.Test;

public class TituloEleitoralValidatorTest {

	/**
	 * HV-491
	 */
	@Test
	public void testCorrectFormattedCPFWithReportAsSingleViolation() {
		Validator validator = ValidatorUtil.getValidator();
		assertNumberOfViolations( validator.validate( new Person( "040806680957" ) ), 0 );
		assertNumberOfViolations( validator.validate( new Person( "038763000914" ) ), 0 );
	}

	/**
	 * HV-491
	 */
	@Test
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
