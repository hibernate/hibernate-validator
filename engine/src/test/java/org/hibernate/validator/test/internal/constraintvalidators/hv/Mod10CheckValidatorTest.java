/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.constraintvalidators.hv;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutils.ValidatorUtil.getConfiguration;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.defs.Mod10CheckDef;
import org.hibernate.validator.constraints.Mod10Check;
import org.hibernate.validator.internal.constraintvalidators.hv.Mod10CheckValidator;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;
import org.hibernate.validator.testutil.MyCustomStringImpl;
import org.hibernate.validator.testutil.TestForIssue;

import org.testng.annotations.Test;

/**
 * Tests for the {@code Mod10CheckValidator}.
 *
 * @author Hardy Ferentschik
 * @author Victor Rezende dos Santos
 */
public class Mod10CheckValidatorTest {

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testInvalidStartIndex() {
		Mod10CheckValidator validator = new Mod10CheckValidator();
		Mod10Check modCheck = createMod10CheckAnnotation( -1, Integer.MAX_VALUE, -1, false );
		validator.initialize( modCheck );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testInvalidEndIndex() {
		Mod10CheckValidator validator = new Mod10CheckValidator();
		Mod10Check modCheck = createMod10CheckAnnotation( 0, -1, -1, false );
		validator.initialize( modCheck );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testEndIndexLessThanStartIndex() {
		Mod10CheckValidator validator = new Mod10CheckValidator();
		Mod10Check modCheck = createMod10CheckAnnotation( 5, 0, -1, false );
		validator.initialize( modCheck );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testInvalidCheckDigitIndex() {
		Mod10CheckValidator validator = new Mod10CheckValidator();
		Mod10Check modCheck = createMod10CheckAnnotation( 0, 10, 5, false );
		validator.initialize( modCheck );
	}

	@Test
	public void testFailOnNonNumeric() throws Exception {
		Mod10CheckValidator validator = new Mod10CheckValidator();
		Mod10Check modCheck = createMod10CheckAnnotation( 0, Integer.MAX_VALUE, -1, false );
		validator.initialize( modCheck );

		assertFalse( validator.isValid( new MyCustomStringImpl( "A79927398712" ), null ) );
	}

	@Test
	public void testIgnoreNonNumeric() throws Exception {
		Mod10CheckValidator validator = new Mod10CheckValidator();
		Mod10Check modCheck = createMod10CheckAnnotation( 0, Integer.MAX_VALUE, -1, true );
		validator.initialize( modCheck );

		assertTrue( validator.isValid( new MyCustomStringImpl( "A79927398712" ), null ) );
	}

	@Test
	public void testValidMod10() throws Exception {
		Mod10CheckValidator validator = new Mod10CheckValidator();
		Mod10Check modCheck = createMod10CheckAnnotation( 0, Integer.MAX_VALUE, -1, false );
		validator.initialize( modCheck );

		assertTrue( validator.isValid( "79927398712", null ) );
	}

	@Test
	public void testValidMod10WithGivenRange() throws Exception {
		Mod10CheckValidator validator = new Mod10CheckValidator();
		Mod10Check modCheck = createMod10CheckAnnotation( 3, 13, -1, true );
		validator.initialize( modCheck );

		assertTrue( validator.isValid( "xxx-7992739871-x", null ) );
	}

	@Test
	public void testValidMod10WithGivenRangeAndExplicitCheckDigit() throws Exception {
		Mod10CheckValidator validator = new Mod10CheckValidator();
		Mod10Check modCheck = createMod10CheckAnnotation( 3, 13, 15, true );
		validator.initialize( modCheck );

		assertTrue( validator.isValid( "xxx-799273987-x1x", null ) );
	}

	@Test
	public void testValidMod10WithGivenRangeAndCheckDigitIndex() throws Exception {
		Mod10CheckValidator validator = new Mod10CheckValidator();
		Mod10Check modCheck = createMod10CheckAnnotation( 3, 12, 13, true );
		validator.initialize( modCheck );

		assertTrue( validator.isValid( "123-7992739871-2-456", null ) );
	}

	@Test
	public void testInvalidMod10() throws Exception {
		Mod10CheckValidator validator = new Mod10CheckValidator();
		Mod10Check modCheck = createMod10CheckAnnotation( 0, Integer.MAX_VALUE, -1, false );
		validator.initialize( modCheck );

		assertFalse( validator.isValid( new MyCustomStringImpl( "79927398713" ), null ) );
	}

	@Test
	@TestForIssue(jiraKey = "HV-813")
	public void testValidEAN_GTIN_13() throws Exception {
		Mod10CheckValidator validator = new Mod10CheckValidator();
		Mod10Check modCheck = createMod10CheckAnnotation( 0, Integer.MAX_VALUE, -1, true );
		validator.initialize( modCheck );

		assertTrue( validator.isValid( new MyCustomStringImpl( "4 007630 00011 6" ), null ) );
		assertTrue( validator.isValid( new MyCustomStringImpl( "1 234567 89012 8" ), null ) );
	}

	@Test
	@TestForIssue(jiraKey = "HV-813")
	public void testValidEAN_GTIN_14() throws Exception {
		Mod10CheckValidator validator = new Mod10CheckValidator();
		Mod10Check modCheck = createMod10CheckAnnotation( 0, Integer.MAX_VALUE, -1, true );
		validator.initialize( modCheck );

		assertTrue( validator.isValid( new MyCustomStringImpl( "0 40 07630 00011 6" ), null ) );
		assertTrue( validator.isValid( new MyCustomStringImpl( "3 07 12345 00001 0" ), null ) );
	}

	@Test
	@TestForIssue(jiraKey = "HV-813")
	public void testValidEAN_14WithEAN_128() throws Exception {
		Mod10CheckValidator validator = new Mod10CheckValidator();
		Mod10Check modCheck = createMod10CheckAnnotation( 5, 22, -1, true );
		validator.initialize( modCheck );
		assertTrue( validator.isValid( new MyCustomStringImpl( "(01) 1 23 45678 90123 1" ), null ) );
	}

	@Test
	@TestForIssue(jiraKey = "HV-813")
	public void testValidISBN_13() throws Exception {
		Mod10CheckValidator validator = new Mod10CheckValidator();
		Mod10Check modCheck = createMod10CheckAnnotation( 0, Integer.MAX_VALUE, -1, true );
		validator.initialize( modCheck );

		assertTrue( validator.isValid( new MyCustomStringImpl( "978-85-61411-03-9" ), null ) );
		assertTrue( validator.isValid( new MyCustomStringImpl( "978-1-4302-1957-6" ), null ) );
	}

	@Test
	@TestForIssue(jiraKey = "HV-813")
	public void testValidIdentcode() throws Exception {
		Mod10CheckValidator validator = new Mod10CheckValidator();
		Mod10Check modCheck = createMod10CheckAnnotation( 0, Integer.MAX_VALUE, -1, true, 4, 9 );
		validator.initialize( modCheck );

		assertTrue( validator.isValid( new MyCustomStringImpl( "56.310 243.031 3" ), null ) );
	}

	@Test
	@TestForIssue(jiraKey = "HV-812")
	public void testProgrammaticMod11Constraint() {
		final HibernateValidatorConfiguration config = getConfiguration( HibernateValidator.class );
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( Product.class )
				.field( "productNumber" )
				.constraint(
						new Mod10CheckDef()
								.multiplier( 3 )
								.weight( 1 )
								.startIndex( 0 )
								.endIndex( 12 )
								.checkDigitIndex( -1 )
								.ignoreNonDigitCharacters( true )
				);
		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		Product product = new Product( "P-79927398712" );

		Set<ConstraintViolation<Product>> constraintViolations = validator.validate( product );
		assertNoViolations( constraintViolations );
	}

	private Mod10Check createMod10CheckAnnotation(int start, int end, int checkDigitIndex, boolean ignoreNonDigits, int multiplier, int weight) {
		ConstraintAnnotationDescriptor.Builder<Mod10Check> descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( Mod10Check.class );
		descriptorBuilder.setAttribute( "startIndex", start );
		descriptorBuilder.setAttribute( "endIndex", end );
		descriptorBuilder.setAttribute( "checkDigitIndex", checkDigitIndex );
		descriptorBuilder.setAttribute( "ignoreNonDigitCharacters", ignoreNonDigits );
		descriptorBuilder.setAttribute( "multiplier", multiplier );
		descriptorBuilder.setAttribute( "weight", weight );

		return descriptorBuilder.build().getAnnotation();
	}

	private Mod10Check createMod10CheckAnnotation(int start, int end, int checkDigitIndex, boolean ignoreNonDigits) {
		return this.createMod10CheckAnnotation( start, end, checkDigitIndex, ignoreNonDigits, 3, 1 );
	}

	private static class Product {
		private final String productNumber;

		private Product(String productNumber) {
			this.productNumber = productNumber;
		}
	}
}
