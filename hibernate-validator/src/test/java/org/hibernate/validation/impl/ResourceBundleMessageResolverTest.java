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
package org.hibernate.validation.impl;

import java.lang.annotation.Annotation;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;
import javax.validation.constraints.NotNull;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

import org.hibernate.validation.constraints.Length;
import org.hibernate.validation.constraints.NotNullConstraint;

/**
 * Tests for message resolution.
 *
 * @author Hardy Ferentschik
 */
public class ResourceBundleMessageResolverTest {

	private ResourceBundleMessageResolver resolver;
	private NotNull notNull;
	private Length length;

	@Before
	public void setUp() {
		resolver = new ResourceBundleMessageResolver( new TestResources() );
		notNull = new NotNull() {
			public String message() {
				return "{validator.notNull}";
			}

			public Class<?>[] groups() {
				return new Class<?>[] { };
			}

			public Class<? extends Annotation> annotationType() {
				return this.getClass();
			}
		};

		length = new Length() {
			public int min() {
				return 0;
			}

			public int max() {
				return Integer.MAX_VALUE;
			}

			public String message() {
				return "{validator.length}";
			}

			public Class<?>[] groups() {
				return new Class<?>[] { };
			}

			public Class<? extends Annotation> annotationType() {
				return this.getClass();
			}
		};
	}

	@Test
	public void testSuccessfulInterpolation() {
		ConstraintDescriptorImpl desciptor = new ConstraintDescriptorImpl(
				notNull, new Class<?>[] { }, new NotNullConstraint(), NotNullConstraint.class
		);

		String expected = "replacement worked";
		String actual = resolver.interpolate( "{foo}", desciptor, null );
		assertEquals( "Wrong substitution", expected, actual );

		expected = "replacement worked replacement worked";
		actual = resolver.interpolate( "{foo} {foo}", desciptor, null );
		assertEquals( "Wrong substitution", expected, actual );

		expected = "This replacement worked just fine";
		actual = resolver.interpolate( "This {foo} just fine", desciptor, null );
		assertEquals( "Wrong substitution", expected, actual );

		expected = "{} { replacement worked }";
		actual = resolver.interpolate( "{} { {foo} }", desciptor, null );
		assertEquals( "Wrong substitution", expected, actual );
	}

	@Test
	public void testUnSuccessfulInterpolation() {
		ConstraintDescriptorImpl desciptor = new ConstraintDescriptorImpl(
				notNull, new Class<?>[] { }, new NotNullConstraint(), NotNullConstraint.class
		);
		String expected = "foo";  // missing {}
		String actual = resolver.interpolate( "foo", desciptor, null );
		assertEquals( "Wrong substitution", expected, actual );

		expected = "#{foo  {}";
		actual = resolver.interpolate( "#{foo  {}", desciptor, null );
		assertEquals( "Wrong substitution", expected, actual );
	}

	@Test
	public void testUnkownTokenInterpolation() {
		ConstraintDescriptorImpl desciptor = new ConstraintDescriptorImpl(
				notNull, new Class<?>[] { }, new NotNullConstraint(), NotNullConstraint.class
		);
		String expected = "{bar}";  // unkown token {}
		String actual = resolver.interpolate( "{bar}", desciptor, null );
		assertEquals( "Wrong substitution", expected, actual );
	}

	@Test
	public void testDefaultInterpolation() {
		ConstraintDescriptorImpl desciptor = new ConstraintDescriptorImpl(
				notNull, new Class<?>[] { }, new NotNullConstraint(), NotNullConstraint.class
		);
		String expected = "may not be null";
		String actual = resolver.interpolate( notNull.message(), desciptor, null );
		assertEquals( "Wrong substitution", expected, actual );

		desciptor = new ConstraintDescriptorImpl( length, new Class<?>[] { }, new NotNullConstraint(), NotNullConstraint.class );
		expected = "length must be between 0 and 2147483647";  // unkown token {}
		actual = resolver.interpolate( length.message(), desciptor, null );
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
