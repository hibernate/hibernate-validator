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
package org.hibernate.validator.test.internal.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeSet;
import javax.validation.Payload;
import javax.validation.ValidationException;
import javax.validation.constraints.NotNull;
import javax.validation.groups.Default;

import org.testng.annotations.Test;

import org.hibernate.validator.internal.util.ReflectionHelper;
import org.hibernate.validator.testutil.TestForIssue;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * Tests for the {@code ReflectionHelper}.
 *
 * @author Hardy Ferentschik
 */
public class ReflectionHelperTest {

	@Test
	public void testIsIterable() throws Exception {
		Type type = TestTypes.class.getField( "stringList" ).getGenericType();
		assertTrue( ReflectionHelper.isIterable( type ) );

		assertTrue( ReflectionHelper.isIterable( TreeSet.class ) );

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

		Object value = ReflectionHelper.getMappedValue( map, key );
		assertEquals( value, testObject, "We should be able to retrieve the indexed object" );

		value = ReflectionHelper.getMappedValue( map, "foo" );
		assertNull( value, "A non existent index should return the null value" );

		value = ReflectionHelper.getMappedValue( map, "2" );
		assertNull( value, "A non existent index should return the null value" );
	}

	@Test
	public void testGetIndexedValueForList() {
		List<Object> list = new ArrayList<Object>();
		Object testObject = new Object();
		list.add( testObject );

		Object value = ReflectionHelper.getIndexedValue( list, 0 );
		assertEquals( value, testObject, "We should be able to retrieve the indexed object" );

		value = ReflectionHelper.getIndexedValue( list, 2 );
		assertNull( value, "A non existent index should return the null value" );
	}

	@Test
	public void testGetIndexedValueForNull() {
		Object value = ReflectionHelper.getIndexedValue( null, 0 );
		assertNull( value );
	}

	@Test
	public void testGetMessageParameter() {
		NotNull testAnnotation = new NotNull() {
			@Override
			public String message() {
				return "test";
			}

			@Override
			public Class<?>[] groups() {
				return new Class<?>[] { Default.class };
			}

			@Override
			public Class<? extends Payload>[] payload() {
				@SuppressWarnings("unchecked")
				Class<? extends Payload>[] classes = new Class[] { };
				return classes;
			}

			@Override
			public Class<? extends Annotation> annotationType() {
				return this.getClass();
			}
		};
		String message = ReflectionHelper.getAnnotationParameter( testAnnotation, "message", String.class );
		assertEquals( "test", message, "Wrong message" );

		Class<?>[] group = ReflectionHelper.getAnnotationParameter( testAnnotation, "groups", Class[].class );
		assertEquals( group[0], Default.class, "Wrong message" );

		try {
			ReflectionHelper.getAnnotationParameter( testAnnotation, "message", Integer.class );
			fail();
		}
		catch ( ValidationException e ) {
			assertTrue( e.getMessage().contains( "Wrong parameter type." ), "Wrong exception message" );
		}

		try {
			ReflectionHelper.getAnnotationParameter( testAnnotation, "foo", Integer.class );
			fail();
		}
		catch ( ValidationException e ) {
			assertTrue(
					e.getMessage().contains( "The specified annotation defines no parameter" ),
					"Wrong exception message"
			);
		}
	}

	@Test
	@TestForIssue(jiraKey = "HV-622")
	public void testIsGetterMethod() throws Exception {
		Method method = Bar.class.getMethod( "getBar" );
		assertTrue( ReflectionHelper.isGetterMethod( method ) );

		method = Bar.class.getMethod( "getBar", String.class );
		assertFalse( ReflectionHelper.isGetterMethod( method ) );
	}

	@Test
	@TestForIssue(jiraKey = "HV-818")
	public void testOverrides() throws Exception {
		Method getBar = Bar.class.getMethod("getBar");
		Method getBarString = Bar.class.getMethod("getBar",String.class);
		
		Method getSuperBar = Foo.class.getMethod("getBar");
		Method getSuperBarString = Foo.class.getMethod("getBar",String.class);
		Method getBarInteger = Foo.class.getMethod("getBar",Integer.class);
		
		Method getFooLong = Foo.class.getMethod("getFoo",Long.class);
		Method getStaticFoo = Foo.class.getMethod("getFoo");
		Method getStaticFooString = Foo.class.getMethod("getFoo",String.class);
		Method getStaticFooInteger = Foo.class.getMethod("getFoo",Integer.class);
        
		assertTrue(ReflectionHelper.overrides(getSuperBar,getBar));
		assertTrue(ReflectionHelper.overrides(getSuperBarString,getBarString));
		
        assertFalse(ReflectionHelper.overrides(getBar,getBarString));
		assertFalse(ReflectionHelper.overrides(getBar,getBarInteger));
		assertFalse(ReflectionHelper.overrides(getBarString,getBarInteger));
		assertFalse(ReflectionHelper.overrides(getSuperBar,getBarInteger));
		assertFalse(ReflectionHelper.overrides(getSuperBar,getBarString));
		assertFalse(ReflectionHelper.overrides(getSuperBarString,getBarInteger));
		assertFalse(ReflectionHelper.overrides(getSuperBarString,getBar));
		assertFalse(ReflectionHelper.overrides(getSuperBarString,getSuperBar));
		
		assertFalse(ReflectionHelper.overrides(getStaticFoo,getStaticFooString));
		assertFalse(ReflectionHelper.overrides(getStaticFoo,getStaticFooInteger));
		assertFalse(ReflectionHelper.overrides(getStaticFooString,getStaticFooInteger));
		assertFalse(ReflectionHelper.overrides(getFooLong,getStaticFoo));
		assertFalse(ReflectionHelper.overrides(getFooLong,getStaticFooInteger));
		assertFalse(ReflectionHelper.overrides(getFooLong,getStaticFooString));
	}
	
	@SuppressWarnings("unused")
	private static class TestTypes {
		public List<String> stringList;
		public Map<String, Object> objectMap;
		public String[] stringArray;
	}

	@SuppressWarnings("unused")
	private static class Bar {
		public String getBar() {
			return null;
		}

		public String getBar(String param) {
			return null;
		}
	}

	@SuppressWarnings("unused")
	private static class Foo extends Bar {
		public String getBar() {
			return null;
		}
		
		public String getBar(String param) {
			return null;
		}
		
		public String getBar(Integer param) {
			return null;
		}
		
		public String getFoo(Long param) {
			return null;
		}

		public static String getFoo() {
			return null;
		}
		
		public static String getFoo(Integer param) {
			return null;
		}

		public static String getFoo(String param) {
			return null;
		}
	}
}
