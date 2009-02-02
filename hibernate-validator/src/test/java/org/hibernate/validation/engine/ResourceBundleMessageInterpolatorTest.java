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
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

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
		interpolator = new ResourceBundleMessageInterpolator( new TestResources() );

		AnnotationDescriptor descriptor = new AnnotationDescriptor( NotNull.class );
		notNull = AnnotationFactory.create( descriptor );

		descriptor = new AnnotationDescriptor( Size.class );
		size = AnnotationFactory.create( descriptor );
	}

	@Test
	public void testSuccessfulInterpolation() {
		ConstraintDescriptorImpl descriptor = new ConstraintDescriptorImpl(
				notNull, new Class<?>[] { }, new BuiltinConstraints()
		);

		String expected = "replacement worked";
		String actual = interpolator.interpolate( "{foo}", descriptor, null );
		assertEquals( "Wrong substitution", expected, actual );

		expected = "replacement worked replacement worked";
		actual = interpolator.interpolate( "{foo} {foo}", descriptor, null );
		assertEquals( "Wrong substitution", expected, actual );

		expected = "This replacement worked just fine";
		actual = interpolator.interpolate( "This {foo} just fine", descriptor, null );
		assertEquals( "Wrong substitution", expected, actual );

		expected = "{} { replacement worked }";
		actual = interpolator.interpolate( "{} { {foo} }", descriptor, null );
		assertEquals( "Wrong substitution", expected, actual );
	}

	@Test
	public void testUnSuccessfulInterpolation() {
		ConstraintDescriptorImpl descriptor = new ConstraintDescriptorImpl(
				notNull, new Class<?>[] { }, new BuiltinConstraints()
		);
		String expected = "foo";  // missing {}
		String actual = interpolator.interpolate( "foo", descriptor, null );
		assertEquals( "Wrong substitution", expected, actual );

		expected = "#{foo  {}";
		actual = interpolator.interpolate( "#{foo  {}", descriptor, null );
		assertEquals( "Wrong substitution", expected, actual );
	}

	@Test
	public void testUnkownTokenInterpolation() {
		ConstraintDescriptorImpl descriptor = new ConstraintDescriptorImpl(
				notNull, new Class<?>[] { }, new BuiltinConstraints()
		);
		String expected = "{bar}";  // unkown token {}
		String actual = interpolator.interpolate( "{bar}", descriptor, null );
		assertEquals( "Wrong substitution", expected, actual );
	}

	@Test
	public void testDefaultInterpolation() {
		ConstraintDescriptorImpl descriptor = new ConstraintDescriptorImpl(
				notNull, new Class<?>[] { }, new BuiltinConstraints()
		);
		String expected = "may not be null";
		String actual = interpolator.interpolate( notNull.message(), descriptor, null );
		assertEquals( "Wrong substitution", expected, actual );

		descriptor = new ConstraintDescriptorImpl( size, new Class<?>[] { }, new BuiltinConstraints() );
		expected = "size must be between -2147483648 and 2147483647";  // unkown token {}
		actual = interpolator.interpolate( size.message(), descriptor, null );
		assertEquals( "Wrong substitution", expected, actual );
	}


	class TestResources extends ResourceBundle implements Enumeration<String> {
		private Map<String, String> testResources;
		Iterator<String> iter;

		public TestResources() {
			testResources = new HashMap<String, String>();
			// add some test messages
			testResources.put( "foo", "replacement worked" );

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
