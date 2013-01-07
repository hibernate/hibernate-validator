/*
* JBoss, Home of Professional Open Source
* Copyright 2012, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.test.constraints.composition.lazyevaluation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
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
	@InvocationCounting.List(value = { @InvocationCounting(message="foo"), @InvocationCounting(message="bar") })
	public @interface MyComposedConstraint {
		String message() default "my composed constraint failed";

		Class<?>[] groups() default { };

		Class<? extends Payload>[] payload() default { };
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


