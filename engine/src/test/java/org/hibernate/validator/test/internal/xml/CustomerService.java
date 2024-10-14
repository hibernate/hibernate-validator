/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.xml;

import jakarta.validation.constraints.NotNull;

/**
 * @author Gunnar Morling
 */
public class CustomerService {

	public void createCustomer(@NotNull Customer customer) {
	}
}
