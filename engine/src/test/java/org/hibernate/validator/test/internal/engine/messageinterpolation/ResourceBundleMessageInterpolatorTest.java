/*
* JBoss, Home of Professional Open Source
* Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.test.internal.engine.messageinterpolation;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;
import javax.validation.MessageInterpolator;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.internal.engine.MessageInterpolatorContext;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationDescriptor;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationFactory;
import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;
import org.hibernate.validator.spi.resourceloading.ResourceBundleLocator;
import org.hibernate.validator.testutil.TestForIssue;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Tests for message interpolation.
 *
 * @author Hardy Ferentschik
 */
public class ResourceBundleMessageInterpolatorTest {

	private ResourceBundleMessageInterpolator interpolator;
	private NotNull notNull;
	private ConstraintDescriptorImpl<NotNull> notNullDescriptor;
	private Size size;
	private ConstraintDescriptorImpl<Size> sizeDescriptor;

	@BeforeTest
	public void setUp() {
		// Create some annotations for testing using AnnotationProxies
		AnnotationDescriptor<NotNull> descriptor = new AnnotationDescriptor<NotNull>( NotNull.class );
		notNull = AnnotationFactory.create( descriptor );
		notNullDescriptor = new ConstraintDescriptorImpl<NotNull>(
				new ConstraintHelper(),
				null,
				notNull,
				java.lang.annotation.ElementType.FIELD
		);

		AnnotationDescriptor<Size> sizeAnnotationDescriptor = new AnnotationDescriptor<Size>( Size.class );
		size = AnnotationFactory.create( sizeAnnotationDescriptor );
		sizeDescriptor = new ConstraintDescriptorImpl<Size>(
				new ConstraintHelper(),
				null,
				size,
				java.lang.annotation.ElementType.FIELD
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

		String expected = "may not be null";
		String actual = interpolator.interpolate( notNull.message(), messageInterpolatorContext );
		assertEquals( actual, expected, "Wrong substitution" );

		expected = "size must be between 0 and 2147483647";  // unknown token {}

		messageInterpolatorContext = createMessageInterpolatorContext( sizeDescriptor );
		actual = interpolator.interpolate( size.message(), messageInterpolatorContext );
		assertEquals( actual, expected, "Wrong substitution" );
	}

	@Test
	public void testMessageInterpolationWithLocale() {
		interpolator = new ResourceBundleMessageInterpolator();
		MessageInterpolatorContext messageInterpolatorContext = createMessageInterpolatorContext( notNullDescriptor );

		String expected = "darf nicht null sein";
		String actual = interpolator.interpolate( notNull.message(), messageInterpolatorContext, Locale.GERMAN );
		assertEquals( actual, expected, "Wrong substitution" );
	}

	@Test
	public void testUserResourceBundle() {
		interpolator = new ResourceBundleMessageInterpolator();
		MessageInterpolatorContext messageInterpolatorContext = createMessageInterpolatorContext( notNullDescriptor );

		String expected = "no puede ser null";
		String actual = interpolator.interpolate(
				notNull.message(),
				messageInterpolatorContext,
				new Locale( "es", "ES" )
		);
		assertEquals( actual, expected, "Wrong substitution" );
	}

	@Test
	@TestForIssue(jiraKey = "HV-102")
	public void testRecursiveMessageInterpolation() {
		AnnotationDescriptor<Max> descriptor = new AnnotationDescriptor<Max>( Max.class );
		descriptor.setValue( "message", "{replace.in.user.bundle1}" );
		descriptor.setValue( "value", 10L );
		Max max = AnnotationFactory.create( descriptor );

		ConstraintDescriptorImpl<Max> constraintDescriptor = new ConstraintDescriptorImpl<Max>(
				new ConstraintHelper(),
				null,
				max,
				java.lang.annotation.ElementType.FIELD
		);

		interpolator = new ResourceBundleMessageInterpolator(
				new TestResourceBundleLocator()
		);
		MessageInterpolator.Context messageInterpolatorContext = createMessageInterpolatorContext( constraintDescriptor );

		String expected = "{replace.in.default.bundle2}";
		String actual = interpolator.interpolate( max.message(), messageInterpolatorContext );
		assertEquals(
				actual, expected, "Within default bundle replacement parameter evaluation should not be recursive!"
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-182")
	public void testCorrectMessageInterpolationIfParameterCannotBeReplaced() {
		AnnotationDescriptor<Max> descriptor = new AnnotationDescriptor<Max>( Max.class );
		String message = "Message should stay unchanged since {fubar} is not replaceable";
		descriptor.setValue( "message", message );
		descriptor.setValue( "value", 10L );
		Max max = AnnotationFactory.create( descriptor );


		ConstraintDescriptorImpl<Max> constraintDescriptor = new ConstraintDescriptorImpl<Max>(
				new ConstraintHelper(),
				null,
				max,
				java.lang.annotation.ElementType.FIELD
		);

		interpolator = new ResourceBundleMessageInterpolator(
				new TestResourceBundleLocator()
		);

		MessageInterpolator.Context messageInterpolatorContext = createMessageInterpolatorContext( constraintDescriptor );

		String actual = interpolator.interpolate( max.message(), messageInterpolatorContext );
		assertEquals(
				actual, message, "The message should not have changed."
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-330")
	public void testMessageCaching() {
		// do the whole tests first with caching enabled
		TestResourceBundle testBundle = new TestResourceBundle();
		interpolator = new ResourceBundleMessageInterpolator(
				new TestResourceBundleLocator( testBundle )
		);
		MessageInterpolator.Context messageInterpolatorContext = createMessageInterpolatorContext( notNullDescriptor );

		String expected = "{hv-330}";
		String actual = interpolator.interpolate( "{hv-330}", messageInterpolatorContext );
		assertEquals( actual, expected, "The key should not not exist in the bundle." );

		testBundle.addOrUpdateMessage( "hv-330", "success" );
		expected = "{hv-330}";
		actual = interpolator.interpolate( "{hv-330}", messageInterpolatorContext );
		assertEquals(
				actual,
				expected,
				"The message has not changed since per default the ResourceBundleMessageInterpolator caches the messages"
		);

		// now without caching
		testBundle = new TestResourceBundle();
		interpolator = new ResourceBundleMessageInterpolator(
				new TestResourceBundleLocator( testBundle ), false
		);

		messageInterpolatorContext = createMessageInterpolatorContext( notNullDescriptor );

		expected = "{hv-330}";
		actual = interpolator.interpolate( "{hv-330}", messageInterpolatorContext );
		assertEquals( actual, expected, "The key should not not exist in the bundle." );

		testBundle.addOrUpdateMessage( "hv-330", "success" );
		expected = "success";
		actual = interpolator.interpolate( "{hv-330}", messageInterpolatorContext );
		assertEquals(
				actual,
				expected,
				"The message should change since ResourceBundleMessageInterpolator does not cache"
		);
	}

	private MessageInterpolatorContext createMessageInterpolatorContext(ConstraintDescriptorImpl<?> descriptor) {
		return new MessageInterpolatorContext(
				descriptor,
				null,
				null,
				Collections.<String, Object>emptyMap()
		);
	}

	/**
	 * A dummy locator always returning a {@link TestResourceBundle}.
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

		public void addOrUpdateMessage(String key, String message) {
			testResources.put( key, message );
			iter = testResources.keySet().iterator();
		}
	}
}
