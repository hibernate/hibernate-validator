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

import java.util.Set;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import javax.validation.groups.ConvertGroup;
import javax.validation.groups.Default;

import org.joda.time.DateMidnight;
import org.hibernate.validator.constraints.ScriptAssert;
import org.hibernate.validator.valuehandling.UnwrapValidatedValue;

/**
 * @author Gunnar Morling
 */
@ScriptAssert(lang = "javascript", script = "some script")
public class CustomerRepository {

	public interface ValidationGroup {
	}

	@Valid
	public CustomerRepository() {
	}

	@ConsistentDateParameters
	public CustomerRepository(DateMidnight start, DateMidnight end) {
	}

	public Customer createCustomer(CharSequence firstName, @NotNull String lastName) {
		return null;
	}

	public void saveCustomer(@Valid Customer customer) {
	}

	public void updateCustomer(Customer customer) {
	}

	@Valid
	public Customer foo() {
		return null;
	}

	@NotNull
	public Customer bar() {
		return null;
	}

	@NotNull(groups = ValidationGroup.class)
	public int baz() {
		return 0;
	}

	public void qux() {
	}

	public void qax(@Max(1) Integer i) {
	}

	public void zap(@Max(1) int i) {
	}

	@ConsistentDateParameters(groups = ValidationGroup.class)
	public void methodWithCrossParameterConstraint(DateMidnight start, DateMidnight end) {
	}

	public void methodWithParameterGroupConversion(
			@Valid
			@ConvertGroup(from = Default.class, to = ValidationGroup.class)
			Set<String> addresses) {
	}

	@Valid
	@ConvertGroup(from = Default.class, to = ValidationGroup.class)
	public Set<String> methodWithReturnValueGroupConversion() {
		return null;
	}

	@UnwrapValidatedValue
	public Set<String> methodRequiringUnwrapping() {
		return null;
	}

	public void methodWithParameterRequiringUnwrapping(@UnwrapValidatedValue long l) {
	}
}
