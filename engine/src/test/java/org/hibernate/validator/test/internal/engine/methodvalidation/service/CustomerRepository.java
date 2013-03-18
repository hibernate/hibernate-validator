/*
* JBoss, Home of Professional Open Source
* Copyright 2009, Red Hat Middleware LLC, and individual contributors
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

import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.joda.time.DateMidnight;

import org.hibernate.validator.constraints.NotEmpty;
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
