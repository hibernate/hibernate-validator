/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.test.engine.messageinterpolation;

import java.util.Locale;
import javax.validation.MessageInterpolator;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.hibernate.validator.engine.MessageInterpolatorContext;
import org.hibernate.validator.messageinterpolation.ValidatedValueInterpolator;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * @author Kevin Pollet - SERLI - (kevin.pollet@serli.com)
 */
public class ValidatedValueInterpolatorTest {

	private static final String SCRIPT_LANG = "javascript";

	private static ValidatedValueInterpolator interpolator;

	@BeforeClass
	public static void init() {
		interpolator = new ValidatedValueInterpolator( new MockDelegateInterpolator(), SCRIPT_LANG );
	}

	@Test
	public void testNoStringInterpolation() {
		String stringToInterpolate = "This string have no validated value interpolation.";
		MessageInterpolatorContext context = new MessageInterpolatorContext( null, null );

		String interpolatedString = interpolator.interpolate( stringToInterpolate, context );

		assertNotNull( interpolatedString );
		assertEquals( interpolatedString, stringToInterpolate );
	}

	@Test
	public void testEmptyStringInterpolation() {
		MessageInterpolatorContext context = new MessageInterpolatorContext( null, null );
		String interpolatedString = interpolator.interpolate( "", context );

		assertNotNull( interpolatedString );
		assertTrue( interpolatedString.isEmpty() );
	}

	@Test
	public void testDefaultToStringInterpolation() {
		String expectedValue = "This is the interpolated value";
		String stringToInterpolate = "This is the ${validatedValue}";

		MessageInterpolatorContext context = new MessageInterpolatorContext( null, "interpolated value" );
		String interpolatedString = interpolator.interpolate( stringToInterpolate, context );

		assertNotNull( interpolatedString );
		assertEquals( interpolatedString, expectedValue );
	}

	@Test
	public void testDefaultToStringNullInterpolation() {
		String expectedValue = "Interpolation of a null value";
		String stringToInterpolate = "Interpolation of a ${validatedValue} value";

		MessageInterpolatorContext context = new MessageInterpolatorContext( null, null );
		String interpolatedString = interpolator.interpolate( stringToInterpolate, context );

		assertNotNull( interpolatedString );
		assertEquals( interpolatedString, expectedValue );
	}

	@Test
	public void testScriptToStringInterpolation() {
		String expectedValue = "Use a script interpolation for integer 12";
		String stringToInterpolate = "Use a script interpolation for integer ${validatedValue:_.toString()}";

		MessageInterpolatorContext context = new MessageInterpolatorContext( null, 12 );
		String interpolatedString = interpolator.interpolate( stringToInterpolate, context );

		assertNotNull( interpolatedString );
		assertEquals( interpolatedString, expectedValue );
	}

	@Test
	public void testInvalidScriptToStringInterpolation() {
		String expectedValue = "This is the interpolated value";
		String stringToInterpolate = "This is the ${validatedValue:_.invalidMethod()}";

		MessageInterpolatorContext context = new MessageInterpolatorContext( null, "interpolated value" );
		String interpolatedString = interpolator.interpolate( stringToInterpolate, context );

		assertNotNull( interpolatedString );
		assertEquals( interpolatedString, expectedValue );
	}

	/**
	 * Mock delegate interpolator who simply return the
	 * message to interpolate.
	 */
	private static class MockDelegateInterpolator implements MessageInterpolator {

		public String interpolate(String message, Context context) {
			return message;
		}

		public String interpolate(String message, Context context, Locale locale) {
			return message;
		}
	}

}
