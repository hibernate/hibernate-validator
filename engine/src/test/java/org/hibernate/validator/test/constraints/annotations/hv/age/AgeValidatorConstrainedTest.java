/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.constraints.annotations.hv.age;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Set;

import javax.validation.ConstraintViolation;

import org.hibernate.validator.constraints.AgeMin;
import org.hibernate.validator.test.constraints.annotations.AbstractConstrainedTest;
import org.hibernate.validator.testutil.TestForIssue;

import org.testng.annotations.Test;

/**
 * Test to make sure that elements annotated with {@link AgeMin} are validated.
 *
 * @author Hillmer Chona
 * @since 6.0.8
 */
@TestForIssue(jiraKey = "HV-1552")
public class AgeValidatorConstrainedTest extends AbstractConstrainedTest {

	private static final int MINIMUM_AGE = 18;

	@Test
	public void testMinAge() {
		LocalDate todayMinus18Years = LocalDate.now().minusYears( MINIMUM_AGE );
		Foo foo = new Foo( todayMinus18Years );
		Set<ConstraintViolation<Foo>> violations = validator.validate( foo );
		assertNoViolations( violations );
	}

	@Test
	public void testMinAgeInvalid() {
		LocalDate tomorrowMinus18Years = LocalDate.now().plusDays( 1 ).minusYears( MINIMUM_AGE );
		Foo foo = new Foo( tomorrowMinus18Years );
		Set<ConstraintViolation<Foo>> violations = validator.validate( foo );
		assertThat( violations ).containsOnlyViolations(
				violationOf( AgeMin.class ).withMessage( "must be older than " + MINIMUM_AGE )
		);
	}

	private static class Foo {

		@AgeMin(value = MINIMUM_AGE)
		private final LocalDate birthDate;

		public Foo(LocalDate birthDate) {
			this.birthDate = birthDate;
		}
	}
}
