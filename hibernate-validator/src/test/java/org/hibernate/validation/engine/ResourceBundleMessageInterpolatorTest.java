// $Id$
/*
* JBoss, Home of Professional Open Source
* Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.hibernate.validation.engine;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.validation.constraints.Max;
import javax.validation.MessageInterpolator;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

import org.hibernate.validation.util.annotationfactory.AnnotationDescriptor;
import org.hibernate.validation.util.annotationfactory.AnnotationFactory;

/**
 * Tests for message resolution.
 *
 * @author Hardy Ferentschik
 */
public class ResourceBundleMessageInterpolatorTest {

	private ResourceBundleMessageInterpolator interpolator;
	private NotNull notNull;
	private Size size;

	@Before
	public void setUp() {
		// Create some annotations for testing using AnnotationProxies
		AnnotationDescriptor<NotNull> descriptor = new AnnotationDescriptor<NotNull>( NotNull.class );
		notNull = AnnotationFactory.create( descriptor );

		AnnotationDescriptor<Size> sizeDescriptor = new AnnotationDescriptor<Size>( Size.class );
		size = AnnotationFactory.create( sizeDescriptor );
	}

	@Test
	public void testSuccessfulInterpolation() {
		ConstraintDescriptorImpl<NotNull> descriptor = new ConstraintDescriptorImpl<NotNull>(
				notNull, new Class<?>[] { }, new ConstraintHelper()
		);

		interpolator = new ResourceBundleMessageInterpolator( new TestResourceBundle() );
		MessageInterpolator.Context context = new MessageInterpolatorContext( descriptor, null );
		String expected = "replacement worked";
		String actual = interpolator.interpolate( "{foo}", context );
		assertEquals( "Wrong substitution", expected, actual );

		expected = "replacement worked replacement worked";
		actual = interpolator.interpolate( "{foo} {foo}", context );
		assertEquals( "Wrong substitution", expected, actual );

		expected = "This replacement worked just fine";
		actual = interpolator.interpolate( "This {foo} just fine", context );
		assertEquals( "Wrong substitution", expected, actual );

		expected = "{} { replacement worked }";
		actual = interpolator.interpolate( "{} { {foo} }", context );
		assertEquals( "Wrong substitution", expected, actual );
	}

	@Test
	public void testUnSuccessfulInterpolation() {
		ConstraintDescriptorImpl<NotNull> descriptor = new ConstraintDescriptorImpl<NotNull>(
				notNull, new Class<?>[] { }, new ConstraintHelper()
		);

		interpolator = new ResourceBundleMessageInterpolator( new TestResourceBundle() );
		MessageInterpolator.Context context = new MessageInterpolatorContext( descriptor, null );

		String expected = "foo";  // missing {}
		String actual = interpolator.interpolate( "foo", context );
		assertEquals( "Wrong substitution", expected, actual );

		expected = "#{foo  {}";
		actual = interpolator.interpolate( "#{foo  {}", context );
		assertEquals( "Wrong substitution", expected, actual );
	}

	@Test
	public void testUnkownTokenInterpolation() {
		ConstraintDescriptorImpl<NotNull> descriptor = new ConstraintDescriptorImpl<NotNull>(
				notNull, new Class<?>[] { }, new ConstraintHelper()
		);

		interpolator = new ResourceBundleMessageInterpolator( new TestResourceBundle() );
		MessageInterpolator.Context context = new MessageInterpolatorContext( descriptor, null );

		String expected = "{bar}";  // unkown token {}
		String actual = interpolator.interpolate( "{bar}", context );
		assertEquals( "Wrong substitution", expected, actual );
	}

	@Test
	public void testDefaultInterpolation() {
		ConstraintDescriptorImpl<NotNull> descriptor = new ConstraintDescriptorImpl<NotNull>(
				notNull, new Class<?>[] { }, new ConstraintHelper()
		);

		interpolator = new ResourceBundleMessageInterpolator( new TestResourceBundle() );
		MessageInterpolator.Context context = new MessageInterpolatorContext( descriptor, null );

		String expected = "may not be null";
		String actual = interpolator.interpolate( notNull.message(), context );
		assertEquals( "Wrong substitution", expected, actual );

		ConstraintDescriptorImpl<Size> sizeDescriptor = new ConstraintDescriptorImpl<Size>(
				size, new Class<?>[] { }, new ConstraintHelper()
		);
		expected = "size must be between 0 and 2147483647";  // unkown token {}
		context = new MessageInterpolatorContext( sizeDescriptor, null );
		actual = interpolator.interpolate( size.message(), context );
		assertEquals( "Wrong substitution", expected, actual );
	}

	@Test
	public void testMessageInterpolationWithLocale() {
		ConstraintDescriptorImpl<NotNull> descriptor = new ConstraintDescriptorImpl<NotNull>(
				notNull, new Class<?>[] { }, new ConstraintHelper()
		);

		interpolator = new ResourceBundleMessageInterpolator();

		String expected = "kann nicht null sein";
		MessageInterpolator.Context context = new MessageInterpolatorContext( descriptor, null );
		String actual = interpolator.interpolate( notNull.message(), context, Locale.GERMAN );
		assertEquals( "Wrong substitution", expected, actual );
	}

	@Test
	public void testFallbackToDefaultLocale() {
		ConstraintDescriptorImpl<NotNull> descriptor = new ConstraintDescriptorImpl<NotNull>(
				notNull, new Class<?>[] { }, new ConstraintHelper()
		);

		interpolator = new ResourceBundleMessageInterpolator();
		MessageInterpolator.Context context = new MessageInterpolatorContext( descriptor, null );

		String expected = "may not be null";
		String actual = interpolator.interpolate( notNull.message(), context, Locale.JAPAN );
		assertEquals( "Wrong substitution", expected, actual );
	}

	@Test
	public void testUserResourceBundle() {
		ConstraintDescriptorImpl<NotNull> descriptor = new ConstraintDescriptorImpl<NotNull>(
				notNull, new Class<?>[] { }, new ConstraintHelper()
		);

		interpolator = new ResourceBundleMessageInterpolator();
		MessageInterpolator.Context context = new MessageInterpolatorContext( descriptor, null );

		String expected = "no puede ser null";
		String actual = interpolator.interpolate( notNull.message(), context, new Locale( "es", "ES" ) );
		assertEquals( "Wrong substitution", expected, actual );
	}

	/**
	 * HV-102
	 */
	@Test
	public void testRecursiveMessageInterpoliation() {
		AnnotationDescriptor<Max> descriptor = new AnnotationDescriptor<Max>( Max.class );
		descriptor.setValue( "message", "{replace.in.user.bundle1}" );
		descriptor.setValue( "value", 10l);
		Max max = AnnotationFactory.create( descriptor );


		ConstraintDescriptorImpl<Max> constraintDescriptor = new ConstraintDescriptorImpl<Max>(
				max, new Class<?>[] { }, new ConstraintHelper()
		);

		interpolator = new ResourceBundleMessageInterpolator( new TestResourceBundle() );
		MessageInterpolator.Context context = new MessageInterpolatorContext( constraintDescriptor, null );

		String expected = "{replace.in.default.bundle2}";
		String actual = interpolator.interpolate( max.message(), context );
		assertEquals( "Within default bundle replacement parameter evauation should not be recursive!", expected, actual );
	}

	/**
	 * A dummy resource bundle which can be passed to the constructor of ResourceBundleMessageInterpolator to replace
	 * the user specified resource bundle.
	 */
	class TestResourceBundle extends ResourceBundle implements Enumeration<String> {
		private Map<String, String> testResources;
		Iterator<String> iter;

		public TestResourceBundle() {
			testResources = new HashMap<String, String>();
			// add some test messages
			testResources.put( "foo", "replacement worked" );
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
	}
}
