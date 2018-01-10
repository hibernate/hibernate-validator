/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.constraintvalidators.hv;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.hibernate.validator.constraints.LuhnCheck;
import org.hibernate.validator.internal.constraintvalidators.hv.LuhnCheckValidator;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;
import org.hibernate.validator.testutil.MyCustomStringImpl;

import org.testng.annotations.Test;

/**
 * Tests for the {@code LuhnCheckValidator}.
 *
 * @author Hardy Ferentschik
 * @author Victor Rezende dos Santos
 */
public class LuhnCheckValidatorTest {

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testInvalidStartIndex() {
		LuhnCheckValidator validator = new LuhnCheckValidator();
		LuhnCheck modCheck = createLuhnCheckAnnotation( -1, Integer.MAX_VALUE, -1, false );
		validator.initialize( modCheck );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testInvalidEndIndex() {
		LuhnCheckValidator validator = new LuhnCheckValidator();
		LuhnCheck modCheck = createLuhnCheckAnnotation( 0, -1, -1, false );
		validator.initialize( modCheck );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testEndIndexLessThanStartIndex() {
		LuhnCheckValidator validator = new LuhnCheckValidator();
		LuhnCheck modCheck = createLuhnCheckAnnotation( 5, 0, -1, false );
		validator.initialize( modCheck );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testInvalidCheckDigitIndex() {
		LuhnCheckValidator validator = new LuhnCheckValidator();
		LuhnCheck modCheck = createLuhnCheckAnnotation( 0, 10, 5, false );
		validator.initialize( modCheck );
	}

	@Test
	public void testFailOnNonNumeric() throws Exception {
		LuhnCheckValidator validator = new LuhnCheckValidator();
		LuhnCheck modCheck = createLuhnCheckAnnotation( 0, Integer.MAX_VALUE, -1, false );
		validator.initialize( modCheck );

		assertFalse( validator.isValid( new MyCustomStringImpl( "A79927398713" ), null ) );
	}

	@Test
	public void testIgnoreNonNumeric() throws Exception {
		LuhnCheckValidator validator = new LuhnCheckValidator();
		LuhnCheck modCheck = createLuhnCheckAnnotation( 0, Integer.MAX_VALUE, -1, true );
		validator.initialize( modCheck );

		assertTrue( validator.isValid( new MyCustomStringImpl( "A79927398713" ), null ) );
	}

	@Test
	public void testValidMod10() throws Exception {
		LuhnCheckValidator validator = new LuhnCheckValidator();
		LuhnCheck modCheck = createLuhnCheckAnnotation( 0, Integer.MAX_VALUE, -1, false );
		validator.initialize( modCheck );

		assertTrue( validator.isValid( "79927398713", null ) );
	}

	@Test
	public void testValidMod10WithGivenRange() throws Exception {
		LuhnCheckValidator validator = new LuhnCheckValidator();
		LuhnCheck modCheck = createLuhnCheckAnnotation( 3, 15, -1, true );
		validator.initialize( modCheck );

		assertTrue( validator.isValid( "123-7992739871-3", null ) );
	}

	@Test
	public void testValidMod10WithGivenRangeAndCheckDigitIndex() throws Exception {
		LuhnCheckValidator validator = new LuhnCheckValidator();
		LuhnCheck modCheck = createLuhnCheckAnnotation( 3, 13, 15, true );
		validator.initialize( modCheck );

		assertTrue( validator.isValid( "123-7992739871-3-456", null ) );
	}

	@Test
	public void testInvalidMod10() throws Exception {
		LuhnCheckValidator validator = new LuhnCheckValidator();
		LuhnCheck modCheck = createLuhnCheckAnnotation( 0, Integer.MAX_VALUE, -1, false );
		validator.initialize( modCheck );

		assertFalse( validator.isValid( new MyCustomStringImpl( "79927398714" ), null ) );
	}

	private LuhnCheck createLuhnCheckAnnotation(int start, int end, int checkDigitIndex, boolean ignoreNonDigits) {
		ConstraintAnnotationDescriptor.Builder<LuhnCheck> descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( LuhnCheck.class );
		descriptorBuilder.setAttribute( "startIndex", start );
		descriptorBuilder.setAttribute( "endIndex", end );
		descriptorBuilder.setAttribute( "checkDigitIndex", checkDigitIndex );
		descriptorBuilder.setAttribute( "ignoreNonDigitCharacters", ignoreNonDigits );

		return descriptorBuilder.build().getAnnotation();
	}
}
