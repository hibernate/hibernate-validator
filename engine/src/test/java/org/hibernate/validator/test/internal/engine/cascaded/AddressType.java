/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.cascaded;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

class AddressType {

	@NotNull
	@Size(min = 10)
	private final String type;

	public AddressType(String value) {
		this.type = value;
	}

	@Override
	public String toString() {
		return type;
	}
}
