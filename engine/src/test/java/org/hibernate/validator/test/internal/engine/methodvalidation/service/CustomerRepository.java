/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.methodvalidation.service;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.test.internal.engine.methodvalidation.model.Customer;
import org.joda.time.DateMidnight;

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
	void methodWithCrossParameterConstraint(@NotNull DateMidnight start, @NotNull DateMidnight end);

	public interface ValidationGroup {
	}
}
