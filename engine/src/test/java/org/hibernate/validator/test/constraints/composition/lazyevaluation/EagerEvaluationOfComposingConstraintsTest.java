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
public class EagerEvaluationOfComposingConstraintsTest {

	@Test
	@TestForIssue(jiraKey = "HV-639")
	public void testComposedConstraintWithoutReportAsSingleViolationEvaluatesAllConstraints() {
		Validator validator = ValidatorUtil.getValidator();

		Foo foo = new Foo();
		validator.validate( foo );
		assertEquals(
				InvocationCounter.getInvocationCount( foo ),
				3,
				"Each constraint should be evaluated since w/o @ReportAsSingleViolation there might be several violations"
		);
	}

	@MyComposedConstraint
	public class Foo {
	}

	@Constraint(validatedBy = MyComposedConstraintValidator.class)
	@Target({ METHOD, FIELD, TYPE })
	@Retention(RUNTIME)
	@InvocationCounting.List(value = { @InvocationCounting(message = "foo"), @InvocationCounting(message = "bar") })
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


