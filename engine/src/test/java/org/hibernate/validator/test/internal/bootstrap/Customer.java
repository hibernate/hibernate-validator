/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.bootstrap;

import java.util.HashSet;
import java.util.Set;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

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
