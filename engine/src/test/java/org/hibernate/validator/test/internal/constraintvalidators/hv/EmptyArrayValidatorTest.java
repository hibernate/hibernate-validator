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
import org.testng.annotations.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;


public class EmptyArrayValidatorTest {

	@Test
	public void testConstraintValidator() {
		NotEmptyArrayValidator constraintValidator = new NotEmptyArrayValidator();
		assertFalse( constraintValidator.isValid( new int[]{}, null ) );
		assertTrue( constraintValidator.isValid( new Object[]{""}, null ) );
		assertTrue( constraintValidator.isValid( new int[]{1}, null ) );
		assertFalse( constraintValidator.isValid( null, null ) );
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

	private <T extends WithPrimitiveArray> void validatePrimitive( T bean, boolean canBeNull ) {
		Validator validator = getValidator();

		Set<ConstraintViolation<T>> constraintViolations = validator.validate( bean );
		assertNumberOfViolations( constraintViolations, canBeNull ? 0 : 1 );

		bean.setArray( new int[]{} );
		constraintViolations = validator.validate( bean );
		assertNumberOfViolations( constraintViolations, 1 );


		bean.setArray( new int[]{1, 2} );
		constraintViolations = validator.validate( bean );
		assertNumberOfViolations( constraintViolations, 0 );

	}

	interface WithArray {
		void setArray( java.lang.Object[] array );
	}

	interface WithPrimitiveArray {
		void setArray( int[] array );
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
		int[] ints;

		@Override
		public void setArray( int[] array ) {
			this.ints = array;
		}
	}

	class BarPrimitive implements WithPrimitiveArray {
		@NotEmpty(canBeNull = true)
		int[] ints;

		@Override
		public void setArray( int[] array ) {
			this.ints = array;
		}
	}
}
