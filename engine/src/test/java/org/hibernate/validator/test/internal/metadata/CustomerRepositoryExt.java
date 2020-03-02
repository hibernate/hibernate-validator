/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.metadata;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.time.LocalDate;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.groups.ConvertGroup;
import jakarta.validation.groups.Default;

import org.hibernate.validator.test.internal.metadata.Customer.CustomerBasic;
import org.hibernate.validator.test.internal.metadata.Customer.CustomerComplex;

/**
 * @author Gunnar Morling
 */
public class CustomerRepositoryExt extends CustomerRepository {

	public interface CustomerRepositoryExtBasic {
	}

	public interface CustomerRepositoryExtComplex {
	}

	public interface CustomerRepositoryExtReturnValueComplex {
	}

	public static class CustomerExtension extends Customer {
	}

	@ValidB2BRepository
	@Valid
	public CustomerRepositoryExt(@NotNull String foo) {
		super();
	}

	public CustomerRepositoryExt(@NotNull String foo, @Valid Customer customer) {
	}

	public CustomerRepositoryExt(int bar) {
	}

	@Valid
	public CustomerRepositoryExt(LocalDate start, LocalDate end) {
	}

	public CustomerRepositoryExt(long l) {
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

	@Valid
	@ConvertGroup.List({
			@ConvertGroup(from = CustomerRepositoryExtBasic.class, to = CustomerBasic.class),
			@ConvertGroup(from = CustomerRepositoryExtReturnValueComplex.class,
					to = CustomerComplex.class)
	})
	public Customer modifyCustomer(
			@Valid
			@ConvertGroup.List({
					@ConvertGroup(from = Default.class, to = CustomerBasic.class),
					@ConvertGroup(from = CustomerRepositoryExtComplex.class,
							to = CustomerComplex.class)
			})
			Customer customer) {
		return null;
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

	public void zap(@NotNull Integer value) {
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
			implements ConstraintValidator<ValidB2BRepository, CustomerRepositoryExt> {

		@Override
		public boolean isValid(CustomerRepositoryExt repository, ConstraintValidatorContext context) {
			return false;
		}
	}

}
