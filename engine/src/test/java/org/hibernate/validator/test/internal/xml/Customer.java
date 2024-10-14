/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.xml;

/**
 * @author Hardy Ferentschik
 */
public class Customer implements Person {

	private String firstName;
	private String middleName;
	private String lastName;

	private String customerId;

	private String password;

	@Override
	public String getFirstName() {
		return this.firstName;
	}

	public void setFirstName(final String firstName) {
		this.firstName = firstName;
	}

	@Override
	public String getMiddleName() {
		return this.middleName;
	}

	public void setMiddleName(final String middleName) {
		this.middleName = middleName;
	}

	@Override
	public String getLastName() {
		return this.lastName;
	}

	public void setLastName(final String lastName) {
		this.lastName = lastName;
	}

	public String getCustomerId() {
		return this.customerId;
	}

	public void setCustomerId(final String customerId) {
		this.customerId = customerId;
	}

	public String getPassword() {
		return this.password;
	}

	public void setPassword(final String password) {
		this.password = password;
	}
}
