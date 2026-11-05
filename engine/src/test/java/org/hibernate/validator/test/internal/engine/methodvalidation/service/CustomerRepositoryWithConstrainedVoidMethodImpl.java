package org.hibernate.validator.test.internal.engine.methodvalidation.service;

/**
 * @author Gunnar Morling
 */
public class CustomerRepositoryWithConstrainedVoidMethodImpl implements CustomerRepositoryWithConstrainedVoidMethod {

	@Override
	public void voidMethodWithIllegalReturnValueConstraint() {
	}
}
