/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.methodvalidation;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import jakarta.validation.ParameterNameProvider;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.testutil.TestForIssue;

import org.testng.annotations.Test;

/**
 * @author Marko Bekhta
 */
@Test
public class ParameterlessMethodValidationTest {

	@Test
	@TestForIssue(jiraKey = "HV-966")
	public void testEmptyParameters() throws NoSuchMethodException {
		HibernateValidatorConfiguration config = Validation.byProvider( HibernateValidator.class ).configure();

		ParameterNameProvider providerMock = new ParameterNameProvider() {

			@Override
			public List<String> getParameterNames(Constructor<?> constructor) {
				throw new IllegalStateException( "this method shouldn't be invoked" );
			}

			@Override
			public List<String> getParameterNames(Method method) {
				if ( method.getParameters().length == 0 ) {
					throw new IllegalStateException( "getParameterNames() shouldn't be invoked for parameterless method" + method.getName() );
				}
				return Arrays.asList( new String[method.getParameterCount()] );
			}
		};

		Validator validator = config
				.parameterNameProvider( providerMock )
				.buildValidatorFactory()
				.getValidator();

		Bar bar = new Bar();

		assertNoViolations( validator.forExecutables().validateParameters( bar, bar.getClass().getMethod( "getString" ), new Object[]{} ) );
	}

	private static class Bar {

		private String string;

		@SuppressWarnings("unused")
		public String getString() {
			return string;
		}
	}

}
