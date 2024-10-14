/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.metadata;

import jakarta.validation.constraints.Pattern;

/**
 * @author Hardy Ferentschik
 */
public class Engine {
	@Pattern.List({
			@Pattern(regexp = "^[A-Z0-9-]+$",
					message = "must contain alphabetical characters only"),
			@Pattern(regexp = "^....-....-....$", message = "must match ....-....-....")
	})
	private String serialNumber;

	public String getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}
}
