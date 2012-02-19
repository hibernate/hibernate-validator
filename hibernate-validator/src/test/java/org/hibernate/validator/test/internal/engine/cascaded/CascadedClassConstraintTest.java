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
package org.hibernate.validator.test.internal.engine.cascaded;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintViolation;
import javax.validation.Payload;
import javax.validation.Valid;
import javax.validation.Validator;

import org.testng.annotations.Test;

import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutil.ValidatorUtil;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectPropertyPaths;

/**
 * @author Hardy Ferentschik
 */
public class CascadedClassConstraintTest {

	@Test
	@TestForIssue( jiraKey = "HV-509")
	public void testCascadedValidation() {
		Validator validator = ValidatorUtil.getValidator();
		Set<ConstraintViolation<Bar>> violations = validator.validate( new Bar() );

		assertCorrectPropertyPaths( violations, "foos[0]", "foos[1]" );
	}

	@ValidFoo
	private static class Foo {
	}

	private static class Bar {
		@Valid
		private List<Foo> foos = Arrays.asList( new Foo(), new Foo() );
	}

	@Constraint(validatedBy = { ValidFooValidator.class })
	@Target({ TYPE })
	@Retention(RUNTIME)
	public @interface ValidFoo {
		String message() default "{ValidFoo.message}";

		Class<?>[] groups() default { };

		Class<? extends Payload>[] payload() default { };
	}

	public static class ValidFooValidator implements ConstraintValidator<ValidFoo, Foo> {

		public void initialize(ValidFoo annotation) {
		}

		public boolean isValid(Foo foo, ConstraintValidatorContext context) {
			return false;
		}
	}
}


