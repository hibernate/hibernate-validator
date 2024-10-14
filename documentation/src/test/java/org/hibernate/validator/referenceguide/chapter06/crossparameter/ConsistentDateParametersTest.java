/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.referenceguide.chapter06.crossparameter;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.executable.ExecutableValidator;

import org.junit.BeforeClass;
import org.junit.Test;

public class ConsistentDateParametersTest {

	private static ExecutableValidator executableValidator;

	@BeforeClass
	public static void setUpValidator() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		executableValidator = factory.getValidator().forExecutables();
	}

	@Test
	public void testConsistentDateParametersConstraint() throws Exception {
		CalendarService object = new CalendarService();
		Method method = CalendarService.class.getMethod( "createCalendarEvent", Date.class, Date.class );

		Calendar start = Calendar.getInstance();
		start.set( 2013, 5, 10 );
		Calendar end = Calendar.getInstance();
		end.set( 2013, 5, 9 );
		Object[] parameterValues = new Object[] { start.getTime(), end.getTime() };
		Set<ConstraintViolation<CalendarService>> constraintViolations = executableValidator.validateParameters(
				object,
				method,
				parameterValues
		);

		assertEquals( 1, constraintViolations.size() );
		assertEquals(
				ConsistentDateParameters.class,
				constraintViolations.iterator().next().getConstraintDescriptor().getAnnotation().annotationType()
		);
	}

	private static class CalendarService {

		@ConsistentDateParameters
		public void createCalendarEvent(@NotNull Date start, @NotNull Date end) {
			//...
		}
	}
}
