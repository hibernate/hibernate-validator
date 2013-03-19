/*
* JBoss, Home of Professional Open Source
* Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.test.internal.engine.groups.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintViolation;
import javax.validation.Payload;
import javax.validation.Validator;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import org.hibernate.validator.testutil.TestForIssue;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectConstraintTypes;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNumberOfViolations;
import static org.hibernate.validator.testutil.ValidatorUtil.getValidator;
import static org.testng.Assert.fail;

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
	public void testConstraintIsOnlyValidatedOnceEvenWhenPartOfMultipleGroups() {
		A a = new A();

		Set<ConstraintViolation<A>> constraintViolations = validator.validate( a, First.class, Second.class );
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectConstraintTypes( constraintViolations, FailOnSecondValidationCall.class );
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

		public class FailOnSecondValidationCallValidator
				implements ConstraintValidator<FailOnSecondValidationCall, Object> {
			private final AtomicInteger invocationCount = new AtomicInteger( 0 );

			@Override
			public void initialize(FailOnSecondValidationCall parameters) {
			}

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
