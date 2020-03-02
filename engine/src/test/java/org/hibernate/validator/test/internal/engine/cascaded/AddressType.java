/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
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
