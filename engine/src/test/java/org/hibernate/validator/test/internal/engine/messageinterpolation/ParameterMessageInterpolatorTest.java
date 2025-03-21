/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.messageinterpolation;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.hibernate.validator.testutils.ValidatorUtil.getConfiguration;
import static org.testng.Assert.assertTrue;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Size;

import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutils.ListAppender;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.testng.annotations.AfterTest;
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

	ListAppender listAppender;

	@BeforeTest
	public void setUp() {
		LoggerContext context = LoggerContext.getContext( false );
		Logger logger = context.getLogger( ParameterMessageInterpolator.class.getName() );
		listAppender = new ListAppender( "list" );
		logger.addAppender( listAppender );
		listAppender.clear();

		validator = getConfiguration()
				.messageInterpolator( new ParameterMessageInterpolator() )
				.buildValidatorFactory()
				.getValidator();
	}

	@AfterTest
	public void tearDown() {
		listAppender.clear();
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
		Foo foo = new Foo();
		Set<ConstraintViolation<Foo>> constraintViolations = validator.validateProperty( foo, "bar" );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( Size.class )
						.withProperty( "bar" )
						.withMessage( "${validatedValue}" ) );

		assertTrue( listAppender.getEvents().stream()
				.filter( event -> event.getLevel().equals( Level.WARN ) )
				.map( event -> event.getMessage().getFormattedMessage() )
				.anyMatch( m -> m.startsWith( "HV000185" ) ) );
	}

	public static class Foo {
		@Size(max = 1, message = "{max}")
		private String snafu = "12";

		@Size(max = 2, message = "${validatedValue}")
		private String bar = "123";
	}
}
