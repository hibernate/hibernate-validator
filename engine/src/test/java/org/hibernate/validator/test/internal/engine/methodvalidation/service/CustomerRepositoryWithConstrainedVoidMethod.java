/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.methodvalidation.service;

import jakarta.validation.constraints.Min;

/**
 * @author Gunnar Morling
 */
public interface CustomerRepositoryWithConstrainedVoidMethod {

	@Min(10)
	void voidMethodWithIllegalReturnValueConstraint();
}
