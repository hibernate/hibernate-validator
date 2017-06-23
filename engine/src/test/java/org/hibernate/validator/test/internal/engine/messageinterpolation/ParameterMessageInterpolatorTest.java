/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.messageinterpolation;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.hibernate.validator.testutils.ValidatorUtil.getConfiguration;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.constraints.Size;

import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.hibernate.validator.testutil.MessageLoggedAssertionLogger;
import org.hibernate.validator.testutil.TestForIssue;

import org.apache.log4j.Logger;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * Tests for {@link org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator}
 *
 * @author Hardy Ferentschik
 */
@TestForIssue(jiraKey = "HV-876")
public class ParameterMessageInterpolatorTest {

	Validator validator;

	@BeforeTest
	public void setUp() {
		validator = getConfiguration()
				.messageInterpolator( new ParameterMessageInterpolator() )
				.buildValidatorFactory()
				.getValidator();
	}

	@Test
	public void testParameterMessageInterpolatorInterpolatesParameters() {
		Foo foo = new Foo();
		Set<ConstraintViolation<Foo>> constraintViolations = validator.validateProperty( foo, "snafu" );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( Size.class )
						.withProperty( "snafu" )
						.withMessage( "1" )
		);
	}

	@Test
	public void testParameterMessageInterpolatorIgnoresELExpressions() {
		Logger log4jRootLogger = Logger.getRootLogger();
		MessageLoggedAssertionLogger assertingLogger = new MessageLoggedAssertionLogger( "HV000185" );
		log4jRootLogger.addAppender( assertingLogger );

		Foo foo = new Foo();
		Set<ConstraintViolation<Foo>> constraintViolations = validator.validateProperty( foo, "bar" );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( Size.class )
						.withProperty( "bar" )
						.withMessage( "${validatedValue}" )
		);

		assertingLogger.assertMessageLogged();
		log4jRootLogger.removeAppender( assertingLogger );
	}

	public static class Foo {
		@Size(max = 1, message = "{max}")
		private String snafu = "12";

		@Size(max = 2, message = "${validatedValue}")
		private String bar = "123";
	}
}
