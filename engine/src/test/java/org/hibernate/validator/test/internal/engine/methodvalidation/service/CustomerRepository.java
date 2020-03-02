/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.methodvalidation.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import org.hibernate.validator.test.internal.engine.methodvalidation.model.Customer;

/**
 * @author Gunnar Morling
 */
public interface CustomerRepository extends RepositoryBase<Customer> {

	@Valid
	Customer findCustomerByName(@NotNull String name);

	void persistCustomer(@NotNull @Valid Customer customer);

	void cascadingMapParameter(@Valid Map<String, Customer> customer);

	void cascadingIterableParameter(@Valid List<Customer> customer);

	void iterableParameterWithCascadingTypeParameter(List<@Valid Customer> customer);

	void cascadingArrayParameter(@Valid Customer... customer);

	void findCustomerByAgeAndName(@Min(5) Integer age, @NotNull String name);

	void cascadingParameter(@NotNull @Valid Customer param1, @NotNull @Valid Customer param2);

	@Override
	void foo(Long id);

	@Override
	void bar(Customer customer);

	void boz();

	@Min(10)
	int baz();

	@Valid
	Customer cascadingReturnValue();

	@Valid
	List<Customer> cascadingIterableReturnValue();

	@Valid
	Map<String, Customer> cascadingMapReturnValue();

	@Valid
	Customer[] cascadingArrayReturnValue();

	@Override
	Customer overriddenMethodWithCascadingReturnValue();

	void parameterConstraintInGroup(@NotNull(groups = { ValidationGroup.class }) String name);

	@Override
	@Min(10)
	int overriddenMethodWithReturnValueConstraint();

	int getFoo(int i);

	int getFoo(@NotEmpty String s);

	@ConsistentDateParameters
	void methodWithCrossParameterConstraint(@NotNull LocalDate start, @NotNull LocalDate end);

	public interface ValidationGroup {
	}
}
