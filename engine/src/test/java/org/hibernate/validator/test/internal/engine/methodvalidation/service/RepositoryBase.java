/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.methodvalidation.service;

import org.hibernate.validator.test.internal.engine.methodvalidation.model.Customer;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

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
