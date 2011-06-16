package org.hibernate.validator.test.constraints.impl;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNumberOfViolations;

import java.util.Set;

import javax.validation.ConstraintViolation;

import org.hibernate.validator.constraints.TituloEleitor;
import org.hibernate.validator.testutil.ValidatorUtil;
import org.testng.annotations.Test;

public class TituloEleitorValidatorTest {

	/**
	 * HV-491
	 */
	@Test
	public void testCorrectFormattedCPFWithReportAsSingleViolation() {
		Set<ConstraintViolation<Person>> violations = ValidatorUtil.getValidator().validate(
				new Person( "038763000914" ) );
		assertNumberOfViolations( violations, 0 );
	}

	/**
	 * HV-491
	 */
	@Test
	public void testIncorrectFormattedCPFWithReportAsSingleViolation() {
		Set<ConstraintViolation<Person>> violations = ValidatorUtil.getValidator().validate(
				new Person( "48255-77" ) );
		assertNumberOfViolations( violations, 1 );
	}

	public static class Person {
		@TituloEleitor
		private String tituloEleitor;

		public Person(String cpf) {
			this.tituloEleitor = cpf;
		}

		public String getTituloEleitor() {
			return tituloEleitor;
		}
	}
}
