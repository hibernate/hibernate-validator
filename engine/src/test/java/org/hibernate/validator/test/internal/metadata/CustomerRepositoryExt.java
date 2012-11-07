/*
* JBoss, Home of Professional Open Source
* Copyright 2011, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.test.internal.metadata;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.joda.time.DateMidnight;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author Gunnar Morling
 */
public class CustomerRepositoryExt extends CustomerRepository {

	public static class CustomerExtension extends Customer {
	}

	@ValidB2BRepository
	@Valid
	public CustomerRepositoryExt(@NotNull String foo) {
	}

	public CustomerRepositoryExt(@NotNull String foo, @Valid Customer customer) {
	}

	public CustomerRepositoryExt(int bar) {
	}

	@Override
	public Customer createCustomer(CharSequence firstName, String lastName) {
		return null;
	}

	@Override
	public void saveCustomer(Customer customer) {
	}

	@Override
	public void updateCustomer(Customer customer) {
	}

	@Override
	public Customer foo() {
		return null;
	}

	@Override
	public CustomerExtension bar() {
		return null;
	}

	@Override
	@Min(0)
	public int baz() {
		return 0;
	}

	public void zap() {
	}

	@Min(0)
	public int zip() {
		return 0;
	}

	@Min(0)
	public int zip(int i) {
		return 0;
	}

	@Override
	public void zap(@Min(0) int i) {
	}

	@ConsistentDateParameters
	public void methodWithCrossParameterConstraint(DateMidnight start, DateMidnight end) {
	}

	@Constraint(validatedBy = { ValidB2BRepositoryValidator.class })
	@Target({ TYPE, CONSTRUCTOR })
	@Retention(RUNTIME)
	public @interface ValidB2BRepository {
		String message() default "{ValidB2BRepository.message}";

		Class<?>[] groups() default { };

		Class<? extends Payload>[] payload() default { };
	}

	public static class ValidB2BRepositoryValidator
			implements ConstraintValidator<ValidB2BRepository, CustomerRepositoryExt> {

		@Override
		public void initialize(ValidB2BRepository annotation) {
		}

		@Override
		public boolean isValid(CustomerRepositoryExt repository, ConstraintValidatorContext context) {
			return false;
		}
	}

}
