package org.hibernate.validator.test.constraints.br;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNumberOfViolations;

import java.util.Set;

import javax.validation.ConstraintViolation;

import org.hibernate.validator.constraints.br.CNPJ;
import org.hibernate.validator.testutil.ValidatorUtil;
import org.testng.annotations.Test;

public class CNPJValidatorTest {

	/**
	 * HV-491
	 */
	@Test
	public void testCorrectFormattedCNPJWithReportAsSingleViolation() {
		Set<ConstraintViolation<Company>> violations = ValidatorUtil.getValidator().validate(
				new Company( "91.509.901/0001-69" ) );
		assertNumberOfViolations( violations, 0 );
	}

	/**
	 * HV-491
	 */
	@Test
	public void testIncorrectFormattedCNPJWithReportAsSingleViolation() {
		Set<ConstraintViolation<Company>> violations = ValidatorUtil.getValidator().validate(
				new Company( "ABCDEF" ) );
		assertNumberOfViolations( violations, 1 );
	}

	public static class Company {
		@CNPJ
		private String cnpj;

		public Company(String cnpj) {
			this.cnpj = cnpj;
		}

		public String getCnpj() {
			return cnpj;
		}
	}
}
