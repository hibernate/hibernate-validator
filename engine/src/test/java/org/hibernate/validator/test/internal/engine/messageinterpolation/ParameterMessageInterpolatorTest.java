/*
* JBoss, Home of Professional Open Source
* Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.test.internal.engine.messageinterpolation;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.constraints.Size;

import org.apache.log4j.Logger;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.hibernate.validator.testutil.MessageLoggedAssertionLogger;
import org.hibernate.validator.testutil.TestForIssue;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectConstraintViolationMessages;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNumberOfViolations;
import static org.hibernate.validator.testutil.ValidatorUtil.getConfiguration;

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
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectConstraintViolationMessages( constraintViolations, "1" );
	}

	@Test
	public void testParameterMessageInterpolatorIgnoresELExpressions() {
		Logger log4jRootLogger = Logger.getRootLogger();
		MessageLoggedAssertionLogger assertingLogger = new MessageLoggedAssertionLogger( "HV000185" );
		log4jRootLogger.addAppender( assertingLogger );

		Foo foo = new Foo();
		Set<ConstraintViolation<Foo>> constraintViolations = validator.validateProperty( foo, "bar" );
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectConstraintViolationMessages( constraintViolations, "${validatedValue}" );

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
