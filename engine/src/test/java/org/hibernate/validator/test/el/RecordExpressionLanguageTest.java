/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.el;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.GenericConstraintDef;
import org.hibernate.validator.messageinterpolation.ExpressionLanguageFeatureLevel;
import org.hibernate.validator.testutil.ConstraintViolationAssert;
import org.hibernate.validator.testutil.TestForIssue;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

@TestForIssue(jiraKey = "HV-2283")
public class RecordExpressionLanguageTest {

	@DataProvider
	public Object[][] expressions() {
		// Expression, expected result with properties enabled, expected result with methods enabled, method invocations.
		Object[][] expressions = {
				{ "${validatedValue.name}", "Alice", "Alice", 0 },
				{ "${validatedValue['name']}", "Alice", "Alice", 0 },
				{ "${validatedValue.displayName}", "Display Alice", "Display Alice", 0 },
				{ "${validatedValue.active}", "true", "true", 0 },
				{ "${validatedValue.optional}", "", "", 0 },
				{ "${validatedValue[validatedValue.property]}", "Alice", "Alice", 0 },
				{ "${validatedValue.name()}", "${validatedValue.name()}", "Alice", 0 },
				{ "${validatedValue.getName()}", "${validatedValue.getName()}", "Getter Alice", 0 },
				{ "${validatedValue.getDisplayName()}", "${validatedValue.getDisplayName()}", "Display Alice", 0 },
				{ "${validatedValue.performAction}", "${validatedValue.performAction}", "${validatedValue.performAction}", 0 },
				{ "${validatedValue['performAction']}", "${validatedValue['performAction']}", "${validatedValue['performAction']}", 0 },
				{ "${validatedValue.performAction()}", "${validatedValue.performAction()}", "executed", 1 },
				{ "${validatedValue.performAction('argument')}", "${validatedValue.performAction('argument')}", "executed argument", 1 },
				{ "${validatedValue.toString}", "${validatedValue.toString}", "${validatedValue.toString}", 0 },
				{ "${validatedValue.missing}", "${validatedValue.missing}", "${validatedValue.missing}", 0 }
		};
		List<Object[]> cases = new ArrayList<>();
		for ( ExpressionLanguageFeatureLevel level : ExpressionLanguageFeatureLevel.values() ) {
			boolean propertiesEnabled = level == ExpressionLanguageFeatureLevel.DEFAULT
					|| level == ExpressionLanguageFeatureLevel.BEAN_PROPERTIES
					|| level == ExpressionLanguageFeatureLevel.BEAN_METHODS;
			boolean methodsEnabled = level == ExpressionLanguageFeatureLevel.BEAN_METHODS;
			for ( Object[] expression : expressions ) {
				Object expectedMessage = methodsEnabled ? expression[2] : propertiesEnabled ? expression[1] : expression[0];
				cases.add( new Object[] { level, expression[0], expectedMessage, methodsEnabled ? expression[3] : 0 } );
			}
		}
		return cases.toArray( new Object[0][] );
	}

	@Test(dataProvider = "expressions")
	public void record_expression_language_feature_level(ExpressionLanguageFeatureLevel level, String expression, String expectedMessage,
			int expectedInvocations) {
		AtomicInteger invocations = new AtomicInteger();
		Value value = new Value( "Alice", invocations, new ChangingProperty(), null );
		HibernateValidatorConfiguration configuration = Validation.byProvider( HibernateValidator.class )
				.configure()
				.constraintExpressionLanguageFeatureLevel( level );
		ConstraintMapping mapping = configuration.createConstraintMapping();
		mapping.type( Value.class ).constraint( new GenericConstraintDef<>( InvalidRecord.class ).message( expression ) );

		try ( ValidatorFactory factory = configuration.addMapping( mapping ).buildValidatorFactory() ) {
			var violations = factory.getValidator().validate( value );

			assertThat( invocations.get() ).as( "Invocations of a method that is not a record component accessor" ).isEqualTo( expectedInvocations );
			ConstraintViolationAssert.assertThat( violations )
					.containsOnlyViolations( violationOf( InvalidRecord.class ).withMessage( expectedMessage ) );
		}
	}

	public record Value(String name, AtomicInteger invocations, Object property, String optional) {

		public String performAction() {
			invocations.incrementAndGet();
			return "executed";
		}

		public String performAction(String argument) {
			invocations.incrementAndGet();
			return "executed " + argument;
		}

		public String getName() {
			return "Getter " + name;
		}

		public String getDisplayName() {
			return "Display " + name;
		}

		public boolean isActive() {
			return true;
		}

		public String getOptional() {
			return "Should not fall back when a component is null";
		}
	}

	private static class ChangingProperty {
		private int calls;

		@Override
		public String toString() {
			return calls++ == 0 ? "name" : "performAction";
		}
	}

	@Target(TYPE)
	@Retention(RUNTIME)
	@Constraint(validatedBy = InvalidRecordValidator.class)
	private @interface InvalidRecord {
		String message() default "Invalid record";

		Class<?>[] groups() default { };

		Class<? extends Payload>[] payload() default { };
	}

	public static class InvalidRecordValidator implements ConstraintValidator<InvalidRecord, Value> {

		@Override
		public boolean isValid(Value value, ConstraintValidatorContext context) {
			return false;
		}
	}
}
