/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.methodvalidation.service;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashMap;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import org.hibernate.validator.test.internal.engine.methodvalidation.model.Customer;

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
	public void iterableParameterWithCascadingTypeParameter(List<Customer> customer) {
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
	public void methodWithCrossParameterConstraint(LocalDate start, LocalDate end) {
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
		public boolean isValid(CustomerRepositoryImpl repository, ConstraintValidatorContext context) {
			return false;
		}
	}
}
