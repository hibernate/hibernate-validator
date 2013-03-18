/*
* JBoss, Home of Professional Open Source
* Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.hibernate.validator.test.internal.engine.methodvalidation.service;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.joda.time.DateMidnight;

import org.hibernate.validator.test.internal.engine.methodvalidation.model.Customer;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashMap;

/**
 * @author Gunnar Morling
 */
public class CustomerRepositoryImpl implements CustomerRepository {

	@NotNull
	private final Customer customer = null;

	@ValidB2BRepository
	public CustomerRepositoryImpl() {
	}

	@Valid
	public CustomerRepositoryImpl(@NotNull String id) {
	}

	public CustomerRepositoryImpl(@Valid Customer customer) {
	}

	@Override
	public Customer findCustomerByName(String name) {
		return null;
	}

	@Override
	public void persistCustomer(Customer customer) {
	}

	@Override
	public void cascadingMapParameter(Map<String, Customer> customer) {
	}

	@Override
	public void cascadingIterableParameter(List<Customer> customer) {
	}

	@Override
	public void cascadingArrayParameter(Customer... customer) {
	}

	@Override
	public void findCustomerByAgeAndName(Integer age, String name) {
	}

	@Override
	public void cascadingParameter(Customer param1, Customer param2) {
	}

	@Override
	public Customer findById(Long id) {
		return null;
	}

	@Override
	public void foo(Long id) {
	}

	@Override
	public void bar(Customer customer) {
	}

	@Override
	public void boz() {
	}

	@Override
	public int baz() {
		return 9;
	}

	@Override
	public Customer cascadingReturnValue() {
		return new Customer( null );
	}

	@Override
	public List<Customer> cascadingIterableReturnValue() {
		return Arrays.asList( null, new Customer( null ) );
	}

	@Override
	public Map<String, Customer> cascadingMapReturnValue() {
		Map<String, Customer> theValue = newHashMap();
		theValue.put( "Bob", new Customer( null ) );
		return theValue;
	}

	@Override
	public Customer[] cascadingArrayReturnValue() {
		return new Customer[] { null, new Customer( null ) };
	}

	@Override
	public Customer overriddenMethodWithCascadingReturnValue() {
		return new Customer( null );
	}

	@Override
	public void parameterConstraintInGroup(String name) {
	}

	@Override
	public int overriddenMethodWithReturnValueConstraint() {
		return 3;
	}

	@Override
	public int getFoo(int i) {
		return 0;
	}

	@Override
	public int getFoo(String s) {
		return 0;
	}

	@Override
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
			implements ConstraintValidator<ValidB2BRepository, CustomerRepositoryImpl> {

		@Override
		public void initialize(ValidB2BRepository annotation) {
		}

		@Override
		public boolean isValid(CustomerRepositoryImpl repository, ConstraintValidatorContext context) {
			return false;
		}
	}
}
