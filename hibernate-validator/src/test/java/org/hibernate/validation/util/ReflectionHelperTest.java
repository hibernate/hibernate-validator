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
package org.hibernate.validation.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.ValidationException;
import javax.validation.groups.Default;
import javax.validation.constraints.NotNull;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;

import org.hibernate.validation.constraints.Pattern;
import org.hibernate.validation.constraints.Patterns;
import org.hibernate.validation.eg.Engine;
import org.hibernate.validation.eg.Order;
import org.hibernate.validation.eg.constraint.NoGroups;
import org.hibernate.validation.eg.constraint.NoMessage;
import org.hibernate.validation.eg.constraint.ValidProperty;
import org.hibernate.tck.annotations.SpecAssertion;

/**
 * Tests for the <code>ReflectionHelper</code>.
 *
 * @author Hardy Ferentschik
 */
public class ReflectionHelperTest {
	@Test
	public void testGetIndexedValueForMap() {
		Map<String, Object> map = new HashMap<String, Object>();
		Object testObject = new Object();
		String key = "key";
		map.put( key, testObject );

		Object value = ReflectionHelper.getIndexedValue( map, key );
		assertEquals( "We should be able to retrieve the indexed object", testObject, value );

		// try to get to the value by integer index
		value = ReflectionHelper.getIndexedValue( map, "0" );
		assertEquals( "We should be able to retrieve the indexed object", testObject, value );

		value = ReflectionHelper.getIndexedValue( map, "foo" );
		assertNull( "A non existent index should return the null value", value );

		value = ReflectionHelper.getIndexedValue( map, "2" );
		assertNull( "A non existent index should return the null value", value );
	}

	@Test
	public void testGetIndexedValueForList() {
		List<Object> list = new ArrayList<Object>();
		Object testObject = new Object();
		list.add( testObject );

		Object value = ReflectionHelper.getIndexedValue( list, "0" );
		assertEquals( "We should be able to retrieve the indexed object", testObject, value );

		value = ReflectionHelper.getIndexedValue( list, "2" );
		assertNull( "A non existent index should return the null value", value );
	}

	@Test
	public void testGetIndexedValueForNull() {
		Object value = ReflectionHelper.getIndexedValue( null, "0" );
		assertNull( value );
	}

	@Test
	public void testGetMessageParamter() {
		NotNull testAnnotation = new NotNull() {
			public String message() {
				return "test";
			}

			public Class<?>[] groups() {
				return new Class<?>[] { Default.class };
			}

			public Class<? extends Annotation> annotationType() {
				return this.getClass();
			}
		};
		String message = ReflectionHelper.getAnnotationParameter( testAnnotation, "message", String.class );
		assertEquals( "Wrong message", "test", message );

		Class<?>[] group = ReflectionHelper.getAnnotationParameter( testAnnotation, "groups", Class[].class );
		assertEquals( "Wrong message", Default.class, group[0] );

		try {
			ReflectionHelper.getAnnotationParameter( testAnnotation, "message", Integer.class );
			fail();
		}
		catch ( ValidationException e ) {
			assertTrue( "Wrong exception message", e.getMessage().startsWith( "Wrong parameter type." ) );
		}

		try {
			ReflectionHelper.getAnnotationParameter( testAnnotation, "foo", Integer.class );
			fail();
		}
		catch ( ValidationException e ) {
			assertTrue(
					"Wrong exception message",
					e.getMessage().startsWith( "The specified annotation defines no parameter" )
			);
		}
	}

	@Test
	@SpecAssertion(section = "2.1.1.2", note = "constraint annotation must specify a groups element")
	public void testConstraintWithNoMessage() {
		Annotation annotation = new NoGroups() {
			public String message() {
				return "";
			}

			public Class<? extends Annotation> annotationType() {
				return this.getClass();
			}
		};
		assertFalse(
				"The constraint annotation should not be valid", ReflectionHelper.isConstraintAnnotation( annotation )
		);
	}

	@Test
	@SpecAssertion(section = "2.1.1.1", note = "constraint annotation must specify a groups element")
	public void testConstraintWithNoGroups() {
		Annotation annotation = new NoMessage() {
			public Class<?>[] groups() {
				return null;
			}

			public Class<? extends Annotation> annotationType() {
				return this.getClass();
			}
		};
		assertFalse(
				"The constraint annotation should not be valid", ReflectionHelper.isConstraintAnnotation( annotation )
		);
	}

	@Test
	@SpecAssertion(section = "2.1.1", note = "properties cannot begin with 'valid'")
	public void testConstraintWithValidInPropertyName() {
		Annotation annotation = new ValidProperty() {
			public String message() {
				return null;
			}

			public Class<?>[] groups() {
				return null;
			}

			public int validLength() {
				return 0;
			}

			public Class<? extends Annotation> annotationType() {
				return this.getClass();
			}
		};
		assertFalse(
				"The constraint annotation should not be valid", ReflectionHelper.isConstraintAnnotation( annotation )
		);
	}

	@Test
	public void testGetMultiValueConstraints() throws Exception {
		Engine engine = new Engine();
		Field[] fields = engine.getClass().getDeclaredFields();
		assertNotNull( fields );
		assertTrue( fields.length == 1 );
		ReflectionHelper.setAccessibility( fields[0] );

		Annotation annotation = fields[0].getAnnotation( Patterns.class );
		assertNotNull( annotation );
		List<Annotation> multiValueConstraintAnnotations = ReflectionHelper.getMultiValueConstraints( annotation );
		assertTrue( "There should be two constraint annotations", multiValueConstraintAnnotations.size() == 2 );
		assertTrue( "Wrong constraint annotation", multiValueConstraintAnnotations.get( 0 ) instanceof Pattern );
		assertTrue( "Wrong constraint annotation", multiValueConstraintAnnotations.get( 1 ) instanceof Pattern );


		Order order = new Order();
		fields = order.getClass().getDeclaredFields();
		assertNotNull( fields );
		assertTrue( fields.length == 1 );
		ReflectionHelper.setAccessibility( fields[0] );

		annotation = fields[0].getAnnotation( NotNull.class );
		assertNotNull( annotation );
		multiValueConstraintAnnotations = ReflectionHelper.getMultiValueConstraints( annotation );
		assertTrue( "There should be no constraint annotations", multiValueConstraintAnnotations.size() == 0 );
	}
}
