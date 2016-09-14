/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.constraintvalidators.hv;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNumberOfViolations;
import static org.hibernate.validator.testutils.ValidatorUtil.getValidator;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.internal.constraintvalidators.hv.empty.NotEmptyArrayValidator;
import org.hibernate.validator.internal.constraintvalidators.hv.empty.NotEmptyBooleanArrayValidator;
import org.hibernate.validator.internal.constraintvalidators.hv.empty.NotEmptyByteArrayValidator;
import org.hibernate.validator.internal.constraintvalidators.hv.empty.NotEmptyCharArrayValidator;
import org.hibernate.validator.internal.constraintvalidators.hv.empty.NotEmptyDoubleArrayValidator;
import org.hibernate.validator.internal.constraintvalidators.hv.empty.NotEmptyFloatArrayValidator;
import org.hibernate.validator.internal.constraintvalidators.hv.empty.NotEmptyIntArrayValidator;
import org.hibernate.validator.internal.constraintvalidators.hv.empty.NotEmptyLongArrayValidator;
import org.hibernate.validator.internal.constraintvalidators.hv.empty.NotEmptyShortArrayValidator;
import org.testng.annotations.Test;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.lang.reflect.Array;
import java.util.Set;


public class EmptyArrayValidatorTest {

	@Test
	public void testConstraintValidator() {
		NotEmptyArrayValidator constraintValidator = new NotEmptyArrayValidator();
		assertTrue( constraintValidator.isValid( new Object[]{""}, null ) );
		assertFalse( constraintValidator.isValid( new Object[]{}, null ) );
		assertFalse( constraintValidator.isValid( null, null ) );

		testPrimitiveValidators( new NotEmptyBooleanArrayValidator(), boolean[].class );
		testPrimitiveValidators( new NotEmptyByteArrayValidator(), byte[].class );
		testPrimitiveValidators( new NotEmptyCharArrayValidator(), char[].class );
		testPrimitiveValidators( new NotEmptyDoubleArrayValidator(), double[].class );
		testPrimitiveValidators( new NotEmptyFloatArrayValidator(), float[].class );
		testPrimitiveValidators( new NotEmptyIntArrayValidator(), int[].class );
		testPrimitiveValidators( new NotEmptyLongArrayValidator(), long[].class );
		testPrimitiveValidators( new NotEmptyShortArrayValidator(), short[].class );
	}

	@Test
	public void testNotEmpty() {
		validate( new Foo(), false );
		validatePrimitive( new FooPrimitive(), false );
	}

	@Test
	public void testNotEmptyCanBeNull() {
		validate( new Bar(), true );
		validatePrimitive( new BarPrimitive(), true );
	}

	private <T extends WithArray> void validate( T bean, boolean canBeNull ) {
		Validator validator = getValidator();

		Set<ConstraintViolation<T>> constraintViolations = validator.validate( bean );
		assertNumberOfViolations( constraintViolations, canBeNull ? 0 : 1 );

		bean.setArray( new java.lang.Object[]{} );
		constraintViolations = validator.validate( bean );
		assertNumberOfViolations( constraintViolations, 1 );


		bean.setArray( new java.lang.Object[]{"", ""} );
		constraintViolations = validator.validate( bean );
		assertNumberOfViolations( constraintViolations, 0 );

	}

	private void validatePrimitive( WithPrimitiveArray bean, boolean canBeNull ) {
		Validator validator = getValidator();

		Set<ConstraintViolation<WithPrimitiveArray>> constraintViolations = validator.validate( bean );
		assertNumberOfViolations( constraintViolations, canBeNull ? 0 : 8 );

		bean.setArraySizes( 0 );
		constraintViolations = validator.validate( bean );
		assertNumberOfViolations( constraintViolations, 8 );

		bean.setArraySizes( 1 );
		constraintViolations = validator.validate( bean );
		assertNumberOfViolations( constraintViolations, 0 );
	}

	@SuppressWarnings("unchecked")
	private <T> void testPrimitiveValidators( ConstraintValidator<NotEmpty, T> validator, Class<T> arrayType ) {
		assertFalse( validator.isValid( null, null ) );

		T array = (T) Array.newInstance( arrayType.getComponentType(), 0 );
		assertFalse( validator.isValid( array, null ) );

		array = (T) Array.newInstance( arrayType.getComponentType(), 1);
		assertTrue( validator.isValid( array, null ) );
	}

	interface WithArray {
		void setArray( java.lang.Object[] array );
	}

	interface WithPrimitiveArray {
		void setArraySizes( int size );
	}

	class Foo implements WithArray {
		@NotEmpty
		java.lang.Object[] objects;

		@Override
		public void setArray( java.lang.Object[] array ) {
			this.objects = array;
		}
	}

	class Bar implements WithArray {
		@NotEmpty(canBeNull = true)
		java.lang.Object[] objects;

		@Override
		public void setArray( java.lang.Object[] array ) {
			this.objects = array;
		}
	}

	class FooPrimitive implements WithPrimitiveArray {
		@NotEmpty
		boolean[] booleans;
		@NotEmpty
		byte[] bytes;
		@NotEmpty
		char[] chars;
		@NotEmpty
		double[] doubles;
		@NotEmpty
		float[] floats;
		@NotEmpty
		int[] ints;
		@NotEmpty
		long[] longs;
		@NotEmpty
		short[] shorts;

		@Override
		public void setArraySizes( int size ) {
			booleans = new boolean[size];
			bytes = new byte[size];
			chars = new char[size];
			doubles = new double[size];
			floats = new float[size];
			ints = new int[size];
			longs = new long[size];
			shorts = new short[size];
		}
	}

	class BarPrimitive implements WithPrimitiveArray {
		@NotEmpty(canBeNull = true)
		boolean[] booleans;
		@NotEmpty(canBeNull = true)
		byte[] bytes;
		@NotEmpty(canBeNull = true)
		char[] chars;
		@NotEmpty(canBeNull = true)
		double[] doubles;
		@NotEmpty(canBeNull = true)
		float[] floats;
		@NotEmpty(canBeNull = true)
		int[] ints;
		@NotEmpty(canBeNull = true)
		long[] longs;
		@NotEmpty(canBeNull = true)
		short[] shorts;

		@Override
		public void setArraySizes( int size ) {
			booleans = new boolean[size];
			bytes = new byte[size];
			chars = new char[size];
			doubles = new double[size];
			floats = new float[size];
			ints = new int[size];
			longs = new long[size];
			shorts = new short[size];
		}
	}
}
