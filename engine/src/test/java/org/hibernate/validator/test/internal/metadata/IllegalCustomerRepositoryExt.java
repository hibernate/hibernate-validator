/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.metadata;

import jakarta.validation.constraints.Min;

/**
 * @author Gunnar Morling
 */
public class IllegalCustomerRepositoryExt extends CustomerRepository {

	@Override
	public void zap(@Min(0) int i) {
	}
}
