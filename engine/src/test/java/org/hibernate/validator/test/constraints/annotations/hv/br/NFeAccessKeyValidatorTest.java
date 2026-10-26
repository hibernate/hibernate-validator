/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.constraints.annotations.hv.br;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import java.util.Set;

import jakarta.validation.ConstraintViolation;

import org.hibernate.validator.constraints.br.NFeAccessKey;
import org.hibernate.validator.test.constraints.annotations.AbstractConstrainedTest;
import org.hibernate.validator.testutil.TestForIssue;

import org.testng.annotations.Test;

/**
 * @author Matheus Pereira
 */
public class NFeAccessKeyValidatorTest extends AbstractConstrainedTest {

	private static final String[] VALID_NUMERIC_NFE_ACCESS_KEYS = {
			"35261041348630000139550010000000011123456782",
			"43260847673240000110551231234567891876543210",
			"31250165627745000120559099999999991000000018",
			"35261041348630000139550010000000011000000080"
	};

	private static final String[] VALID_ALPHANUMERIC_NFE_ACCESS_KEYS = {
			"35261000000000E08G12550010000000011123456783",
			"41260912ABC34501DE35551230001234561876543214"
	};

	private static final String[] INVALID_CHECK_DIGIT_NFE_ACCESS_KEYS = {
			"35261041348630000139550010000000011123456783",
			"43260847673240000110551231234567891876543211",
			"35261000000000E08G12550010000000011123456784"
	};

	private static final String[] INVALID_LENGTH_NFE_ACCESS_KEYS = {
			"",
			"3526104134863000013955001000000001112345678",
			"352610413486300001395500100000000111234567820"
	};

	private static final String[] INVALID_CHARACTER_NFE_ACCESS_KEYS = {
			"A5261041348630000139550010000000011123456782",
			"3526104134a630000139550010000000011123456782",
			"352610413486300001A9550010000000011123456782",
			"352610413486300001395500100000000111A3456782",
			"\u06635261041348630000139550010000000011123456782"
	};

	private static final String[] INVALID_MODEL_NFE_ACCESS_KEYS = {
			"35261041348630000139650010000000011123456785",
			"35261041348630000139570010000000011123456780"
	};

	private static final String ALL_ZERO_NFE_ACCESS_KEY = "00000000000000000000000000000000000000000000";

	@Test
	@TestForIssue(jiraKey = "HV-2264")
	public void testNumericNFeAccessKeyIsValid() {
		for ( String accessKey : VALID_NUMERIC_NFE_ACCESS_KEYS ) {
			assertNoViolations( validator.validate( new EInvoice( accessKey ) ) );
		}
	}

	@Test
	@TestForIssue(jiraKey = "HV-2264")
	public void testAlphanumericNFeAccessKeyIsValid() {
		for ( String accessKey : VALID_ALPHANUMERIC_NFE_ACCESS_KEYS ) {
			assertNoViolations( validator.validate( new EInvoice( accessKey ) ) );
		}
	}

	@Test
	@TestForIssue(jiraKey = "HV-2264")
	public void testNullIsValid() {
		assertNoViolations( validator.validate( new EInvoice( null ) ) );
	}

	@Test
	@TestForIssue(jiraKey = "HV-2264")
	public void testNFeAccessKeyWithInvalidCheckDigitIsInvalid() {
		for ( String accessKey : INVALID_CHECK_DIGIT_NFE_ACCESS_KEYS ) {
			Set<ConstraintViolation<EInvoice>> violations = validator.validate( new EInvoice( accessKey ) );
			assertThat( violations ).containsOnlyViolations(
					violationOf( NFeAccessKey.class ).withProperty( "accessKey" ) );
		}
	}

	@Test
	@TestForIssue(jiraKey = "HV-2264")
	public void testUnsupportedLengthIsInvalid() {
		for ( String accessKey : INVALID_LENGTH_NFE_ACCESS_KEYS ) {
			Set<ConstraintViolation<EInvoice>> violations = validator.validate( new EInvoice( accessKey ) );
			assertThat( violations ).containsOnlyViolations(
					violationOf( NFeAccessKey.class ).withProperty( "accessKey" ) );
		}
	}

	@Test
	@TestForIssue(jiraKey = "HV-2264")
	public void testInvalidCharacterIsInvalid() {
		for ( String accessKey : INVALID_CHARACTER_NFE_ACCESS_KEYS ) {
			Set<ConstraintViolation<EInvoice>> violations = validator.validate( new EInvoice( accessKey ) );
			assertThat( violations ).containsOnlyViolations(
					violationOf( NFeAccessKey.class ).withProperty( "accessKey" ) );
		}
	}

	@Test
	@TestForIssue(jiraKey = "HV-2264")
	public void testDifferentDFeModelIsInvalid() {
		for ( String accessKey : INVALID_MODEL_NFE_ACCESS_KEYS ) {
			Set<ConstraintViolation<EInvoice>> violations = validator.validate( new EInvoice( accessKey ) );
			assertThat( violations ).containsOnlyViolations(
					violationOf( NFeAccessKey.class ).withProperty( "accessKey" ) );
		}
	}

	@Test
	@TestForIssue(jiraKey = "HV-2264")
	public void testAllZeroNFeAccessKeyIsInvalid() {
		Set<ConstraintViolation<EInvoice>> violations = validator.validate( new EInvoice( ALL_ZERO_NFE_ACCESS_KEY ) );
		assertThat( violations ).containsOnlyViolations(
				violationOf( NFeAccessKey.class ).withProperty( "accessKey" ) );
	}

	public static class EInvoice {

		@NFeAccessKey
		private String accessKey;

		public EInvoice(String accessKey) {
			this.accessKey = accessKey;
		}
	}
}
