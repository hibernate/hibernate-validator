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
package org.hibernate.validator.test.engine.messageinterpolation;

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

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import org.hibernate.validator.engine.MessageInterpolatorContext;
import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;
import org.hibernate.validator.metadata.ConstraintDescriptorImpl;
import org.hibernate.validator.metadata.ConstraintHelper;
import org.hibernate.validator.metadata.ConstraintOrigin;
import org.hibernate.validator.resourceloading.ResourceBundleLocator;
import org.hibernate.validator.util.annotationfactory.AnnotationDescriptor;
import org.hibernate.validator.util.annotationfactory.AnnotationFactory;

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
				notNull,
				new ConstraintHelper(),
				java.lang.annotation.ElementType.FIELD,
				ConstraintOrigin.DEFINED_LOCALLY
		);

		AnnotationDescriptor<Size> sizeAnnotationDescriptor = new AnnotationDescriptor<Size>( Size.class );
		size = AnnotationFactory.create( sizeAnnotationDescriptor );
		sizeDescriptor = new ConstraintDescriptorImpl<Size>(
				size, new ConstraintHelper(), java.lang.annotation.ElementType.FIELD, ConstraintOrigin.DEFINED_LOCALLY
		);
	}

	@Test
	public void testSuccessfulInterpolation() {
		interpolator = new ResourceBundleMessageInterpolator(
				new TestResourceBundleLocator()
		);
		MessageInterpolator.Context context = new MessageInterpolatorContext( notNullDescriptor, null );
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
		MessageInterpolator.Context context = new MessageInterpolatorContext( notNullDescriptor, null );

		String expected = "{";
		String actual = interpolator.interpolate( "\\{", context );
		assertEquals( actual, expected, "Wrong substitution" );

		expected = "}";
		actual = interpolator.interpolate( "\\}", context );
		assertEquals( actual, expected, "Wrong substitution" );

		expected = "\\";
		actual = interpolator.interpolate( "\\", context );
		assertEquals( actual, expected, "Wrong substitution" );
	}

	@Test
	public void testUnSuccessfulInterpolation() {
		interpolator = new ResourceBundleMessageInterpolator(
				new TestResourceBundleLocator()
		);
		MessageInterpolator.Context context = new MessageInterpolatorContext( notNullDescriptor, null );

		String expected = "foo";  // missing {}
		String actual = interpolator.interpolate( "foo", context );
		assertEquals( actual, expected, "Wrong substitution" );

		expected = "#{foo  {}";
		actual = interpolator.interpolate( "#{foo  {}", context );
		assertEquals( actual, expected, "Wrong substitution" );
	}

	@Test
	public void testUnknownTokenInterpolation() {
		interpolator = new ResourceBundleMessageInterpolator(
				new TestResourceBundleLocator()
		);
		MessageInterpolator.Context context = new MessageInterpolatorContext( notNullDescriptor, null );

		String expected = "{bar}";  // unknown token {}
		String actual = interpolator.interpolate( "{bar}", context );
		assertEquals( actual, expected, "Wrong substitution" );
	}

	@Test
	public void testKeyWithDashes() {
		interpolator = new ResourceBundleMessageInterpolator(
				new TestResourceBundleLocator()
		);
		MessageInterpolator.Context context = new MessageInterpolatorContext( notNullDescriptor, null );

		String expected = "message interpolation successful";  // unknown token {}
		String actual = interpolator.interpolate( "{key-with-dashes}", context );
		assertEquals( actual, expected, "Wrong substitution" );
	}

	@Test
	public void testKeyWithSpaces() {
		interpolator = new ResourceBundleMessageInterpolator(
				new TestResourceBundleLocator()
		);
		MessageInterpolator.Context context = new MessageInterpolatorContext( notNullDescriptor, null );

		String expected = "message interpolation successful";  // unknown token {}
		String actual = interpolator.interpolate( "{key with spaces}", context );
		assertEquals( actual, expected, "Wrong substitution" );
	}

	@Test
	public void testDefaultInterpolation() {
		interpolator = new ResourceBundleMessageInterpolator(
				new TestResourceBundleLocator()
		);
		MessageInterpolator.Context context = new MessageInterpolatorContext( notNullDescriptor, null );

		String expected = "may not be null";
		String actual = interpolator.interpolate( notNull.message(), context );
		assertEquals( actual, expected, "Wrong substitution" );

		expected = "size must be between 0 and 2147483647";  // unknown token {}
		context = new MessageInterpolatorContext( sizeDescriptor, null );
		actual = interpolator.interpolate( size.message(), context );
		assertEquals( actual, expected, "Wrong substitution" );
	}

	@Test
	public void testMessageInterpolationWithLocale() {
		interpolator = new ResourceBundleMessageInterpolator();

		String expected = "kann nicht null sein";
		MessageInterpolator.Context context = new MessageInterpolatorContext( notNullDescriptor, null );
		String actual = interpolator.interpolate( notNull.message(), context, Locale.GERMAN );
		assertEquals( actual, expected, "Wrong substitution" );
	}

	@Test
	public void testUserResourceBundle() {
		interpolator = new ResourceBundleMessageInterpolator();
		MessageInterpolator.Context context = new MessageInterpolatorContext( notNullDescriptor, null );

		String expected = "no puede ser null";
		String actual = interpolator.interpolate( notNull.message(), context, new Locale( "es", "ES" ) );
		assertEquals( actual, expected, "Wrong substitution" );
	}

	/**
	 * HV-102
	 */
	@Test
	public void testRecursiveMessageInterpolation() {
		AnnotationDescriptor<Max> descriptor = new AnnotationDescriptor<Max>( Max.class );
		descriptor.setValue( "message", "{replace.in.user.bundle1}" );
		descriptor.setValue( "value", 10l );
		Max max = AnnotationFactory.create( descriptor );


		ConstraintDescriptorImpl<Max> constraintDescriptor = new ConstraintDescriptorImpl<Max>(
				max, new ConstraintHelper(), java.lang.annotation.ElementType.FIELD, ConstraintOrigin.DEFINED_LOCALLY
		);

		interpolator = new ResourceBundleMessageInterpolator(
				new TestResourceBundleLocator()
		);
		MessageInterpolator.Context context = new MessageInterpolatorContext( constraintDescriptor, null );

		String expected = "{replace.in.default.bundle2}";
		String actual = interpolator.interpolate( max.message(), context );
		assertEquals(
				actual, expected, "Within default bundle replacement parameter evaluation should not be recursive!"
		);
	}

	/**
	 * HV-182
	 */
	@Test
	public void testCorrectMessageInterpolationIfParameterCannotBeReplaced() {
		AnnotationDescriptor<Max> descriptor = new AnnotationDescriptor<Max>( Max.class );
		String message = "Message should stay unchanged since {fubar} is not replaceable";
		descriptor.setValue( "message", message );
		descriptor.setValue( "value", 10l );
		Max max = AnnotationFactory.create( descriptor );


		ConstraintDescriptorImpl<Max> constraintDescriptor = new ConstraintDescriptorImpl<Max>(
				max, new ConstraintHelper(), java.lang.annotation.ElementType.FIELD, ConstraintOrigin.DEFINED_LOCALLY
		);

		interpolator = new ResourceBundleMessageInterpolator(
				new TestResourceBundleLocator()
		);

		MessageInterpolator.Context context = new MessageInterpolatorContext( constraintDescriptor, null );

		String actual = interpolator.interpolate( max.message(), context );
		assertEquals(
				actual, message, "The message should not have changed."
		);
	}

	/**
	 * HV-330
	 */
	@Test
	public void testMessageCaching() {

		// do the whole tests first with caching enabled
		TestResourceBundle testBundle = new TestResourceBundle();
		interpolator = new ResourceBundleMessageInterpolator(
				new TestResourceBundleLocator( testBundle )
		);
		MessageInterpolator.Context context = new MessageInterpolatorContext( notNullDescriptor, null );

		String expected = "{hv-330}";
		String actual = interpolator.interpolate( "{hv-330}", context );
		assertEquals( actual, expected, "The key should not not exist in the bundle." );

		testBundle.addOrUpdateMessage( "hv-330", "success" );
		expected = "{hv-330}";
		actual = interpolator.interpolate( "{hv-330}", context );
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
		context = new MessageInterpolatorContext( notNullDescriptor, null );

		expected = "{hv-330}";
		actual = interpolator.interpolate( "{hv-330}", context );
		assertEquals( actual, expected, "The key should not not exist in the bundle." );

		testBundle.addOrUpdateMessage( "hv-330", "success" );
		expected = "success";
		actual = interpolator.interpolate( "{hv-330}", context );
		assertEquals(
				actual,
				expected,
				"The message should change since ResourceBundleMessageInterpolator does not cache"
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

		public ResourceBundle getResourceBundle(Locale locale) {
			return resourceBundle;
		}
	}

	/**
	 * A dummy resource bundle which can be passed to the constructor of ResourceBundleMessageInterpolator to replace
	 * the user specified resource bundle.
	 */
	private static class TestResourceBundle extends ResourceBundle implements Enumeration<String> {
		private Map<String, String> testResources;
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

		public Object handleGetObject(String key) {
			return testResources.get( key );
		}

		public Enumeration<String> getKeys() {
			return this;
		}

		public boolean hasMoreElements() {
			return iter.hasNext();
		}

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
