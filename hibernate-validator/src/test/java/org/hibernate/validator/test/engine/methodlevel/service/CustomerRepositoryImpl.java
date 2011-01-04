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
package org.hibernate.validator.test.engine.methodlevel.service;

import java.util.List;
import java.util.Map;

import org.hibernate.validator.test.engine.methodlevel.model.Customer;

/**
 * @author Gunnar Morling
 */
public class CustomerRepositoryImpl implements CustomerRepository {

	public Customer findCustomerByName(String name) {
		return null;
	}

	public void persistCustomer(Customer customer) {

	}

	public void cascadingMapParameter(Map<String, Customer> customer) {

	}

	public void cascadingIterableParameter(List<Customer> customer) {

	}

	public void cascadingArrayParameter(Customer... customer) {

	}

	public void findCustomerByAgeAndName(Integer age, String name) {

	}

	public Customer findById(Long id) {
		return null;
	}

	public void foo(Long id) {

	}

	public void bar(Customer customer) {

	}

	public void boz() {

	}

	public int baz() {
		return 9;
	}

	public void parameterConstraintInGroup(String name) {

	}

	public void namedParameters(String param1, Customer customer) {

	}

}
