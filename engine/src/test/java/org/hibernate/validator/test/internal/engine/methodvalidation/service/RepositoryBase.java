/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.methodvalidation.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import org.hibernate.validator.test.internal.engine.methodvalidation.model.Customer;

/**
 * @author Gunnar Morling
 */
public interface RepositoryBase<T> {
	/**
	 * Used to test, that constraints at methods from base interfaces/classes are evaluated.
	 */
	T findById(@NotNull Long id);

	/**
	 * Used to test, that constraints at overridden methods from base interfaces/classes are evaluated.
	 */
	void foo(@NotNull Long id);

	void bar(@NotNull @Valid Customer customer);

	/**
	 * Used to test, that in an inheritance hierarchy multiple return value
	 * constraints for an overridden method are joined.
	 */
	@Min(5)
	int overriddenMethodWithReturnValueConstraint();

	@Valid
	Customer overriddenMethodWithCascadingReturnValue();
}
