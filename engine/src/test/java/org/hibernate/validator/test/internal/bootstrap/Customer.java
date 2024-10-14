/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.bootstrap;

import java.util.HashSet;
import java.util.Set;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * @author Hardy Ferentschik
 */
public class Customer {
	@NotEmpty
	private String firstName;
	private String middleName;
	@NotEmpty
	private String lastName;

	@Valid
	private Set<Order> orders = new HashSet<Order>();

	public void addOrder(@NotNull Order order) {
		orders.add( order );
	}

	public Set<Order> getOrders() {
		return orders;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getMiddleName() {
		return middleName;
	}

	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
}
