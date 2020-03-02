/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.cfg;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.pathWith;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.testng.Assert.assertTrue;

import java.time.Instant;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.defs.ScriptAssertDef;
import org.hibernate.validator.constraints.ScriptAssert;
import org.hibernate.validator.testutil.ConstraintViolationAssert;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutils.ValidatorUtil;
import org.testng.annotations.Test;

/**
 * @author Marko Bekhta
 */
public class ScriptAssertDefTest {

	@Test
	@TestForIssue(jiraKey = "HV-1201")
	public void testWithReportOn() {
		final HibernateValidatorConfiguration configuration = ValidatorUtil.getConfiguration( HibernateValidator.class );

		final ConstraintMapping programmaticMapping = configuration.createConstraintMapping();
		programmaticMapping.type( CalendarEvent.class )
				.constraint( new ScriptAssertDef().lang( "groovy" )
						.script( "_this.startDate.isBefore(_this.endDate)" )
						.reportOn( "startDate" )
				);
		configuration.addMapping( programmaticMapping );

		assertCalendarEventViolations( configuration.buildValidatorFactory().getValidator(), pathWith().property( "startDate" ) );
	}

	@Test
	@TestForIssue(jiraKey = "HV-1201")
	public void testWithoutReportOn() {
		final HibernateValidatorConfiguration configuration = ValidatorUtil.getConfiguration( HibernateValidator.class );

		final ConstraintMapping programmaticMapping = configuration.createConstraintMapping();
		programmaticMapping.type( CalendarEvent.class )
				.constraint( new ScriptAssertDef().lang( "groovy" )
						.script( "_this.startDate.isBefore(_this.endDate)" )
				);
		configuration.addMapping( programmaticMapping );

		assertCalendarEventViolations( configuration.buildValidatorFactory().getValidator(), pathWith().bean() );
	}

	@Test(expectedExceptions = ValidationException.class)
	@TestForIssue(jiraKey = "HV-1201")
	public void testBadLang() {
		final HibernateValidatorConfiguration configuration = ValidatorUtil.getConfiguration( HibernateValidator.class );

		final ConstraintMapping programmaticMapping = configuration.createConstraintMapping();
		programmaticMapping.type( CalendarEvent.class )
				.constraint( new ScriptAssertDef().lang( "not real lang" )
						.script( "and script is not real as well" )
				);
		configuration.addMapping( programmaticMapping );

		assertCalendarEventViolations( configuration.buildValidatorFactory().getValidator(), pathWith().bean() );
	}

	private void assertCalendarEventViolations(Validator validator, ConstraintViolationAssert.PathExpectation propertyPath) {
		assertTrue( validator.validate( new CalendarEvent(
						Instant.now(),
						Instant.now().plusMillis( 1000L )
				)
		).isEmpty(), "Should pass validation" );

		Set<ConstraintViolation<CalendarEvent>> violations = validator.validate( new CalendarEvent(
						Instant.now().plusMillis( 1000L ),
						Instant.now().minusMillis( 1000L )
				)
		);

		assertThat( violations ).containsOnlyViolations(
				violationOf( ScriptAssert.class ).withPropertyPath( propertyPath )

		);
	}

	/**
	 * Test model class.
	 */
	@SuppressWarnings("unused")
	private static class CalendarEvent {

		private final Instant startDate;
		private final Instant endDate;

		public CalendarEvent(Instant startDate, Instant endDate) {
			this.startDate = startDate;
			this.endDate = endDate;
		}
	}
}
