/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.constraints.annotations.hv.ru;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import java.util.Set;

import jakarta.validation.ConstraintViolation;

import org.hibernate.validator.constraints.ru.INN;
import org.hibernate.validator.test.constraints.annotations.AbstractConstrainedTest;

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
