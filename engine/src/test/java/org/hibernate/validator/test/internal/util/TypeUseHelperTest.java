/*
* JBoss, Home of Professional Open Source
* Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual contributors
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

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.lang.reflect.TypeVariable;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import org.hibernate.validator.internal.util.TypeUseHelper;

import static org.testng.Assert.assertEquals;

/**
 * Tests for {@link org.hibernate.validator.internal.util.TypeUseHelper}
 *
 * @author Khalid Alqinyah
 */
public class TypeUseHelperTest {

	@Test
	public void testActualTypeArgumentsForAnnotatedType() throws Exception {
		Field field = Bar.class.getDeclaredField( "map" );
		List<AnnotatedType> actualArguments = TypeUseHelper.getAnnotatedActualTypeArguments( field.getAnnotatedType() );
		assertEquals( actualArguments.get( 0 ).getType(), Integer.class, "Actual type argument does not match." );
		assertEquals( actualArguments.get( 1 ).getType(), String.class, "Actual type argument does not match." );
	}

	@Test
	public void testActualTypeArgumentsNotAnnotatedType() throws Exception {
		Field field = Bar.class.getDeclaredField( "number" );
		List<AnnotatedType> actualArguments = TypeUseHelper.getAnnotatedActualTypeArguments( field.getAnnotatedType() );
		assertEquals( actualArguments.size(), 0, "Unexpected actual type arguments" );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testActualTypeArgumentsNull() {
		TypeUseHelper.getAnnotatedActualTypeArguments( null );
	}

	@Test
	public void testFormalToActual() {
		Class<?> cls = Bar.class.getSuperclass();
		AnnotatedType annotatedType = Bar.class.getAnnotatedSuperclass();
		Map<TypeVariable<?>, AnnotatedType> map = TypeUseHelper.getFormalToActualMap( annotatedType, cls );

		TypeVariable<?> t = BarBase.class.getTypeParameters()[0];
		TypeVariable<?> v = BarBase.class.getTypeParameters()[1];

		assertEquals( map.get( t ).getType(), Integer.class, "Formal parameter to actual argument mapping does not match." );
		assertEquals( map.get( v ).getType(), String.class, "Formal parameter to actual argument mapping does not match." );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testFormalToActualNullClass() {
		AnnotatedType annotatedType = Bar.class.getAnnotatedSuperclass();
		TypeUseHelper.getFormalToActualMap( annotatedType, null );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testFormalToActualNullAnnotatedType() {
		Class<?> cls = Bar.class.getSuperclass();
		TypeUseHelper.getFormalToActualMap( null, cls );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testFormalToActualAllNull() {
		TypeUseHelper.getFormalToActualMap( null, null );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testFormalToActualMismatch() throws Exception {
		Class<?> cls = Bar.class.getSuperclass();
		AnnotatedType annotatedType = Bar.class.getDeclaredField( "map" ).getAnnotatedType();
		TypeUseHelper.getFormalToActualMap( annotatedType, cls );
	}

	class Bar extends BarBase<Integer, String> {
		Map<Integer, String> map;

		Integer number;
	}

	class BarBase<T, V> {

	}
}
