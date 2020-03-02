/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.constraints.composition.lazyevaluation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import jakarta.validation.ReportAsSingleViolation;
import jakarta.validation.Validator;

import org.testng.annotations.Test;

import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutils.ValidatorUtil;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.testng.Assert.assertEquals;

/**
 * @author Hardy Ferentschik
 */
public class LazyEvaluationOfComposingConstraintsTest {

	@Test
	@TestForIssue(jiraKey = "HV-639")
	public void testComposedConstraintWithReportAsSingleViolationStopsEvaluatingOnFirstViolation() {
		Validator validator = ValidatorUtil.getValidator();

		Foo foo = new Foo();
		validator.validate( foo );
		assertEquals(
				InvocationCounter.getInvocationCount( foo ),
				1,
				"There should have been only one single invocation due to @ReportAsSingleViolation"
		);
	}

	@MyComposedConstraint
	public class Foo {
	}

	@Constraint(validatedBy = { MyComposedConstraintValidator.class })
	@Target({ METHOD, FIELD, TYPE })
	@Retention(RUNTIME)
	@ReportAsSingleViolation
	@InvocationCounting.List(value = { @InvocationCounting })
	public @interface MyComposedConstraint {
		String message() default "my composed constraint failed";

		Class<?>[] groups() default { };

		Class<? extends Payload>[] payload() default { };
	}

	public static class MyComposedConstraintValidator extends InvocationCounter
			implements ConstraintValidator<MyComposedConstraint, Object> {

		@Override
		public boolean isValid(Object value, ConstraintValidatorContext context) {
			incrementCount( value );
			return false;
		}
	}
}


