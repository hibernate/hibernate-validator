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
import org.hibernate.validator.cfg.defs.IBANDef;
import org.hibernate.validator.constraints.IBAN;
import org.hibernate.validator.internal.constraintvalidators.hv.IBANValidator;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * A set of tests for {@link IBAN} constraint validator ({@link IBANValidator}), which
 * make sure that validation is performed correctly.
 *
 * @author Andrea Boriero
 */
public class IBANValidatorTest {

	private IBANValidator validator;

	@BeforeMethod
	public void setUp() throws Exception {
		validator = new IBANValidator();
	}

	@Test
	public void validIBAN() throws Exception {
		assertValidIBAN( null );
		assertValidIBAN( "GB82WEST12345698765432" );
		assertValidIBAN( "DE89370400440532013000" );
		assertValidIBAN( "FR1420041010050500013M02606" );
		assertValidIBAN( "ES9121000418450200051332" );
		assertValidIBAN( "NL91ABNA0417164300" );
		assertValidIBAN( "CH9300762011623852957" );
		assertValidIBAN( "BE68539007547034" );
		assertValidIBAN( "IT60X0542811101000000123456" );
		assertValidIBAN( "NO9386011117947" );
		assertValidIBAN( "SA0380000000608010167519" );
	}

	@Test
	public void validIBANWithSpaces() throws Exception {
		// Spaces used to group characters when printing an IBAN are ignored.
		assertValidIBAN( "GB82 WEST 1234 5698 7654 32" );
		assertValidIBAN( "DE89 3704 0044 0532 0130 00" );
	}

	@Test
	public void invalidCheckDigits() throws Exception {
		assertInvalidIBAN( "GB94WEST12345698765432" );
		assertInvalidIBAN( "DE99370400440532013000" );
		assertInvalidIBAN( "BE68539007547035" );
	}

	@Test
	public void invalidLengthForCountry() throws Exception {
		// GB IBANs are 22 characters long.
		assertInvalidIBAN( "GB82WEST1234569876543" );
		assertInvalidIBAN( "GB82WEST123456987654321" );
	}

	@Test
	public void invalidStructure() throws Exception {
		assertInvalidIBAN( "" );
		// Missing check digits.
		assertInvalidIBAN( "GBWEST12345698765432" );
		// Non-alphanumeric characters.
		assertInvalidIBAN( "GB82WEST1234-698765432" );
	}

	@Test
	public void unknownCountry() throws Exception {
		assertInvalidIBAN( "ZZ82WEST12345698765432" );
	}

	@Test
	public void lowercaseIsInvalidByDefault() throws Exception {
		// By default lowercase letters are rejected.
		assertInvalidIBAN( "gb82west12345698765432" );
		assertInvalidIBAN( "GB82west12345698765432" );
	}

	@Test
	public void lowercaseIsValidWhenAllowed() throws Exception {
		validator.initialize( createIBANAnnotation( true ) );

		assertValidIBAN( null );
		assertValidIBAN( "gb82west12345698765432" );
		assertValidIBAN( "GB82west12345698765432" );
		assertValidIBAN( "de89370400440532013000" );
		// Uppercase and spaces keep working when lowercase is allowed.
		assertValidIBAN( "GB82WEST12345698765432" );
		assertValidIBAN( "gb82 west 1234 5698 7654 32" );
	}

	@Test
	public void allowLowercaseStillRejectsInvalidIBAN() throws Exception {
		validator.initialize( createIBANAnnotation( true ) );

		// Wrong check digits are still rejected regardless of case.
		assertInvalidIBAN( "gb94west12345698765432" );
		// Unknown country is still rejected regardless of case.
		assertInvalidIBAN( "zz82west12345698765432" );
	}

	@Test
	public void testProgrammaticDefinition() throws Exception {
		HibernateValidatorConfiguration config = getConfiguration( HibernateValidator.class );
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( Account.class )
				.field( "iban" )
				.constraint( new IBANDef() );
		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		Set<ConstraintViolation<Account>> constraintViolations = validator.validate( new Account( "GB82WEST12345698765432" ) );
		assertNoViolations( constraintViolations );

		constraintViolations = validator.validate( new Account( "GB94WEST12345698765432" ) );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( IBAN.class )
		);
	}

	private void assertValidIBAN(String iban) {
		assertTrue( validator.isValid( iban, null ), iban + " should be a valid IBAN" );
	}

	private void assertInvalidIBAN(String iban) {
		assertFalse( validator.isValid( iban, null ), iban + " should be an invalid IBAN" );
	}

	private IBAN createIBANAnnotation(boolean allowLowercase) {
		ConstraintAnnotationDescriptor.Builder<IBAN> descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( IBAN.class );
		descriptorBuilder.setAttribute( "allowLowercase", allowLowercase );

		return descriptorBuilder.build().getAnnotation();
	}

	private static class Account {

		private final String iban;

		private Account(String iban) {
			this.iban = iban;
		}
	}
}
