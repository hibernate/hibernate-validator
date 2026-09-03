/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.constraintvalidators.hv;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.hibernate.validator.testutils.ValidatorUtil.getConfiguration;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.defs.BICDef;
import org.hibernate.validator.constraints.BIC;
import org.hibernate.validator.internal.constraintvalidators.hv.BICValidator;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * A set of tests for {@link BIC} constraint validator ({@link BICValidator}), which
 * make sure that validation is performed correctly.
 *
 * @author Andrea Boriero
 */
public class BICValidatorTest {

	private BICValidator validator;

	@BeforeMethod
	public void setUp() throws Exception {
		validator = new BICValidator();
		validator.initialize( createBICAnnotation( false, new String[0], new String[0] ) );
	}

	@Test
	public void validBIC() throws Exception {
		assertValidBIC( null );
		// BIC8 examples
		assertValidBIC( "DEUTDEFF" );
		assertValidBIC( "NEDSZAJJ" );
		assertValidBIC( "DABADKKK" );
		assertValidBIC( "UNCRIT2B" );
		assertValidBIC( "DSBACNBX" );
		// BIC11 examples
		assertValidBIC( "DEUTDEFF500" );
		assertValidBIC( "NEDSZAJJXXX" );
		assertValidBIC( "CHASUS33XXX" );
		assertValidBIC( "RZTIAT22263" );
		// Kosovo (XK) is supported as a SWIFT-specific country code
		assertValidBIC( "RBKOXKPR" );
	}

	@Test
	public void invalidLength() throws Exception {
		assertInvalidBIC( "" );
		assertInvalidBIC( "DEUTDE" );
		assertInvalidBIC( "DEUTDEFF5" );
		assertInvalidBIC( "DEUTDEFF50" );
		assertInvalidBIC( "DEUTDEFF5000" );
	}

	@Test
	public void invalidBankCode() throws Exception {
		// Bank code (positions 1-4) must be letters only
		assertInvalidBIC( "D3UTDEFF" );
		assertInvalidBIC( "DEU1DEFF" );
		assertInvalidBIC( "deuTDEFF" );
	}

	@Test
	public void invalidCountryCode() throws Exception {
		// Country code must be letters only
		assertInvalidBIC( "DEUT1EFF" );
		assertInvalidBIC( "DEUTD2FF" );
		// Unknown country codes
		assertInvalidBIC( "DEUTZZFF" );
		assertInvalidBIC( "DEUTQQFF" );
	}

	@Test
	public void invalidLocationCode() throws Exception {
		// Location code (positions 7-8) can be alphanumeric, but must be valid
		assertInvalidBIC( "DEUTDE-F" );
		assertInvalidBIC( "DEUTDE F" );
	}

	@Test
	public void invalidBranchCode() throws Exception {
		// Branch code (positions 9-11) can be alphanumeric, but must be valid
		assertInvalidBIC( "DEUTDEFF-00" );
		assertInvalidBIC( "DEUTDEFF 00" );
	}

	@Test
	public void lowercaseIsInvalidByDefault() throws Exception {
		// By default lowercase letters are rejected
		assertInvalidBIC( "deutdeff" );
		assertInvalidBIC( "DEUTdeff" );
		assertInvalidBIC( "deutdeff500" );
	}

	@Test
	public void lowercaseIsValidWhenAllowed() throws Exception {
		validator.initialize( createBICAnnotation( true, new String[0], new String[0] ) );

		assertValidBIC( null );
		assertValidBIC( "deutdeff" );
		assertValidBIC( "DEUTdeff" );
		assertValidBIC( "deutdeff500" );
		// Uppercase keeps working when lowercase is allowed
		assertValidBIC( "DEUTDEFF" );
		assertValidBIC( "DEUTDEFF500" );
	}

	@Test
	public void allowLowercaseStillRejectsInvalidBIC() throws Exception {
		validator.initialize( createBICAnnotation( true, new String[0], new String[0] ) );

		// Invalid structure is still rejected regardless of case
		assertInvalidBIC( "deutde" );
		assertInvalidBIC( "deutzzff" );
	}

	@Test
	public void testCountryCodeFiltering() throws Exception {
		validator.initialize( createBICAnnotation( false, new String[] { "DE", "FR" }, new String[0] ) );

		// Allowed countries
		assertValidBIC( "DEUTDEFF" );
		assertValidBIC( "SOGEFRPP" );

		// Rejected countries
		assertInvalidBIC( "CHASUS33" );
		assertInvalidBIC( "NEDSZAJJ" );
	}

	@Test
	public void testCountryCodeFilteringCaseInsensitive() throws Exception {
		validator.initialize( createBICAnnotation( true, new String[] { "de", "FR" }, new String[0] ) );

		// Country code matching is case-insensitive
		assertValidBIC( "deutdeff" );
		assertValidBIC( "DEUTDEFF" );
		assertValidBIC( "sogefrpp" );
	}

	@Test
	public void testBankCodeFiltering() throws Exception {
		validator.initialize( createBICAnnotation( false, new String[0], new String[] { "DEUT", "CHAS" } ) );

		// Allowed bank codes
		assertValidBIC( "DEUTDEFF" );
		assertValidBIC( "DEUTDEFF500" );
		assertValidBIC( "CHASDEFX" );

		// Rejected bank codes
		assertInvalidBIC( "SOGEFRPP" );
		assertInvalidBIC( "NEDSZAJJ" );
	}

	@Test
	public void testBankCodeFilteringCaseInsensitive() throws Exception {
		validator.initialize( createBICAnnotation( true, new String[0], new String[] { "deut", "CHAS" } ) );

		// Bank code matching is case-insensitive
		assertValidBIC( "deutdeff" );
		assertValidBIC( "DEUTDEFF" );
		assertValidBIC( "chasdefx" );
	}

	@Test
	public void testCombinedCountryAndBankCodeFiltering() throws Exception {
		validator.initialize( createBICAnnotation( false, new String[] { "DE" }, new String[] { "DEUT" } ) );

		// Valid: German bank with allowed bank code
		assertValidBIC( "DEUTDEFF" );

		// Invalid: German bank but wrong bank code
		assertInvalidBIC( "SOLADEST" );

		// Invalid: Correct bank code but wrong country
		assertInvalidBIC( "DEUTGB2L" );
	}

	@Test
	public void testBIC8AndBIC11BothValid() throws Exception {
		// Both BIC-8 and BIC-11 should be accepted
		assertValidBIC( "DEUTDEFF" );
		assertValidBIC( "DEUTDEFFXXX" );
		assertValidBIC( "DEUTDEFF500" );
	}

	@Test
	public void testTestBICsAreAllowed() throws Exception {
		// Test BICs (position 8 = '0') are valid
		assertValidBIC( "DEUTDE0F" );
		assertValidBIC( "DEUTDE0FXXX" );
	}

	@Test
	public void testProgrammaticDefinition() throws Exception {
		HibernateValidatorConfiguration config = getConfiguration( HibernateValidator.class );
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( Bank.class )
				.field( "bic" )
				.constraint( new BICDef()
						.countryCodes( "DE", "FR" )
						.bankCodes( "DEUT" ) );
		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		Set<ConstraintViolation<Bank>> constraintViolations = validator.validate( new Bank( "DEUTDEFF" ) );
		assertNoViolations( constraintViolations );

		constraintViolations = validator.validate( new Bank( "SOGEFRPP" ) );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( BIC.class )
		);
	}

	private void assertValidBIC(String bic) {
		assertTrue( validator.isValid( bic, null ), bic + " should be a valid BIC" );
	}

	private void assertInvalidBIC(String bic) {
		assertFalse( validator.isValid( bic, null ), bic + " should be an invalid BIC" );
	}

	private BIC createBICAnnotation(boolean allowLowercase, String[] countryCodes, String[] bankCodes) {
		ConstraintAnnotationDescriptor.Builder<BIC> descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( BIC.class );
		descriptorBuilder.setAttribute( "allowLowercase", allowLowercase );
		descriptorBuilder.setAttribute( "countryCodes", countryCodes );
		descriptorBuilder.setAttribute( "bankCodes", bankCodes );

		return descriptorBuilder.build().getAnnotation();
	}

	private static class Bank {

		private final String bic;

		private Bank(String bic) {
			this.bic = bic;
		}
	}
}
