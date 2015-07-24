/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.constraintvalidators.bv.size;

import org.hibernate.validator.internal.constraintvalidators.bv.size.SizeValidatorForArray;
import org.hibernate.validator.internal.constraintvalidators.bv.size.SizeValidatorForArraysOfBoolean;
import org.hibernate.validator.internal.constraintvalidators.bv.size.SizeValidatorForArraysOfByte;
import org.hibernate.validator.internal.constraintvalidators.bv.size.SizeValidatorForArraysOfChar;
import org.hibernate.validator.internal.constraintvalidators.bv.size.SizeValidatorForArraysOfDouble;
import org.hibernate.validator.internal.constraintvalidators.bv.size.SizeValidatorForArraysOfFloat;
import org.hibernate.validator.internal.constraintvalidators.bv.size.SizeValidatorForArraysOfInt;
import org.hibernate.validator.internal.constraintvalidators.bv.size.SizeValidatorForArraysOfLong;
import org.hibernate.validator.internal.constraintvalidators.bv.size.SizeValidatorForArraysOfShort;
import org.hibernate.validator.internal.constraintvalidators.bv.size.SizeValidatorForCharSequence;
import org.hibernate.validator.internal.constraintvalidators.bv.size.SizeValidatorForCollection;
import org.hibernate.validator.internal.constraintvalidators.bv.size.SizeValidatorForMap;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationDescriptor;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationFactory;
import org.hibernate.validator.testutil.ConstraintViolationAssert;
import org.hibernate.validator.testutil.MyCustomStringImpl;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutil.ValidatorUtil;
import org.testng.annotations.Test;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.constraints.Size;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author Alaa Nassef
 * @author Hardy Ferentschik
 */
public class SizeValidatorTest {

	@Test
	public void testIsValidObjectArray() throws Exception {
		ConstraintValidator<Size, Object[]> validator = getValidatorMin1Max2( SizeValidatorForArray.class );
		assertSizes( validator, Object[].class );
	}

	@Test
	public void testIsValidBooleanArray() throws Exception {
		ConstraintValidator<Size, boolean[]> validator = getValidatorMin1Max2( SizeValidatorForArraysOfBoolean.class );
		assertSizes( validator, boolean[].class );
	}

	@Test
	public void testIsValidByteArray() throws Exception {
		ConstraintValidator<Size, byte[]> validator = getValidatorMin1Max2( SizeValidatorForArraysOfByte.class );
		assertSizes( validator, byte[].class );
	}

	@Test
	public void testIsValidCharArray() throws Exception {
		ConstraintValidator<Size, char[]> validator = getValidatorMin1Max2( SizeValidatorForArraysOfChar.class );
		assertSizes( validator, char[].class );
	}

	@Test
	public void testIsValidDoubleArray() throws Exception {
		ConstraintValidator<Size, double[]> validator = getValidatorMin1Max2( SizeValidatorForArraysOfDouble.class );
		assertSizes( validator, double[].class );
	}

	@Test
	public void testIsValidFloatArray() throws Exception {
		ConstraintValidator<Size, float[]> validator = getValidatorMin1Max2( SizeValidatorForArraysOfFloat.class );
		assertSizes( validator, float[].class );
	}

	@Test
	public void testIsValidIntArray() throws Exception {
		ConstraintValidator<Size, int[]> validator = getValidatorMin1Max2( SizeValidatorForArraysOfInt.class );
		assertSizes( validator, int[].class );
	}

	@Test
	public void testIsValidLongArray() throws Exception {
		ConstraintValidator<Size, long[]> validator = getValidatorMin1Max2( SizeValidatorForArraysOfLong.class );
		assertSizes( validator, long[].class );
	}

	@Test
	public void testIsValidShortArray() throws Exception {
		ConstraintValidator<Size, short[]> validator = getValidatorMin1Max2( SizeValidatorForArraysOfShort.class );
		assertSizes( validator, short[].class );
	}

	@Test
	public void testIsValidCollection() throws Exception {
		ConstraintValidator<Size, Collection> validator = getValidatorMin1Max2( SizeValidatorForCollection.class );

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
		ConstraintValidator<Size, Map> validator = getValidatorMin1Max2( SizeValidatorForMap.class );

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
	public void testIsValidString() throws Exception {
		ConstraintValidator<Size, String> validator = getValidatorMin1Max2( SizeValidatorForCharSequence.class );

		assertTrue( validator.isValid( null, null ) );
		assertFalse( validator.isValid( "", null ) );
		assertTrue( validator.isValid( "a", null ) );
		assertTrue( validator.isValid( "ab", null ) );
		assertFalse( validator.isValid( "abc", null ) );
	}

	@Test
	@TestForIssue(jiraKey = "HV-502")
	public void testIsValidCharSequence() throws Exception {
		ConstraintValidator<Size, CharSequence> validator = getValidatorMin1Max2( SizeValidatorForCharSequence.class );

		assertTrue( validator.isValid( new MyCustomStringImpl( "ab" ), null ) );
		assertFalse( validator.isValid( new MyCustomStringImpl( "abc" ), null ) );
	}

	@Test
	@TestForIssue(jiraKey = "HV-551")
	public void testGenericsExtendsFoo() throws Exception {
		Validator validator = ValidatorUtil.getValidator();
		Set<ConstraintViolation<Foo>> violations = validator.validate( new Foo() );
		ConstraintViolationAssert.assertCorrectConstraintViolationMessages( violations, "there can only be one" );
	}

	private <T> ConstraintValidator<Size, T> getValidatorMin1Max2(Class<?> validatorClass) throws Exception {
		AnnotationDescriptor<Size> descriptor = new AnnotationDescriptor<Size>( Size.class );
		descriptor.setValue( "min", 1 );
		descriptor.setValue( "max", 2 );
		descriptor.setValue( "message", "{validator.max}" );
		Size m = AnnotationFactory.create( descriptor );
		@SuppressWarnings("unchecked")
		ConstraintValidator<Size, T> validator = (ConstraintValidator<Size, T>) validatorClass.newInstance();
		validator.initialize( m );
		return validator;
	}

	@SuppressWarnings("unchecked")
	private <T> void assertSizes(ConstraintValidator<Size, T> validator, Class<T> arrayType) {
		assertTrue( validator.isValid( null, null ) );

		T array = (T) Array.newInstance( arrayType.getComponentType(), 0 );
		assertFalse( validator.isValid( array, null ) );

		array = (T) Array.newInstance( arrayType.getComponentType(), 1 );
		assertTrue( validator.isValid( array, null ) );

		array = (T) Array.newInstance( arrayType.getComponentType(), 2 );
		assertTrue( validator.isValid( array, null ) );

		array = (T) Array.newInstance( arrayType.getComponentType(), 3 );
		assertFalse( validator.isValid( array, null ) );
	}

	private class Foo {
		@Size(min = 1, max = 1, message = "there can only be one")
		private List<? extends Foo> listOfSubClasses = new ArrayList<Foo>();
	}
}
