package org.hibernate.validator.test.constraints.composition.lazyevaluation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import javax.validation.ReportAsSingleViolation;
import javax.validation.Validator;

import org.testng.annotations.Test;

import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutil.ValidatorUtil;

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
		public abstract String message() default "my composed constraint failed";

		public abstract Class<?>[] groups() default { };

		public abstract Class<? extends Payload>[] payload() default { };
	}

	public static class MyComposedConstraintValidator extends InvocationCounter
			implements ConstraintValidator<MyComposedConstraint, Object> {
		@Override
		public void initialize(MyComposedConstraint constraintAnnotation) {
		}

		@Override
		public boolean isValid(Object value, ConstraintValidatorContext context) {
			incrementCount( value );
			return false;
		}
	}
}


