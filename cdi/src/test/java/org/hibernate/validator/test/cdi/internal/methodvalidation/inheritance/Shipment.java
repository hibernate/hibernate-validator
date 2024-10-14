/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.cdi.internal.methodvalidation.inheritance;

import jakarta.validation.constraints.NotNull;

/**
 * @author Gunnar Morling
 */
public class Shipment {

	@NotNull
	private String name;

	public String getName() {
		return name;
	}
}
