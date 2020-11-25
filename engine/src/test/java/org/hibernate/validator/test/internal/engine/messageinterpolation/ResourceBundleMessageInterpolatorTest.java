/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.messageinterpolation;

import static org.testng.Assert.assertEquals;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.validation.MessageInterpolator;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.hibernate.validator.internal.engine.MessageInterpolatorContext;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation.ConstraintLocationKind;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;
import org.hibernate.validator.messageinterpolation.ExpressionLanguageFeatureLevel;
import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;
import org.hibernate.validator.spi.resourceloading.ResourceBundleLocator;
import org.hibernate.validator.testutil.TestForIssue;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * Tests for message interpolation.
 *
 * @author Hardy Ferentschik
 */
public class ResourceBundleMessageInterpolatorTest {

	private ResourceBundleMessageInterpolator interpolator;
	private ConstraintDescriptorImpl<NotNull> notNullDescriptor;
	private ConstraintDescriptorImpl<Size> sizeDescriptor;

	@BeforeTest
	public void setUp() {
		// Create some annotations for testing using AnnotationProxies
		ConstraintAnnotationDescriptor.Builder<NotNull> descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( NotNull.class );
		notNullDescriptor = new ConstraintDescriptorImpl<>(
				ConstraintHelper.forAllBuiltinConstraints(),
				null,
				descriptorBuilder.build(),
				ConstraintLocationKind.FIELD
		);

		ConstraintAnnotationDescriptor.Builder<Size> sizeAnnotationDescriptorBuilder = new ConstraintAnnotationDescriptor.Builder<Size>( Size.class );
		sizeDescriptor = new ConstraintDescriptorImpl<>(
				ConstraintHelper.forAllBuiltinConstraints(),
				null,
				sizeAnnotationDescriptorBuilder.build(),
				ConstraintLocationKind.FIELD
		);
	}

	@Test
	public void testSuccessfulInterpolation() {
		interpolator = new ResourceBundleMessageInterpolator(
				new TestResourceBundleLocator()
		);
		MessageInterpolator.Context context = createMessageInterpolatorContext( notNullDescriptor );
		String expected = "message interpolation successful";
		String actual = interpolator.interpolate( "{simple.key}", context );
		assertEquals( actual, expected, "Wrong substitution" );

		expected = "message interpolation successful message interpolation successful";
		actual = interpolator.interpolate( "{simple.key} {simple.key}", context );
		assertEquals( actual, expected, "Wrong substitution" );

		expected = "The message interpolation successful completed";
		actual = interpolator.interpolate( "The {simple.key} completed", context );
		assertEquals( actual, expected, "Wrong substitution" );

		expected = "{{simple.key}}";
		actual = interpolator.interpolate( "{{simple.key}}", context );
		assertEquals( actual, expected, "Wrong substitution" );
	}

	@Test
	public void testMessageLiterals() {
		interpolator = new ResourceBundleMessageInterpolator(
				new TestResourceBundleLocator()
		);
		MessageInterpolatorContext messageInterpolatorContext = createMessageInterpolatorContext( notNullDescriptor );

		String expected = "{";
		String actual = interpolator.interpolate( "\\{", messageInterpolatorContext );
		assertEquals( actual, expected, "Wrong substitution" );

		expected = "}";
		actual = interpolator.interpolate( "\\}", messageInterpolatorContext );
		assertEquals( actual, expected, "Wrong substitution" );

		expected = "\\";
		actual = interpolator.interpolate( "\\", messageInterpolatorContext );
		assertEquals( actual, expected, "Wrong substitution" );
	}

	@Test
	public void testUnSuccessfulInterpolation() {
		interpolator = new ResourceBundleMessageInterpolator(
				new TestResourceBundleLocator()
		);
		MessageInterpolatorContext messageInterpolatorContext = createMessageInterpolatorContext( notNullDescriptor );

		String expected = "foo";  // missing {}
		String actual = interpolator.interpolate( "foo", messageInterpolatorContext );
		assertEquals( actual, expected, "Wrong substitution" );

		expected = "#{foo  {}";
		actual = interpolator.interpolate( "#{foo  {}", messageInterpolatorContext );
		assertEquals( actual, expected, "Wrong substitution" );
	}

	@Test
	public void testUnknownTokenInterpolation() {
		interpolator = new ResourceBundleMessageInterpolator(
				new TestResourceBundleLocator()
		);
		MessageInterpolatorContext messageInterpolatorContext = createMessageInterpolatorContext( notNullDescriptor );

		String expected = "{bar}";  // unknown token {}
		String actual = interpolator.interpolate( "{bar}", messageInterpolatorContext );
		assertEquals( actual, expected, "Wrong substitution" );
	}

	@Test
	public void testKeyWithDashes() {
		interpolator = new ResourceBundleMessageInterpolator(
				new TestResourceBundleLocator()
		);
		MessageInterpolatorContext messageInterpolatorContext = createMessageInterpolatorContext( notNullDescriptor );

		String expected = "message interpolation successful";  // unknown token {}
		String actual = interpolator.interpolate( "{key-with-dashes}", messageInterpolatorContext );
		assertEquals( actual, expected, "Wrong substitution" );
	}

	@Test
	public void testKeyWithSpaces() {
		interpolator = new ResourceBundleMessageInterpolator(
				new TestResourceBundleLocator()
		);
		MessageInterpolatorContext messageInterpolatorContext = createMessageInterpolatorContext( notNullDescriptor );

		String expected = "message interpolation successful";  // unknown token {}
		String actual = interpolator.interpolate( "{key with spaces}", messageInterpolatorContext );
		assertEquals( actual, expected, "Wrong substitution" );
	}

	@Test
	public void testDefaultInterpolation() {
		interpolator = new ResourceBundleMessageInterpolator(
				new TestResourceBundleLocator()
		);
		MessageInterpolatorContext messageInterpolatorContext = createMessageInterpolatorContext( notNullDescriptor );

		String expected = "must not be null";
		String actual = interpolator.interpolate( notNullDescriptor.getAnnotation().message(), messageInterpolatorContext );
		assertEquals( actual, expected, "Wrong substitution" );

		expected = "size must be between 0 and 2147483647";  // unknown token {}

		messageInterpolatorContext = createMessageInterpolatorContext( sizeDescriptor );
		actual = interpolator.interpolate( sizeDescriptor.getAnnotation().message(), messageInterpolatorContext );
		assertEquals( actual, expected, "Wrong substitution" );
	}

	@Test
	public void testMessageInterpolationWithLocale() {
		interpolator = new ResourceBundleMessageInterpolator();
		MessageInterpolatorContext messageInterpolatorContext = createMessageInterpolatorContext( notNullDescriptor );

		String expected = "darf nicht null sein";
		String actual = interpolator.interpolate( notNullDescriptor.getAnnotation().message(), messageInterpolatorContext, Locale.GERMAN );
		assertEquals( actual, expected, "Wrong substitution" );
	}

	@Test
	public void testUserResourceBundle() {
		interpolator = new ResourceBundleMessageInterpolator();
		MessageInterpolatorContext messageInterpolatorContext = createMessageInterpolatorContext( notNullDescriptor );

		String expected = "no puede ser null";
		String actual = interpolator.interpolate(
				notNullDescriptor.getAnnotation().message(),
				messageInterpolatorContext,
				new Locale( "es", "ES" )
		);
		assertEquals( actual, expected, "Wrong substitution" );
	}

	@Test
	@TestForIssue(jiraKey = "HV-102")
	public void testRecursiveMessageInterpolation() {
		ConstraintAnnotationDescriptor.Builder<Max> descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( Max.class );
		descriptorBuilder.setMessage( "{replace.in.user.bundle1}" );
		descriptorBuilder.setAttribute( "value", 10L );

		ConstraintAnnotationDescriptor<Max> descriptor = descriptorBuilder.build();

		ConstraintDescriptorImpl<Max> constraintDescriptor = new ConstraintDescriptorImpl<Max>(
				ConstraintHelper.forAllBuiltinConstraints(),
				null,
				descriptorBuilder.build(),
				ConstraintLocationKind.FIELD
		);

		interpolator = new ResourceBundleMessageInterpolator(
				new TestResourceBundleLocator()
		);
		MessageInterpolator.Context messageInterpolatorContext = createMessageInterpolatorContext( constraintDescriptor );

		String expected = "{replace.in.default.bundle2}";
		String actual = interpolator.interpolate( descriptor.getAnnotation().message(), messageInterpolatorContext );
		assertEquals(
				actual, expected, "Within default bundle replacement parameter evaluation should not be recursive!"
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-182")
	public void testCorrectMessageInterpolationIfParameterCannotBeReplaced() {
		ConstraintAnnotationDescriptor.Builder<Max> descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( Max.class );
		String message = "Message should stay unchanged since {fubar} is not replaceable";
		descriptorBuilder.setMessage( message );
		descriptorBuilder.setAttribute( "value", 10L );

		ConstraintAnnotationDescriptor<Max> maxDescriptor = descriptorBuilder.build();

		ConstraintDescriptorImpl<Max> constraintDescriptor = new ConstraintDescriptorImpl<Max>(
				ConstraintHelper.forAllBuiltinConstraints(),
				null,
				maxDescriptor,
				ConstraintLocationKind.FIELD
		);

		interpolator = new ResourceBundleMessageInterpolator(
				new TestResourceBundleLocator()
		);

		MessageInterpolator.Context messageInterpolatorContext = createMessageInterpolatorContext( constraintDescriptor );

		String actual = interpolator.interpolate( maxDescriptor.getMessage(), messageInterpolatorContext );
		assertEquals(
				actual, message, "The message should not have changed."
		);
	}

	@Test(expectedExceptions = RuntimeException.class,
			expectedExceptionsMessageRegExp = "ReadOnceOnlyResourceBundle can only be accessed once!")
	@TestForIssue(jiraKey = "HV-330")
	public void testResourceBundleGetsAccessedMultipleTimesWhenCachingIsDisabled() {
		runInterpolation( false );
	}

	@Test
	@TestForIssue(jiraKey = "HV-330")
	public void testResourceBundleGetsAccessedOnlyOnceWhenCachingIsEnabled() {
		runInterpolation( true );
	}

	private MessageInterpolatorContext createMessageInterpolatorContext(ConstraintDescriptorImpl<?> descriptor) {
		return new MessageInterpolatorContext(
				descriptor,
				null,
				null,
				null,
				Collections.<String, Object>emptyMap(),
				Collections.<String, Object>emptyMap(),
				ExpressionLanguageFeatureLevel.BEAN_METHODS,
				false );
	}

	private void runInterpolation(boolean cachingEnabled) {
		ReadOnceOnlyResourceBundle testBundle = new ReadOnceOnlyResourceBundle();
		interpolator = new ResourceBundleMessageInterpolator(
				new TestResourceBundleLocator( testBundle ), cachingEnabled
		);
		MessageInterpolator.Context messageInterpolatorContext = createMessageInterpolatorContext( notNullDescriptor );

		// the ReadOnceOnlyResourceBundle will throw an exception in case the bundle is read more than once!
		for ( int i = 0; i < 3; i++ ) {
			String expected = "fixed";
			String actual = interpolator.interpolate( "{hv-330}", messageInterpolatorContext );
			assertEquals( actual, expected, "Wrong interpolation" );
		}
	}

	/**
	 * A dummy locator always returning a {@link org.hibernate.validator.test.internal.engine.messageinterpolation.ResourceBundleMessageInterpolatorTest.ReadOnceOnlyResourceBundle}.
	 */
	private static class TestResourceBundleLocator implements ResourceBundleLocator {

		private final ResourceBundle resourceBundle;

		public TestResourceBundleLocator() {
			this( new TestResourceBundle() );
		}

		public TestResourceBundleLocator(ResourceBundle bundle) {
			resourceBundle = bundle;
		}

		@Override
		public ResourceBundle getResourceBundle(Locale locale) {
			return resourceBundle;
		}
	}

	/**
	 * A dummy resource bundle which can be passed to the constructor of ResourceBundleMessageInterpolator to replace
	 * the user specified resource bundle.
	 */
	private static class TestResourceBundle extends ResourceBundle implements Enumeration<String> {
		private final Map<String, String> testResources;
		Iterator<String> iter;

		public TestResourceBundle() {
			testResources = new HashMap<String, String>();
			// add some test messages
			testResources.put( "simple.key", "message interpolation successful" );
			testResources.put( "key-with-dashes", "message interpolation successful" );
			testResources.put( "key with spaces", "message interpolation successful" );
			testResources.put( "replace.in.user.bundle1", "{replace.in.user.bundle2}" );
			testResources.put( "replace.in.user.bundle2", "{replace.in.default.bundle1}" );

			iter = testResources.keySet().iterator();
		}

		@Override
		public Object handleGetObject(String key) {
			return testResources.get( key );
		}

		@Override
		public Enumeration<String> getKeys() {
			return this;
		}

		@Override
		public boolean hasMoreElements() {
			return iter.hasNext();
		}

		@Override
		public String nextElement() {
			if ( hasMoreElements() ) {
				return iter.next();
			}
			else {
				throw new NoSuchElementException();
			}
		}
	}

	/**
	 * A dummy resource bundle which can be passed to the constructor of ResourceBundleMessageInterpolator to replace
	 * the user specified resource bundle.
	 */
	private static class ReadOnceOnlyResourceBundle extends ResourceBundle {
		private AtomicInteger counter;
		private final String key = "hv-330";
		private final String value = "fixed";

		public ReadOnceOnlyResourceBundle() {
			counter = new AtomicInteger( 1 );
		}

		@Override
		public Object handleGetObject(String key) {
			if ( counter.decrementAndGet() < 0 ) {
				throw new RuntimeException( "ReadOnceOnlyResourceBundle can only be accessed once!" );
			}
			if ( this.key.equals( key ) ) {
				return value;
			}
			else {
				throw new RuntimeException( "Unexpected key: " + key );
			}

		}

		@Override
		public Enumeration<String> getKeys() {
			return new Enumeration<String>() {
				AtomicBoolean hasMoreElements = new AtomicBoolean( Boolean.TRUE );

				@Override
				public boolean hasMoreElements() {
					return hasMoreElements.getAndSet( Boolean.FALSE );
				}

				@Override
				public String nextElement() {
					return value;
				}
			};
		}
	}
}
