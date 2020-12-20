/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.constraints.annotations.hv.ru;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import java.util.Set;

import org.hibernate.validator.constraints.ru.INN;
import org.hibernate.validator.test.constraints.annotations.AbstractConstrainedTest;

import jakarta.validation.ConstraintViolation;
import org.testng.annotations.Test;

/**
 * Test to make sure that elements annotated with {@link INN} are validated.
 *
 * @author Artem Boiarshinov
 */
public class INNValidatorTest extends AbstractConstrainedTest {

	@Test
	public void testINN() {
		final Person person = new Person( "245885856020" );
		final Set<ConstraintViolation<Person>> violations = validator.validate( person );
		assertNoViolations( violations );
	}

	@Test
	public void testINNInvalid() {
		final Person person = new Person( "0123456789" );
		final Set<ConstraintViolation<Person>> violations = validator.validate( person );
		assertThat( violations ).containsOnlyViolations(
				violationOf( INN.class ).withMessage( "invalid Russian taxpayer identification number (INN)" )
		);
	}

	private static class Person {

		@INN
		private final String inn;

		public Person(String inn) {
			this.inn = inn;
		}
	}
}
