/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNumberOfViolations;
import static org.hibernate.validator.testutils.ValidatorUtil.getValidator;

import java.util.Optional;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.valuehandling.UnwrapValidatedValue;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Marko Bekhta
 */
public class TraceLoggingTest {

	private Level currentLevel;

	@BeforeMethod
	public void setUp() throws Exception {
		currentLevel = LogManager.getRootLogger().getLevel();
		LogManager.getRootLogger().setLevel( Level.TRACE );
	}

	@Test
	@TestForIssue(jiraKey = "HV-1352")
	public void testLoggingIsNotFailing() {
		assertNumberOfViolations( getValidator().validate( new Foo( Optional.of( 1 ), 1 ) ), 0 );
	}

	@AfterMethod
	public void tearDown() throws Exception {
		LogManager.getRootLogger().setLevel( currentLevel );
	}

	private static class Foo {

		@NotNull
		@UnwrapValidatedValue
		private final Optional<Integer> optInt;
		@NotNull
		private final Integer integer;

		public Foo(@NotNull Optional<Integer> optInt, @NotNull Integer integer) {
			this.optInt = optInt;
			this.integer = integer;
		}
	}

}
