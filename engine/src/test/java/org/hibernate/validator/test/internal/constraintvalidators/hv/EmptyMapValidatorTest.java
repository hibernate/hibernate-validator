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
import org.hibernate.validator.internal.constraintvalidators.hv.empty.NotEmptyMapValidator;
import org.testng.annotations.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class EmptyMapValidatorTest {

	@Test
	public void testConstraintValidator() {
		NotEmptyMapValidator constraintValidator = new NotEmptyMapValidator();
		assertFalse( constraintValidator.isValid( Collections.emptyMap(), null ) );
		assertTrue( constraintValidator.isValid( getNonEmptyMap(), null ) );
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

	private <T extends WithMap> void validate( T bean, boolean canBeNull ) {
		Validator validator = getValidator();

		Set<ConstraintViolation<T>> constraintViolations = validator.validate( bean );
		assertNumberOfViolations( constraintViolations, canBeNull ? 0 : 1 );

		bean.setMap( Collections.emptyMap() );
		constraintViolations = validator.validate( bean );
		assertNumberOfViolations( constraintViolations, 1 );

		bean.setMap( getNonEmptyMap() );
		constraintViolations = validator.validate( bean );
		assertNumberOfViolations( constraintViolations, 0 );

	}

	private Map getNonEmptyMap() {
		return new HashMap() {
			{
				put( "key", "val" );
			}
		};

	}

	interface WithMap {
		void setMap( Map map );
	}

	class Foo implements WithMap {
		@NotEmpty
		Map map;

		@Override
		public void setMap( Map map ) {
			this.map = map;
		}
	}

	class Bar implements WithMap {
		@NotEmpty(canBeNull = true)
		Map map;

		@Override
		public void setMap( Map map ) {
			this.map = map;
		}
	}
}
