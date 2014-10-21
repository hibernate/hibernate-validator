/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.constraintvalidators.hv;

import org.testng.annotations.Test;

import org.hibernate.validator.constraints.ModCheck;
import org.hibernate.validator.internal.constraintvalidators.hv.ModCheckValidator;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationDescriptor;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationFactory;
import org.hibernate.validator.testutil.MyCustomStringImpl;

import static org.testng.Assert.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

/**
 * Tests for the {@code ModCheckValidator}.
 *
 * @author Hardy Ferentschik
 */
public class ModCheckValidatorTest {

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testInvalidStartIndex() {
		ModCheckValidator validator = new ModCheckValidator();
		ModCheck modCheck = createModCheckAnnotation( ModCheck.ModType.MOD10, 2, -1, Integer.MAX_VALUE, -1, false );
		validator.initialize( modCheck );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testInvalidEndIndex() {
		ModCheckValidator validator = new ModCheckValidator();
		ModCheck modCheck = createModCheckAnnotation( ModCheck.ModType.MOD10, 2, 0, -1, -1, false );
		validator.initialize( modCheck );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testEndIndexLessThanStartIndex() {
		ModCheckValidator validator = new ModCheckValidator();
		ModCheck modCheck = createModCheckAnnotation( ModCheck.ModType.MOD10, 2, 5, 0, -1, false );
		validator.initialize( modCheck );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testInvalidCheckDigitPosition() {
		ModCheckValidator validator = new ModCheckValidator();
		ModCheck modCheck = createModCheckAnnotation( ModCheck.ModType.MOD10, 2, 0, 10, 5, false );
		validator.initialize( modCheck );
	}

	@Test
	public void testFailOnNonNumeric() throws Exception {
		ModCheckValidator validator = new ModCheckValidator();
		ModCheck modCheck = createModCheckAnnotation( ModCheck.ModType.MOD10, 2, 0, Integer.MAX_VALUE, -1, false );
		validator.initialize( modCheck );

		assertFalse( validator.isValid( new MyCustomStringImpl( "A79927398713" ), null ) );
	}

	@Test
	public void testIgnoreNonNumeric() throws Exception {
		ModCheckValidator validator = new ModCheckValidator();
		ModCheck modCheck = createModCheckAnnotation( ModCheck.ModType.MOD10, 2, 0, Integer.MAX_VALUE, -1, true );
		validator.initialize( modCheck );

		assertTrue( validator.isValid( new MyCustomStringImpl( "A79927398713" ), null ) );
	}

	@Test
	public void testValidMod10() throws Exception {
		ModCheckValidator validator = new ModCheckValidator();
		ModCheck modCheck = createModCheckAnnotation( ModCheck.ModType.MOD10, 2, 0, Integer.MAX_VALUE, -1, false );
		validator.initialize( modCheck );

		assertTrue( validator.isValid( "79927398713", null ) );
	}

	@Test
	public void testInvalidMod10() throws Exception {
		ModCheckValidator validator = new ModCheckValidator();
		ModCheck modCheck = createModCheckAnnotation( ModCheck.ModType.MOD10, 2, 0, Integer.MAX_VALUE, -1, false );
		validator.initialize( modCheck );

		assertFalse( validator.isValid( new MyCustomStringImpl( "79927398714" ), null ) );
	}

	@Test
	public void testValidMod11() throws Exception {
		ModCheckValidator validator = new ModCheckValidator();
		ModCheck modCheck = createModCheckAnnotation( ModCheck.ModType.MOD11, 11, 0, Integer.MAX_VALUE, -1, false );
		validator.initialize( modCheck );

		assertTrue( validator.isValid( "23322023583", null ) );

		assertTrue( validator.isValid( "37879695001", null ) );

		assertTrue( validator.isValid( "33181429643", null ) );
	}

	@Test
	public void testInvalidMod11() throws Exception {
		ModCheckValidator validator = new ModCheckValidator();
		ModCheck modCheck = createModCheckAnnotation( ModCheck.ModType.MOD11, 11, 0, Integer.MAX_VALUE, -1, false );
		validator.initialize( modCheck );

		assertFalse( validator.isValid( "23322023584", null ) );

		assertFalse( validator.isValid( "37879695002", null ) );

		assertFalse( validator.isValid( "33181429652", null ) );

	}

	private ModCheck createModCheckAnnotation(ModCheck.ModType modType, int multiplier, int start, int end, int checkDigitIndex, boolean ignoreNonDigits) {
		AnnotationDescriptor<ModCheck> descriptor = new AnnotationDescriptor<ModCheck>( ModCheck.class );
		descriptor.setValue( "modType", modType );
		descriptor.setValue( "multiplier", multiplier );
		descriptor.setValue( "startIndex", start );
		descriptor.setValue( "endIndex", end );
		descriptor.setValue( "checkDigitPosition", checkDigitIndex );
		descriptor.setValue( "ignoreNonDigitCharacters", ignoreNonDigits );

		return AnnotationFactory.create( descriptor );
	}
}
