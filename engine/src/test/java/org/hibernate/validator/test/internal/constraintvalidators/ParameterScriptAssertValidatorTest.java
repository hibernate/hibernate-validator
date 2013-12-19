/*
* JBoss, Home of Professional Open Source
* Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual contributors
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.hibernate.validator.test.internal.constraintvalidators;

import java.lang.reflect.Constructor;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.executable.ExecutableValidator;

import org.testng.annotations.Test;

import org.hibernate.validator.constraints.ParameterScriptAssert;
import org.hibernate.validator.internal.engine.DefaultParameterNameProvider;
import org.hibernate.validator.testutil.TestForIssue;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectConstraintViolationMessages;
import static org.hibernate.validator.testutil.ValidatorUtil.getConfiguration;
import static org.hibernate.validator.testutil.ValidatorUtil.getValidatingProxy;
import static org.hibernate.validator.testutil.ValidatorUtil.getValidator;
import static org.testng.Assert.fail;

/**
 * Test for {@link org.hibernate.validator.internal.constraintvalidators.ParameterScriptAssertValidator}.
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
		catch ( ConstraintViolationException e ) {
			assertCorrectConstraintViolationMessages( e, "script expression \"arg0 < arg1\" didn't evaluate to true" );
		}
	}

	@Test
	public void shouldApplyParameterNameProvider() {
		CalendarService calendar = getValidatingProxy(
				new CalendarServiceImpl(),
				getConfiguration().parameterNameProvider( new DummyParameterNameProvider() )
						.buildValidatorFactory()
						.getValidator()
		);

		Date startDate = new GregorianCalendar( 2009, 8, 20 ).getTime();
		Date endDate = new GregorianCalendar( 2009, 8, 21 ).getTime();

		try {
			calendar.createEvent( endDate, startDate, "Meeting" );
			fail( "Expected exception wasn't raised" );
		}
		catch ( ConstraintViolationException e ) {
			assertCorrectConstraintViolationMessages(
					e,
					"script expression \"param0 < param1\" didn't evaluate to true"
			);
		}
	}

	@Test
	public void shouldValidateParameterScriptAssertConstraintOnConstructor() throws Exception {
		ExecutableValidator executableValidator = getValidator().forExecutables();

		Constructor<?> constructor = CalendarServiceImpl.class.getConstructor( String.class );
		Object[] parameterValues = new Object[] { "Foo" };

		Set<ConstraintViolation<Object>> violations = executableValidator.validateConstructorParameters(
				constructor,
				parameterValues
		);
		assertCorrectConstraintViolationMessages(
				violations,
				"script expression \"arg0.size() > 3\" didn't evaluate to true"
		);
	}

	public interface CalendarService {

		@ParameterScriptAssert(script = "arg0 < arg1", lang = "groovy")
		void createEvent(Date start, Date end);


		@ParameterScriptAssert(script = "param0 < param1", lang = "groovy")
		void createEvent(Date start, Date end, String title);
	}

	public static class CalendarServiceImpl implements CalendarService {

		public CalendarServiceImpl() {
		}

		@ParameterScriptAssert(script = "arg0.size() > 3", lang = "groovy")
		public CalendarServiceImpl(String name) {
		}

		@Override
		public void createEvent(Date start, Date end) {
		}

		@Override
		public void createEvent(Date start, Date end, String title) {
		}
	}

	private static class DummyParameterNameProvider extends DefaultParameterNameProvider {
		@Override
		protected String getPrefix() {
			return "param";
		}
	}
}
