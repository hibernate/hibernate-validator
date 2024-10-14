/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.methodvalidation.returnvaluevalidation;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

/**
 * @author Hardy Ferentschik
 */
@AtLeastOneContactProvided
public class ContactBean {

	@Email(regexp = "[a-zA-Z0-9._%-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}")
	private String email;

	@Pattern(regexp = "[0-9]{3,9}")
	private String phone;

	public String getEmail() {
		return email;
	}

	public void setEmail(final String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(final String phone) {
		this.phone = phone;
	}
}
