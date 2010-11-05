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

	private static ValidatedValueInterpolator INTERPOLATOR;

	@BeforeClass
	public static void init() {
		INTERPOLATOR = new ValidatedValueInterpolator( new MockDelegateInterpolator(), SCRIPT_LANG );
	}

	@Test
	public void testNoStringInterpolation() {
		String stringToInterpolate = "This string have no validated value interpolation.";
		MessageInterpolatorContext context = new MessageInterpolatorContext( null, null );

		String interpolatedString = INTERPOLATOR.interpolate( stringToInterpolate, context );

		assertNotNull( interpolatedString );
		assertEquals( interpolatedString, stringToInterpolate );
	}

	@Test
	public void testEmptyStringInterpolation() {
		MessageInterpolatorContext context = new MessageInterpolatorContext( null, null );
		String interpolatedString = INTERPOLATOR.interpolate( "", context );

		assertNotNull( interpolatedString );
		assertTrue( interpolatedString.isEmpty() );
	}

	@Test
	public void testDefaultToStringInterpolation() {
		String expectedValue = "This is the interpolated value";
		String stringToInterpolate = "This is the ${validatedValue}";

		MessageInterpolatorContext context = new MessageInterpolatorContext( null, "interpolated value" );
		String interpolatedString = INTERPOLATOR.interpolate( stringToInterpolate, context );

		assertNotNull( interpolatedString );
		assertEquals( interpolatedString, expectedValue );
	}

	@Test
	public void testDefaultToStringNullInterpolation() {
		String expectedValue = "Interpolation of a null value";
		String stringToInterpolate = "Interpolation of a ${validatedValue} value";

		MessageInterpolatorContext context = new MessageInterpolatorContext( null, null );
		String interpolatedString = INTERPOLATOR.interpolate( stringToInterpolate, context );

		assertNotNull( interpolatedString );
		assertEquals( interpolatedString, expectedValue );
	}

	@Test
	public void testScriptToStringInterpolation() {
		String expectedValue = "Use a script interpolation for integer 12";
		String stringToInterpolate = "Use a script interpolation for integer ${validatedValue:_.toString()}";

		MessageInterpolatorContext context = new MessageInterpolatorContext( null, 12 );
		String interpolatedString = INTERPOLATOR.interpolate( stringToInterpolate, context );

		assertNotNull( interpolatedString );
		assertEquals( interpolatedString, expectedValue );
	}

	@Test
	public void testInvalidScriptToStringInterpolation() {
		String expectedValue = "This is the interpolated value";
		String stringToInterpolate = "This is the ${validatedValue:_.invalidMethod()}";

		MessageInterpolatorContext context = new MessageInterpolatorContext( null, "interpolated value" );
		String interpolatedString = INTERPOLATOR.interpolate( stringToInterpolate, context );

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
