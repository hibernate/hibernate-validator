/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.methodvalidation.service;

/**
 * @author Gunnar Morling
 */
public class CustomerRepositoryWithConstrainedVoidMethodImpl implements CustomerRepositoryWithConstrainedVoidMethod {

	@Override
	public void voidMethodWithIllegalReturnValueConstraint() {
	}
}
