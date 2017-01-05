/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.cascaded;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Email;

class EmailAddress {

	@NotNull
	@Email
	private final String email;

	public EmailAddress(String value) {
		this.email = value;
	}

	@Override
	public String toString() {
		return "EmailAddress [email=" + email + "]";
	}
}
