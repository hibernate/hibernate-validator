/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.constraintvalidators.bv.time;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.hibernate.validator.testutils.ValidatorUtil.getConfiguration;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.Future;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.Past;
import javax.validation.constraints.PastOrPresent;

import org.hibernate.validator.testutil.TestForIssue;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Guillaume Smet
 */
@TestForIssue(jiraKey = "HV-1878")
public class JavaSqlDateTest {

	private static final ZoneId TZ_BERLIN = ZoneId.of( "Europe/Berlin" );

	private Validator validator;

	@BeforeMethod
	public void setupValidator() {
		FixedClockProvider clockProvider = new FixedClockProvider(
				ZonedDateTime.of(
						2000, 2, 15, 4, 0, 0, 0,
						TZ_BERLIN ) );
		ValidatorFactory validatorFactory = getConfiguration()
				.clockProvider( clockProvider )
				.buildValidatorFactory();

		validator = validatorFactory.getValidator();
	}

	@Test
	public void testFuture() {
		JavaSqlDateHolder javaSqlDateHolder = new JavaSqlDateHolder(
				new java.sql.Date( LocalDate.of( 2010, 2, 2 ).atStartOfDay( ZoneId.systemDefault() ).toInstant().toEpochMilli() ) );
		Set<ConstraintViolation<JavaSqlDateHolder>> constraintViolations = validator.validate( javaSqlDateHolder );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( Past.class ),
				violationOf( PastOrPresent.class ) );
	}

	@Test
	public void testPast() {
		JavaSqlDateHolder javaSqlDateHolder = new JavaSqlDateHolder(
				new java.sql.Date( LocalDate.of( 1990, 2, 2 ).atStartOfDay( ZoneId.systemDefault() ).toInstant().toEpochMilli() ) );
		Set<ConstraintViolation<JavaSqlDateHolder>> constraintViolations = validator.validate( javaSqlDateHolder );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( Future.class ),
				violationOf( FutureOrPresent.class ) );
	}

	private static class JavaSqlDateHolder {

		@Future
		private java.sql.Date future;

		@FutureOrPresent
		private java.sql.Date futureOrPresent;

		@Past
		private java.sql.Date past;

		@PastOrPresent
		private java.sql.Date pastOrPresent;

		JavaSqlDateHolder(java.sql.Date date) {
			this.future = date;
			this.futureOrPresent = date;
			this.past = date;
			this.pastOrPresent = date;
		}
	}
}
