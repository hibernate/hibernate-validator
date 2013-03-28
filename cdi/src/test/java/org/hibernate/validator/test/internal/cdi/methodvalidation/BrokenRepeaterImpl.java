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
package org.hibernate.validator.test.internal.cdi.methodvalidation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import javax.validation.executable.ValidateOnExecution;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author Hardy Ferentschik
 */
@Broken
@ValidateOnExecution
public class BrokenRepeaterImpl implements Repeater {

	@BrokenConstraint
	BrokenRepeaterImpl() {
	}

	@Override
	public String repeat(String in) {
		return in;
	}

	@Override
	public String reverse(String in) {
		return null;
	}

	@Override
	public String getHelloWorld() {
		return null;
	}

	@Constraint(validatedBy = { BrokenConstraint.BrokenConstraintImpl.class })
	@Documented
	@Target({ ElementType.CONSTRUCTOR })
	@Retention(RUNTIME)
	public @interface BrokenConstraint {
		String message() default "foobar";

		Class<?>[] groups() default { };

		Class<? extends Payload>[] payload() default { };


		public class BrokenConstraintImpl implements ConstraintValidator<BrokenConstraint, Repeater> {

			public void initialize(BrokenConstraint parameters) {
			}

			public boolean isValid(Repeater repeater, ConstraintValidatorContext constraintValidatorContext) {
				return false;
			}
		}
	}
}
