/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.groups.validation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.hibernate.validator.testutils.ValidatorUtil.getValidator;
import static org.testng.Assert.fail;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Payload;
import jakarta.validation.Validator;

import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutils.CandidateForTck;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Hardy Ferentschik
 */
public class GroupValidationTest {
	private Validator validator;

	@BeforeMethod
	public void setUp() {
		validator = getValidator();
	}

	@Test
	@TestForIssue(jiraKey = "HV-678")
	@CandidateForTck
	public void testConstraintIsOnlyValidatedOnceEvenWhenPartOfMultipleGroups() {
		A a = new A();

		Set<ConstraintViolation<A>> constraintViolations = validator.validate( a, First.class, Second.class );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( FailOnSecondValidationCall.class )
		);
	}

	public static class A {
		@FailOnSecondValidationCall(groups = { First.class, Second.class })
		private String foo;
	}

	public interface First {
	}

	public interface Second {
	}

	@Constraint(validatedBy = FailOnSecondValidationCall.FailOnSecondValidationCallValidator.class)
	@Documented
	@Target({ METHOD, FIELD, TYPE })
	@Retention(RUNTIME)
	public @interface FailOnSecondValidationCall {
		String message() default "my custom constraint";

		Class<?>[] groups() default { };

		Class<? extends Payload>[] payload() default { };

		class FailOnSecondValidationCallValidator
				implements ConstraintValidator<FailOnSecondValidationCall, Object> {
			private final AtomicInteger invocationCount = new AtomicInteger( 0 );

			@Override
			public boolean isValid(Object o, ConstraintValidatorContext constraintValidatorContext) {
				int invocationCount = this.invocationCount.incrementAndGet();
				if ( invocationCount > 1 ) {
					fail( "Validator instance called more than once" );
				}
				return false;
			}
		}
	}
}
