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

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

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
