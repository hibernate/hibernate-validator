/*
* JBoss, Home of Professional Open Source
* Copyright 2012, Red Hat, Inc. and/or its affiliates, and individual contributors
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.hibernate.validator.test.internal.constraintvalidators;

import org.testng.annotations.Test;

import org.hibernate.validator.constraints.LuhnCheck;
import org.hibernate.validator.internal.constraintvalidators.LuhnCheckValidator;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationDescriptor;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationFactory;

import static org.testng.Assert.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

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
		AnnotationDescriptor<LuhnCheck> descriptor = new AnnotationDescriptor<LuhnCheck>( LuhnCheck.class );
		descriptor.setValue( "startIndex", start );
		descriptor.setValue( "endIndex", end );
		descriptor.setValue( "checkDigitIndex", checkDigitIndex );
		descriptor.setValue( "ignoreNonDigitCharacters", ignoreNonDigits );

		return AnnotationFactory.create( descriptor );
	}
}
