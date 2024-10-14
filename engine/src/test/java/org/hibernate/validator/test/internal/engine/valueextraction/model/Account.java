/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.valueextraction.model;

import jakarta.validation.Valid;

/**
 * @author Gunnar Morling
 */
public class Account {

	@Valid
	private final Customer customer = new Customer();
}
