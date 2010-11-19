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

import java.text.DateFormat;
import java.util.Date;
import java.util.ListResourceBundle;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;
import javax.validation.Configuration;
import javax.validation.ConstraintViolation;
import javax.validation.MessageInterpolator;
import javax.validation.Validation;
import javax.validation.ValidationException;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.hibernate.validator.engine.MessageInterpolatorContext;
import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;
import org.hibernate.validator.messageinterpolation.ValueMessageInterpolator;
import org.hibernate.validator.resourceloading.ResourceBundleLocator;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * @author Kevin Pollet - SERLI - (kevin.pollet@serli.com)
 */
public class ValueMessageInterpolatorTest {

	private static final String SCRIPT_LANG = "javascript";

	private static ValueMessageInterpolator interpolator;

	private static ValueMessageResourceBundle valueMessageResourceBundle;

	@BeforeClass
	public static void init() {
		interpolator = new ValueMessageInterpolator( new MockDelegateInterpolator(), SCRIPT_LANG );
		valueMessageResourceBundle = new ValueMessageResourceBundle();
	}

	@Test
	public void testSuccessfulInterpolationWithBootstrap() {
		User user = new User();
		user.setEmail( "hibernate.validator@" );
		user.setAge( 19 );

		//Bootstrap
		Configuration<?> config = Validation.byDefaultProvider().configure();
		MessageInterpolator resourceBundleInterpolator = new ResourceBundleMessageInterpolator(
				new ResourceBundleLocator() {
					public ResourceBundle getResourceBundle(Locale locale) {
						return valueMessageResourceBundle;
					}
				}
		);
		config.messageInterpolator( new ValueMessageInterpolator( resourceBundleInterpolator, SCRIPT_LANG ) );
		ValidatorFactory factory = config.buildValidatorFactory();

		//Validate the object
		Validator validator = factory.getValidator();
		Set<ConstraintViolation<User>> violations = validator.validate( user );
		ConstraintViolation<User> emailViolation = violations.iterator().next();

		assertNotNull( violations );
		assertEquals( violations.size(), 1 );
		assertEquals( emailViolation.getMessage(), "\"hibernate.validator@\" is not a well-formed email address" );
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
	public void testScriptDateFormatting() {
		Date date = new Date();
		String stringToInterpolate = "${validatedValue:java.text.DateFormat.getTimeInstance(java.text.DateFormat.SHORT).format(_)}";

		MessageInterpolatorContext context = new MessageInterpolatorContext( null, date );
		String interpolatedString = interpolator.interpolate( stringToInterpolate, context );

		assertNotNull( interpolatedString );
		assertEquals( interpolatedString, DateFormat.getTimeInstance( DateFormat.SHORT ).format( date ) );
	}

	@Test(expectedExceptions = { ValidationException.class })
	public void testInvalidScriptToStringInterpolation() {
		String stringToInterpolate = "This is the ${validatedValue:_.invalidMethod()}";

		MessageInterpolatorContext context = new MessageInterpolatorContext( null, "interpolated value" );
		interpolator.interpolate( stringToInterpolate, context );
	}

	/**
	 * Mock interpolator who simply return the message to interpolate.
	 */
	private static class MockDelegateInterpolator implements MessageInterpolator {

		public String interpolate(String message, Context context) {
			return message;
		}

		public String interpolate(String message, Context context, Locale locale) {
			return message;
		}
	}

	/**
	 * Create a resource bundle which provide translation message with a validated value
	 * interpolation. (Simulates a user resource bundle)
	 */
	private static class ValueMessageResourceBundle extends ListResourceBundle {

		public ValueMessageResourceBundle() {
			super();
		}

		@Override
		protected Object[][] getContents() {
			return new Object[][] {
					{ //Custom message for @Email constraint
							"org.hibernate.validator.constraints.Email.message",
							"\"${validatedValue}\" is not a well-formed email address"
					}
			};
		}
	}
}
