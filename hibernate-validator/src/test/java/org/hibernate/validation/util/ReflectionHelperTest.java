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
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeSet;
import javax.validation.ValidationException;
import javax.validation.constraints.NotNull;
import javax.validation.groups.Default;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import org.testng.annotations.Test;

/**
 * Tests for the <code>ReflectionHelper</code>.
 *
 * @author Hardy Ferentschik
 */
public class ReflectionHelperTest {

	@Test
	public void testIsIterable() throws Exception {
		Type type = TestTypes.class.getField( "stringList" ).getGenericType();
		assertTrue( ReflectionHelper.isIterable( type ) );

		assertTrue( ReflectionHelper.isIterable( new TreeSet<Object>().getClass() ) );

		assertTrue( ReflectionHelper.isIterable( List.class ) );
		assertTrue( ReflectionHelper.isIterable( HashSet.class ) );
		assertTrue( ReflectionHelper.isIterable( Iterable.class ) );
		assertTrue( ReflectionHelper.isIterable( Collection.class ) );

		assertFalse( ReflectionHelper.isIterable( null ) );
		assertFalse( ReflectionHelper.isIterable( Object.class ) );
	}

	@Test
	public void testIsMap() throws Exception {
		assertTrue( ReflectionHelper.isMap( Map.class ) );
		assertTrue( ReflectionHelper.isMap( SortedMap.class ) );

		Type type = TestTypes.class.getField( "objectMap" ).getGenericType();
		assertTrue( ReflectionHelper.isMap( type ) );

		assertFalse( ReflectionHelper.isMap( null ) );
		assertFalse( ReflectionHelper.isMap( Object.class ) );
	}

	@Test
	public void testGetIndexedType() throws Exception {
		Type type = TestTypes.class.getField( "stringList" ).getGenericType();
		assertEquals( String.class, ReflectionHelper.getIndexedType( type ) );

		type = TestTypes.class.getField( "objectMap" ).getGenericType();
		assertEquals( Object.class, ReflectionHelper.getIndexedType( type ) );

		type = TestTypes.class.getField( "stringArray" ).getGenericType();
		assertEquals( String.class, ReflectionHelper.getIndexedType( type ) );
	}

	@Test
	public void testGetIndexedValueForMap() {
		Map<String, Object> map = new HashMap<String, Object>();
		Object testObject = new Object();
		String key = "key";
		map.put( key, testObject );

		Object value = ReflectionHelper.getIndexedValue( map, key );
		assertEquals( testObject, value, "We should be able to retrieve the indexed object" );

		// try to get to the value by integer index
		value = ReflectionHelper.getIndexedValue( map, "0" );
		assertEquals( testObject, value, "We should be able to retrieve the indexed object" );

		value = ReflectionHelper.getIndexedValue( map, "foo" );
		assertNull( value, "A non existent index should return the null value" );

		value = ReflectionHelper.getIndexedValue( map, "2" );
		assertNull( value, "A non existent index should return the null value" );
	}

	@Test
	public void testGetIndexedValueForList() {
		List<Object> list = new ArrayList<Object>();
		Object testObject = new Object();
		list.add( testObject );

		Object value = ReflectionHelper.getIndexedValue( list, "0" );
		assertEquals( testObject, value, "We should be able to retrieve the indexed object" );

		value = ReflectionHelper.getIndexedValue( list, "2" );
		assertNull( value, "A non existent index should return the null value" );
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
		assertEquals( "test", message, "Wrong message" );

		Class<?>[] group = ReflectionHelper.getAnnotationParameter( testAnnotation, "groups", Class[].class );
		assertEquals( Default.class, group[0], "Wrong message" );

		try {
			ReflectionHelper.getAnnotationParameter( testAnnotation, "message", Integer.class );
			fail();
		}
		catch ( ValidationException e ) {
			assertTrue( e.getMessage().startsWith( "Wrong parameter type." ), "Wrong exception message" );
		}

		try {
			ReflectionHelper.getAnnotationParameter( testAnnotation, "foo", Integer.class );
			fail();
		}
		catch ( ValidationException e ) {
			assertTrue(
					e.getMessage().startsWith( "The specified annotation defines no parameter" ),
					"Wrong exception message"
			);
		}
	}

	public class TestTypes {
		public List<String> stringList;
		public Map<String, Object> objectMap;
		public String[] stringArray;
	}
}
