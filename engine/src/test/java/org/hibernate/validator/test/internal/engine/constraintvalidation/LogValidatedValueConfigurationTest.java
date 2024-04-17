/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.constraintvalidation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.hibernate.validator.testutils.ValidatorUtil.getConfiguration;
import static org.testng.Assert.assertTrue;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Set;

import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutils.ListAppender;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.apache.logging.log4j.core.LoggerContext;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

@TestForIssue(jiraKey = "HV-1903")
public class LogValidatedValueConfigurationTest {

	ListAppender simpleAppender;
	ListAppender composingAppender;

	@BeforeTest
	public void setUp() {
		LoggerContext context = LoggerContext.getContext( false );
		simpleAppender = new ListAppender( "simple" );
		context.getLogger( "org.hibernate.validator.internal.engine.constraintvalidation.SimpleConstraintTree" )
				.addAppender( simpleAppender );
		simpleAppender.clear();

		composingAppender = new ListAppender( "composing" );
		context.getLogger( "org.hibernate.validator.internal.engine.constraintvalidation.ComposingConstraintTree" )
				.addAppender( composingAppender );
		composingAppender.clear();
	}

	@AfterTest
	public void tearDown() {
		simpleAppender.clear();
		composingAppender.clear();
	}

	@Test
	public void testValueLoggingIsEnabled() {
		Foo foo = new Foo();
		Set<ConstraintViolation<Foo>> constraintViolations = getConfiguration()
				.showValidatedValuesInTraceLogs( true )
				.buildValidatorFactory()
				.getValidator()
				.validate( foo );

		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( Size.class ).withProperty( "value" ),
				violationOf( Size.class ).withProperty( "value2" ),
				violationOf( ComposingConstraint.class ).withProperty( "value2" )
		);

		assertTrue(
				simpleAppender.getEvents().stream()
						.map( event -> event.getMessage().getFormattedMessage() )
						.anyMatch( m -> m.startsWith( "Validating value 123 against constraint defined by" ) )
		);
		assertTrue(
				composingAppender.getEvents().stream()
						.map( event -> event.getMessage().getFormattedMessage() )
						.anyMatch( m -> m.startsWith( "Validating value 123 against constraint defined by" ) )
		);
	}

	@Test
	public void testValueLoggingIsDisabledByDefault() {
		Foo foo = new Foo();
		Set<ConstraintViolation<Foo>> constraintViolations = getConfiguration()
				.buildValidatorFactory()
				.getValidator()
				.validate( foo );

		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( Size.class ).withProperty( "value" ),
				violationOf( Size.class ).withProperty( "value2" ),
				violationOf( ComposingConstraint.class ).withProperty( "value2" )
		);

		assertTrue(
				simpleAppender.getEvents().stream()
						.map( event -> event.getMessage().getFormattedMessage() )
						.anyMatch( m -> m.startsWith( "Validating against constraint defined by" ) )
		);
		assertTrue(
				composingAppender.getEvents().stream()
						.map( event -> event.getMessage().getFormattedMessage() )
						.anyMatch( m -> m.startsWith( "Validating against constraint defined by" ) )
		);
	}

	@Test
	public void testValueLoggingIsEnabledUsingProperty() {
		Foo foo = new Foo();
		Set<ConstraintViolation<Foo>> constraintViolations = getConfiguration()
				.addProperty( "hibernate.validator.show_validated_value_in_trace_logs", "true" )
				.showValidatedValuesInTraceLogs( true )
				.buildValidatorFactory()
				.getValidator()
				.validate( foo );

		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( Size.class ).withProperty( "value" ),
				violationOf( Size.class ).withProperty( "value2" ),
				violationOf( ComposingConstraint.class ).withProperty( "value2" )
		);

		assertTrue(
				simpleAppender.getEvents().stream()
						.map( event -> event.getMessage().getFormattedMessage() )
						.anyMatch( m -> m.startsWith( "Validating value 123 against constraint defined by" ) )
		);
		assertTrue(
				composingAppender.getEvents().stream()
						.map( event -> event.getMessage().getFormattedMessage() )
						.anyMatch( m -> m.startsWith( "Validating value 123 against constraint defined by" ) )
		);
	}

	public static class Foo {
		@Size(max = 2)
		private String value = "123";

		@ComposingConstraint
		private String value2 = "123";
	}

	@NotNull
	@Size(max = 2)
	@Target({ FIELD })
	@Retention(RUNTIME)
	@Constraint(validatedBy = ComposingConstraint.Validator.class)
	@Documented
	public @interface ComposingConstraint {
		String message() default "";

		Class<?>[] groups() default {};

		Class<? extends Payload>[] payload() default {};

		class Validator implements ConstraintValidator<ComposingConstraint, Object> {

			@Override
			public boolean isValid(Object value, ConstraintValidatorContext context) {
				return false;
			}
		}
	}

}
