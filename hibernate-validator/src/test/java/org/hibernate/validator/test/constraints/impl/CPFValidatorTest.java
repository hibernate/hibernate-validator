package org.hibernate.validator.test.constraints.impl;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNumberOfViolations;

import java.util.Set;

import javax.validation.ConstraintViolation;

import org.hibernate.validator.constraints.CPF;
import org.hibernate.validator.testutil.ValidatorUtil;
import org.testng.annotations.Test;

public class CPFValidatorTest {

	/**
	 * HV-491
	 */
	@Test
	public void testCorrectFormattedCPFWithReportAsSingleViolation() {
		Set<ConstraintViolation<Person>> violations = ValidatorUtil.getValidator().validate(
				new Person( "008.168.699-44" ) );
		assertNumberOfViolations( violations, 0 );
	}

	/**
	 * HV-491
	 */
	@Test
	public void testIncorrectFormattedCPFWithReportAsSingleViolation() {
		Set<ConstraintViolation<Person>> violations = ValidatorUtil.getValidator().validate(
				new Person( "008.168.699-77" ) );
		assertNumberOfViolations( violations, 1 );
	}

	public static class Person {
		@CPF
		private String cpf;

		public Person(String cpf) {
			this.cpf = cpf;
		}

		public String getCpf() {
			return cpf;
		}
	}
}
