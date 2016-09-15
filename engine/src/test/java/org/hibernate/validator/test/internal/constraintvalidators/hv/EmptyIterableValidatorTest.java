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
import org.hibernate.validator.internal.constraintvalidators.hv.empty.NotEmptyIterableValidator;
import org.testng.annotations.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;


public class EmptyIterableValidatorTest {

	@Test
	public void testConstraintValidator() {
		NotEmptyIterableValidator constraintValidator = new NotEmptyIterableValidator();
		assertTrue( constraintValidator.isValid( Arrays.asList("some text"), null ) );
		assertFalse( constraintValidator.isValid( Collections.emptyList(), null ) );
		assertFalse( constraintValidator.isValid( null, null ) );
	}

	@Test
	public void testNotEmpty() {
		validate( new Foo(), false );
	}

	@Test
	public void testNotEmptyCanBeNull() {
		validate( new Bar(), true );
	}

	private <T extends WithIterable> void validate( T bean, boolean canBeNull ) {
		Validator validator = getValidator();

		Set<ConstraintViolation<T>> constraintViolations = validator.validate( bean );
		assertNumberOfViolations( constraintViolations, canBeNull ? 0 : 1 );

		bean.setIterable( Collections.emptyList() );
		constraintViolations = validator.validate( bean );
		assertNumberOfViolations( constraintViolations, 1 );

		bean.setIterable( Arrays.asList( "something" ) );
		constraintViolations = validator.validate( bean );
		assertNumberOfViolations( constraintViolations, 0 );

	}

	interface WithIterable {
		void setIterable( Iterable list );
	}

	class Foo implements WithIterable {
		@NotEmpty
		Iterable list;

		@Override
		public void setIterable( Iterable list ) {
			this.list = list;
		}
	}

	class Bar implements WithIterable {
		@NotEmpty(canBeNull = true)
		Iterable list;

		@Override
		public void setIterable( Iterable list ) {
			this.list = list;
		}
	}
}
