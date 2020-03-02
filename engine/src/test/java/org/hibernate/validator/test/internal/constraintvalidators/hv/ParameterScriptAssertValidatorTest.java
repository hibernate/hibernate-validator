/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.constraintvalidators.hv;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.hibernate.validator.testutils.ValidatorUtil.getConfiguration;
import static org.hibernate.validator.testutils.ValidatorUtil.getValidatingProxy;
import static org.hibernate.validator.testutils.ValidatorUtil.getValidator;
import static org.testng.Assert.fail;

import java.lang.reflect.Constructor;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.executable.ExecutableValidator;

import org.hibernate.validator.constraints.ParameterScriptAssert;
import org.hibernate.validator.testutil.PrefixableParameterNameProvider;
import org.hibernate.validator.testutil.TestForIssue;
import org.testng.annotations.Test;

/**
 * Test for {@link org.hibernate.validator.internal.constraintvalidators.hv.ParameterScriptAssertValidator}.
 *
 * @author Gunnar Morling
 */
@TestForIssue(jiraKey = "HV-714")
public class ParameterScriptAssertValidatorTest {

	@Test
	public void shouldValidateParameterScriptAssertConstraint() {
		CalendarService calendar = getValidatingProxy( new CalendarServiceImpl(), getValidator() );

		Date startDate = new GregorianCalendar( 2009, 8, 20 ).getTime();
		Date endDate = new GregorianCalendar( 2009, 8, 21 ).getTime();

		try {
			calendar.createEvent( endDate, startDate );
			fail( "Expected exception wasn't raised" );
		}
		catch (ConstraintViolationException e) {
			assertThat( e.getConstraintViolations() ).containsOnlyViolations(
					violationOf( ParameterScriptAssert.class ).withMessage( "script expression \"start < end\" didn't evaluate to true" )
			);
		}
	}

	@Test
	public void shouldApplyParameterNameProvider() {
		CalendarService calendar = getValidatingProxy(
				new CalendarServiceImpl(),
				getConfiguration().parameterNameProvider( new PrefixableParameterNameProvider( "param" ) )
						.buildValidatorFactory()
						.getValidator()
		);

		Date startDate = new GregorianCalendar( 2009, 8, 20 ).getTime();
		Date endDate = new GregorianCalendar( 2009, 8, 21 ).getTime();

		try {
			calendar.createEvent( endDate, startDate, "Meeting" );
			fail( "Expected exception wasn't raised" );
		}
		catch (ConstraintViolationException e) {
			assertThat( e.getConstraintViolations() ).containsOnlyViolations(
					violationOf( ParameterScriptAssert.class ).withMessage( "script expression \"param0 < param1\" didn't evaluate to true" )
			);
		}
	}

	@Test
	public void shouldValidateParameterScriptAssertConstraintOnConstructor() throws Exception {
		ExecutableValidator executableValidator = getValidator().forExecutables();

		Constructor<CalendarServiceImpl> constructor = CalendarServiceImpl.class.getConstructor( String.class );
		Object[] parameterValues = new Object[] { "Foo" };

		Set<ConstraintViolation<CalendarServiceImpl>> violations = executableValidator.validateConstructorParameters(
				constructor,
				parameterValues
		);
		assertThat( violations ).containsOnlyViolations(
				violationOf( ParameterScriptAssert.class ).withMessage( "script expression \"name.size() > 3\" didn't evaluate to true" )
		);
	}

	public interface CalendarService {

		@ParameterScriptAssert(script = "start < end", lang = "groovy")
		void createEvent(Date start, Date end);


		@ParameterScriptAssert(script = "param0 < param1", lang = "groovy")
		void createEvent(Date start, Date end, String title);
	}

	public static class CalendarServiceImpl implements CalendarService {

		public CalendarServiceImpl() {
		}

		@ParameterScriptAssert(script = "name.size() > 3", lang = "groovy")
		public CalendarServiceImpl(String name) {
		}

		@Override
		public void createEvent(Date start, Date end) {
		}

		@Override
		public void createEvent(Date start, Date end, String title) {
		}
	}
}
