/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
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
import org.hibernate.validator.cfg.defs.Mod11CheckDef;
import org.hibernate.validator.constraints.Mod11Check;
import org.hibernate.validator.constraints.Mod11Check.ProcessingDirection;
import org.hibernate.validator.internal.constraintvalidators.hv.Mod11CheckValidator;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;
import org.hibernate.validator.testutil.MyCustomStringImpl;
import org.hibernate.validator.testutil.TestForIssue;

import org.testng.annotations.Test;

/**
 * Tests for the {@code Mod11CheckValidator}.
 *
 * @author Hardy Ferentschik
 * @author Victor Rezende dos Santos
 */
public class Mod11CheckValidatorTest {

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testInvalidStartIndex() {
		Mod11CheckValidator validator = new Mod11CheckValidator();
		Mod11Check modCheck = createMod11CheckAnnotation(
				-1,
				Integer.MAX_VALUE,
				-1,
				false,
				'0',
				'0',
				ProcessingDirection.RIGHT_TO_LEFT
		);
		validator.initialize( modCheck );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testInvalidEndIndex() {
		Mod11CheckValidator validator = new Mod11CheckValidator();
		Mod11Check modCheck = createMod11CheckAnnotation(
				0,
				-1,
				-1,
				false,
				'0',
				'0',
				ProcessingDirection.RIGHT_TO_LEFT
		);
		validator.initialize( modCheck );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testEndIndexLessThanStartIndex() {
		Mod11CheckValidator validator = new Mod11CheckValidator();
		Mod11Check modCheck = createMod11CheckAnnotation(
				5,
				0,
				-1,
				false,
				'0',
				'0',
				ProcessingDirection.RIGHT_TO_LEFT
		);
		validator.initialize( modCheck );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testInvalidcheckDigitIndex() {
		Mod11CheckValidator validator = new Mod11CheckValidator();
		Mod11Check modCheck = createMod11CheckAnnotation(
				0,
				10,
				5,
				false,
				'0',
				'0',
				ProcessingDirection.RIGHT_TO_LEFT
		);
		validator.initialize( modCheck );
	}

	@Test
	public void testFailOnNonNumeric() throws Exception {
		Mod11CheckValidator validator = new Mod11CheckValidator();
		Mod11Check modCheck = createMod11CheckAnnotation(
				0,
				Integer.MAX_VALUE,
				-1,
				false,
				'0',
				'0',
				ProcessingDirection.RIGHT_TO_LEFT
		);
		validator.initialize( modCheck );

		assertFalse( validator.isValid( new MyCustomStringImpl( "Z54679542616" ), null ) );
	}

	@Test
	public void testIgnoreNonNumeric() throws Exception {
		Mod11CheckValidator validator = new Mod11CheckValidator();
		Mod11Check modCheck = createMod11CheckAnnotation(
				0,
				Integer.MAX_VALUE,
				-1,
				true,
				'0',
				'0',
				ProcessingDirection.RIGHT_TO_LEFT
		);
		validator.initialize( modCheck );

		assertTrue( validator.isValid( new MyCustomStringImpl( "Z54679542616" ), null ) );
	}

	@Test
	public void testIgnoreNonNumericWithCharCheckDigit() throws Exception {
		Mod11CheckValidator validator = new Mod11CheckValidator();
		Mod11Check modCheck = createMod11CheckAnnotation(
				0,
				12,
				-1,
				true,
				'0',
				'X',
				ProcessingDirection.LEFT_TO_RIGHT
		);
		validator.initialize( modCheck );

		assertTrue( validator.isValid( "123-456-789-X", null ) );
	}

	@Test
	public void testValidMod11() throws Exception {
		Mod11CheckValidator validator = new Mod11CheckValidator();
		Mod11Check modCheck = createMod11CheckAnnotation(
				0,
				Integer.MAX_VALUE,
				-1,
				true,
				'0',
				'0',
				ProcessingDirection.RIGHT_TO_LEFT
		);
		validator.initialize( modCheck );

		assertTrue( validator.isValid( "23322023583", null ) );
		assertTrue( validator.isValid( "378.796.950-01", null ) );
		assertTrue( validator.isValid( "331.814.296-43", null ) );
	}

	@Test
	public void testInvalidMod11() throws Exception {
		Mod11CheckValidator validator = new Mod11CheckValidator();
		Mod11Check modCheck = createMod11CheckAnnotation(
				0,
				Integer.MAX_VALUE,
				-1,
				true,
				'0',
				'0',
				ProcessingDirection.RIGHT_TO_LEFT
		);
		validator.initialize( modCheck );

		assertFalse( validator.isValid( "23322023584", null ) );
		assertFalse( validator.isValid( "378.796.950-02", null ) );
		assertFalse( validator.isValid( "331.814.296-52", null ) );
	}

	@Test
	public void testValidMod11CharCheckDigit() throws Exception {
		Mod11CheckValidator validator = new Mod11CheckValidator();
		Mod11Check modCheck = createMod11CheckAnnotation(
				0,
				Integer.MAX_VALUE,
				-1,
				false,
				'X',
				'Z',
				ProcessingDirection.RIGHT_TO_LEFT
		);
		validator.initialize( modCheck );

		assertTrue( validator.isValid( "123456Z", null ) );
		assertTrue( validator.isValid( "1234575X", null ) );
	}

	@Test
	public void testInvalidMod11CharCheckDigit() throws Exception {
		Mod11CheckValidator validator = new Mod11CheckValidator();
		Mod11Check modCheck = createMod11CheckAnnotation(
				0,
				Integer.MAX_VALUE,
				-1,
				false,
				'X',
				'Z',
				ProcessingDirection.RIGHT_TO_LEFT
		);
		validator.initialize( modCheck );

		assertFalse( validator.isValid( "123458Z", null ) );
		assertFalse( validator.isValid( "1234557X", null ) );
	}

	@Test
	public void testValidMod11ReverseOrder() throws Exception {
		Mod11CheckValidator validator = new Mod11CheckValidator();
		Mod11Check modCheck = createMod11CheckAnnotation(
				0,
				Integer.MAX_VALUE,
				-1,
				false,
				'X',
				'Z',
				ProcessingDirection.LEFT_TO_RIGHT
		);
		validator.initialize( modCheck );

		assertTrue( validator.isValid( "059697873Z", null ) );
		assertTrue( validator.isValid( "5754321X", null ) );
	}

	@Test
	public void testInvalidMod11ReverseOrder() throws Exception {
		Mod11CheckValidator validator = new Mod11CheckValidator();
		Mod11Check modCheck = createMod11CheckAnnotation(
				0,
				Integer.MAX_VALUE,
				-1,
				false,
				'X',
				'Z',
				ProcessingDirection.LEFT_TO_RIGHT
		);
		validator.initialize( modCheck );

		assertFalse( validator.isValid( "378796950Z", null ) );
		assertFalse( validator.isValid( "1234557X", null ) );
	}

	@Test
	@TestForIssue(jiraKey = "HV-812")
	public void testProgrammaticMod11Constraint() {
		final HibernateValidatorConfiguration config = getConfiguration( HibernateValidator.class );
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( Product.class )
				.field( "productNumber" )
				.constraint(
						new Mod11CheckDef()
								.threshold( Integer.MAX_VALUE )
								.startIndex( 0 )
								.endIndex( 12 )
								.ignoreNonDigitCharacters( true )
								.treatCheck10As( 'X' )
								.treatCheck11As( 'P' )
								.processingDirection( ProcessingDirection.LEFT_TO_RIGHT )
				);
		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		Product product = new Product( "123-456-789-P" );

		Set<ConstraintViolation<Product>> constraintViolations = validator.validate( product );
		assertNoViolations( constraintViolations );
	}

	private Mod11Check createMod11CheckAnnotation(
			int start,
			int end,
			int checkDigitIndex,
			boolean ignoreNonDigits,
			char treatCheck10As,
			char treatCheck11As,
			ProcessingDirection processingDirection) {
		ConstraintAnnotationDescriptor.Builder<Mod11Check> descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( Mod11Check.class );
		descriptorBuilder.setAttribute( "startIndex", start );
		descriptorBuilder.setAttribute( "endIndex", end );
		descriptorBuilder.setAttribute( "checkDigitIndex", checkDigitIndex );
		descriptorBuilder.setAttribute( "ignoreNonDigitCharacters", ignoreNonDigits );
		descriptorBuilder.setAttribute( "treatCheck10As", treatCheck10As );
		descriptorBuilder.setAttribute( "treatCheck11As", treatCheck11As );
		descriptorBuilder.setAttribute( "processingDirection", processingDirection );

		return descriptorBuilder.build().getAnnotation();
	}

	private static class Product {
		private final String productNumber;

		private Product(String productNumber) {
			this.productNumber = productNumber;
		}
	}
}
