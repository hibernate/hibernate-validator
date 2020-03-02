/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.metadata;

import java.time.LocalDate;
import java.util.Set;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.groups.ConvertGroup;
import jakarta.validation.groups.Default;

import org.hibernate.validator.constraints.ScriptAssert;

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
	public CustomerRepository(LocalDate start, LocalDate end) {
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
	public void methodWithCrossParameterConstraint(LocalDate start, LocalDate end) {
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

}
