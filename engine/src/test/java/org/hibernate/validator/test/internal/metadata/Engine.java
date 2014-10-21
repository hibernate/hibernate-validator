/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.metadata;

import javax.validation.constraints.Pattern;


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
