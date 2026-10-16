/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.constraintvalidators.hv;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutils.ValidatorUtil.getConfiguration;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Random;
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
import org.hibernate.validator.testutils.Mod11TestUtil;

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

	@Test
	public void testCheckDigitAtEndIndex() {
		Mod11CheckValidator validator = new Mod11CheckValidator();
		validator.initialize( createMod11CheckAnnotation( 0, 10, 10, false, '0', '0', ProcessingDirection.RIGHT_TO_LEFT ) );

		assertTrue( validator.isValid( "23322023583", null ) );
		assertFalse( validator.isValid( "23322023584", null ) );
	}

	@Test
	public void testThresholdsWeightsAndDirectionsMatchIndependentLegacyReference() {
		Random random = new Random( 813 );
		String characters = "0123456789٠١٢٣٤٥٦٧٨٩ -aXZ";
		for ( int threshold : new int[] { Integer.MIN_VALUE, -1, 0, 1, 2, 3, 9, Integer.MAX_VALUE } ) {
			for ( int[] weights : new int[][] { { }, { 2 }, { 2, 7, 3 }, { -1, 0, 3 }, { Integer.MAX_VALUE, Integer.MIN_VALUE } } ) {
				for ( ProcessingDirection direction : ProcessingDirection.values() ) {
					for ( boolean ignore : new boolean[] { false, true } ) {
						Mod11CheckValidator validator = new Mod11CheckValidator();
						validator.initialize( 0, Integer.MAX_VALUE, -1, ignore, threshold, 'X', 'Z', direction, weights );
						for ( CharSequence value : new CharSequence[] { null, "", "0Z", "6X", "19", "1٩", "٠Z", " -0", "123456Z" } ) {
							assertEquals( validator.isValid( value, null ), Mod11TestUtil.isValidLegacy(
									value, 0, Integer.MAX_VALUE, -1, ignore, threshold, 'X', 'Z', direction == ProcessingDirection.LEFT_TO_RIGHT, weights ) );
						}
						for ( int sample = 0; sample < 20; sample++ ) {
							StringBuilder value = new StringBuilder();
							int length = random.nextInt( 16 );
							for ( int i = 0; i < length; i++ ) {
								value.append( sample % 3 == 0
										? (char) ( '0' + random.nextInt( 10 ) )
										: characters.charAt( random.nextInt( characters.length() ) ) );
							}
							assertEquals( validator.isValid( value, null ), Mod11TestUtil.isValidLegacy(
									value, 0, Integer.MAX_VALUE, -1, ignore, threshold, 'X', 'Z', direction == ProcessingDirection.LEFT_TO_RIGHT, weights ),
									value.toString() );
						}
					}
				}
			}
		}
	}

	@Test
	public void testExceptionalCharacterMappingsRemainLiteral() {
		Mod11CheckValidator validator = new Mod11CheckValidator();
		validator.initialize( 0, Integer.MAX_VALUE, -1, false, 9, 'X', 'Z', ProcessingDirection.RIGHT_TO_LEFT );
		assertTrue( validator.isValid( "6X", null ) );
		assertTrue( validator.isValid( "0Z", null ) );
		assertTrue( validator.isValid( "1٩", null ) );
		assertFalse( validator.isValid( "60", null ) );
		validator.initialize( 0, Integer.MAX_VALUE, -1, false, 9, '0', '0', ProcessingDirection.RIGHT_TO_LEFT );
		assertTrue( validator.isValid( "60", null ) );
		assertTrue( validator.isValid( "00", null ) );
		assertFalse( validator.isValid( "6٠", null ) );
		assertFalse( validator.isValid( "0٠", null ) );
	}

	@Test
	public void testCustomWeightsAndDirectionAtPartialCycles() {
		Mod11CheckValidator validator = new Mod11CheckValidator();
		validator.initialize( 0, Integer.MAX_VALUE, -1, false, 9, 'X', 'Z', ProcessingDirection.RIGHT_TO_LEFT, 2, 7, 3 );
		assertTrue( validator.isValid( "125", null ) );
		validator.initialize( 0, Integer.MAX_VALUE, -1, false, 9, 'X', 'Z', ProcessingDirection.LEFT_TO_RIGHT, 2, 7, 3 );
		assertTrue( validator.isValid( "129", null ) );
	}

	@Test
	public void testSubclassDigitAndNormalizationHooksRemainInUse() {
		Mod11CheckValidator validator = new Mod11CheckValidator() {
			@Override
			protected int extractDigit(char value) {
				return value == 'A' ? 1 : super.extractDigit( value );
			}

			@Override
			protected String stripNonDigitsIfRequired(String value) {
				return value.replace( "-", "" );
			}
		};
		validator.initialize( 0, Integer.MAX_VALUE, -1, true, 9, '0', '0', ProcessingDirection.RIGHT_TO_LEFT );
		assertTrue( validator.isValid( "A-9", null ) );
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
