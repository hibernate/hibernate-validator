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
package org.hibernate.validator.test.constraints.impl;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.validation.ConstraintValidator;
import javax.validation.constraints.Size;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;
import org.testng.annotations.Test;

import org.hibernate.validator.constraints.impl.SizeValidatorForArray;
import org.hibernate.validator.constraints.impl.SizeValidatorForArraysOfBoolean;
import org.hibernate.validator.constraints.impl.SizeValidatorForArraysOfByte;
import org.hibernate.validator.constraints.impl.SizeValidatorForArraysOfChar;
import org.hibernate.validator.constraints.impl.SizeValidatorForArraysOfDouble;
import org.hibernate.validator.constraints.impl.SizeValidatorForArraysOfFloat;
import org.hibernate.validator.constraints.impl.SizeValidatorForArraysOfInt;
import org.hibernate.validator.constraints.impl.SizeValidatorForArraysOfLong;
import org.hibernate.validator.constraints.impl.SizeValidatorForArraysOfShort;
import org.hibernate.validator.constraints.impl.SizeValidatorForCollection;
import org.hibernate.validator.constraints.impl.SizeValidatorForMap;
import org.hibernate.validator.constraints.impl.SizeValidatorForString;
import org.hibernate.validator.util.annotationfactory.AnnotationDescriptor;
import org.hibernate.validator.util.annotationfactory.AnnotationFactory;

/**
 * @author Alaa Nassef
 */
public class SizeValidatorTest {


	@Test
	public void testIsValidObjectArray() throws Exception {
		ConstraintValidator<Size, Object[]> validator = getValidator( SizeValidatorForArray.class );
		assertSizes( validator, Object[].class );
	}

	@Test
	public void testIsValidBooleanArray() throws Exception {
		ConstraintValidator<Size, boolean[]> validator = getValidator( SizeValidatorForArraysOfBoolean.class );
		assertSizes( validator, boolean[].class );
	}

	@Test
	public void testIsValidByteArray() throws Exception {
		ConstraintValidator<Size, byte[]> validator = getValidator( SizeValidatorForArraysOfByte.class );
		assertSizes( validator, byte[].class );
	}

	@Test
	public void testIsValidCharArray() throws Exception {
		ConstraintValidator<Size, char[]> validator = getValidator( SizeValidatorForArraysOfChar.class );
		assertSizes( validator, char[].class );
	}

	@Test
	public void testIsValidDoubleArray() throws Exception {
		ConstraintValidator<Size, double[]> validator = getValidator( SizeValidatorForArraysOfDouble.class );
		assertSizes( validator, double[].class );
	}

	@Test
	public void testIsValidFloatArray() throws Exception {
		ConstraintValidator<Size, float[]> validator = getValidator( SizeValidatorForArraysOfFloat.class );
		assertSizes( validator, float[].class );
	}

	@Test
	public void testIsValidIntArray() throws Exception {
		ConstraintValidator<Size, int[]> validator = getValidator( SizeValidatorForArraysOfInt.class );
		assertSizes( validator, int[].class );
	}

	@Test
	public void testIsValidLongArray() throws Exception {
		ConstraintValidator<Size, long[]> validator = getValidator( SizeValidatorForArraysOfLong.class );
		assertSizes( validator, long[].class );
	}

	@Test
	public void testIsValidShortArray() throws Exception {
		ConstraintValidator<Size, short[]> validator = getValidator( SizeValidatorForArraysOfShort.class );
		assertSizes( validator, short[].class );
	}

	@Test
	public void testIsValidCollection() throws Exception {
		ConstraintValidator<Size, Collection> validator = getValidator( SizeValidatorForCollection.class );

		assertTrue( validator.isValid( null, null ) );

		Collection<String> collection = new ArrayList<String>();
		assertFalse( validator.isValid( collection, null ) );

		collection.add( "firstItem" );
		assertTrue( validator.isValid( collection, null ) );

		collection.add( "secondItem" );
		assertTrue( validator.isValid( collection, null ) );

		collection.add( "thirdItem" );
		assertFalse( validator.isValid( collection, null ) );
	}

	@Test
	public void testIsValidMap() throws Exception {
		ConstraintValidator<Size, Map> validator = getValidator( SizeValidatorForMap.class );

		assertTrue( validator.isValid( null, null ) );

		Map<String, String> map = new HashMap<String, String>();
		assertFalse( validator.isValid( map, null ) );

		map.put( "key1", "firstItem" );
		assertTrue( validator.isValid( map, null ) );

		map.put( "key3", "secondItem" );
		assertTrue( validator.isValid( map, null ) );

		map.put( "key2", "thirdItem" );
		assertFalse( validator.isValid( map, null ) );
	}

	@Test
	public void testIsValiString() throws Exception {
		ConstraintValidator<Size, String> validator = getValidator( SizeValidatorForString.class );

		assertTrue( validator.isValid( null, null ) );
		assertFalse( validator.isValid( "", null ) );
		assertTrue( validator.isValid( "a", null ) );
		assertTrue( validator.isValid( "ab", null ) );
		assertFalse( validator.isValid( "abc", null ) );
	}

	private <T> ConstraintValidator<Size, T> getValidator(Class<?> validatorClass) throws Exception {
		AnnotationDescriptor<Size> descriptor = new AnnotationDescriptor<Size>( Size.class );
		descriptor.setValue( "min", 1 );
		descriptor.setValue( "max", 2 );
		descriptor.setValue( "message", "{validator.max}" );
		Size m = AnnotationFactory.create( descriptor );
		@SuppressWarnings("unchecked")
		ConstraintValidator<Size, T> validator = ( ConstraintValidator<Size, T> ) validatorClass.newInstance();
		validator.initialize( m );
		return validator;
	}

	@SuppressWarnings("unchecked")
	private <T> void assertSizes(ConstraintValidator<Size, T> validator, Class<T> arrayType) {
		assertTrue( validator.isValid( null, null ) );

		T array = ( T ) Array.newInstance( arrayType.getComponentType(), 0 );
		assertFalse( validator.isValid( array, null ) );

		array = ( T ) Array.newInstance( arrayType.getComponentType(), 1 );
		assertTrue( validator.isValid( array, null ) );

		array = ( T ) Array.newInstance( arrayType.getComponentType(), 2 );
		assertTrue( validator.isValid( array, null ) );

		array = ( T ) Array.newInstance( arrayType.getComponentType(), 3 );
		assertFalse( validator.isValid( array, null ) );
	}
}
