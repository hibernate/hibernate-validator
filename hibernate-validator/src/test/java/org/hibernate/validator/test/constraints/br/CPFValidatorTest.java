package org.hibernate.validator.test.constraints.br;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNumberOfViolations;

import java.util.Set;

import javax.validation.ConstraintViolation;

import org.hibernate.validator.constraints.br.CPF;
import org.hibernate.validator.testutil.ValidatorUtil;
import org.testng.annotations.Test;

public class CPFValidatorTest {

	/**
	 * HV-491
	 */
	@Test
	public void testCorrectFormattedCPFWithReportAsSingleViolation() {
		Set<ConstraintViolation<Person>> violations = ValidatorUtil.getValidator().validate(
				new Person( "134.241.313-00" ) );
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
