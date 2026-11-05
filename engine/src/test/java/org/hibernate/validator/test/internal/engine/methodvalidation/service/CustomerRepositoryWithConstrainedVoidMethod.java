package org.hibernate.validator.test.internal.engine.methodvalidation.service;

import jakarta.validation.constraints.Min;

/**
 * @author Gunnar Morling
 */
public interface CustomerRepositoryWithConstrainedVoidMethod {

	@Min(10)
	void voidMethodWithIllegalReturnValueConstraint();
}
