/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.constraintvalidators.hv;

import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.internal.constraintvalidators.hv.empty.NotEmptyCharSequenceValidator;
import org.testng.annotations.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNumberOfViolations;
import static org.hibernate.validator.testutils.ValidatorUtil.getValidator;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;


public class EmptyCharSequenceValidatorTest {

	@Test
	public void testConstraintValidator() {
		NotEmptyCharSequenceValidator constraintValidator = new NotEmptyCharSequenceValidator();
		assertTrue( constraintValidator.isValid( "a", null ) );
		assertTrue( constraintValidator.isValid( " ", null ) );
		assertTrue( constraintValidator.isValid( "\t", null ) );
		assertTrue( constraintValidator.isValid( "\n", null ) );
		assertFalse( constraintValidator.isValid( null, null ) );
		assertFalse( constraintValidator.isValid( "", null ) );

	}

	@Test
	public void testNotEmpty() {
		validate( new Foo(), false );
	}

	@Test
	public void testNotEmptyCanBeNull() {
		validate( new Bar(), true );
	}

	private <T extends WithName> void validate( T bean, boolean canBeNull ) {
		Validator validator = getValidator();

		Set<ConstraintViolation<T>> constraintViolations = validator.validate( bean );
		assertNumberOfViolations( constraintViolations, canBeNull ? 0 : 1 );

		bean.setName( "" );
		constraintViolations = validator.validate( bean );
		assertNumberOfViolations( constraintViolations, 1 );

		bean.setName( " " );
		constraintViolations = validator.validate( bean );
		assertNumberOfViolations( constraintViolations, 0 );

		bean.setName( "\t" );
		constraintViolations = validator.validate( bean );
		assertNumberOfViolations( constraintViolations, 0 );

		bean.setName( "\n" );
		constraintViolations = validator.validate( bean );
		assertNumberOfViolations( constraintViolations, 0 );

		bean.setName( "john doe" );
		constraintViolations = validator.validate( bean );
		assertNumberOfViolations( constraintViolations, 0 );
	}

	interface WithName {
		void setName( String name );
	}

	class Foo implements WithName {
		@NotEmpty
		String name;

		@Override
		public void setName( String name ) {
			this.name = name;
		}
	}

	class Bar implements WithName {
		@NotEmpty(canBeNull = true)
		String name;

		@Override
		public void setName( String name ) {
			this.name = name;
		}
	}
}
