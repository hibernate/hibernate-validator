/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.metadata;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.groups.ConvertGroup;

import org.hibernate.validator.test.internal.metadata.Order.OrderBasic;
import org.hibernate.validator.test.internal.metadata.Order.OrderComplex;

/**
 * @author Hardy Ferentschik
 */
public class Customer implements Person {

	public interface CustomerBasic {
	}

	public interface CustomerComplex {
	}

	public interface CustomerGetterBasic {
	}

	public interface CustomerGetterComplex {
	}


	private String firstName;
	private String middleName;
	private String lastName;

	@Valid
	@ConvertGroup.List({
			@ConvertGroup(from = CustomerBasic.class, to = OrderBasic.class),
			@ConvertGroup(from = CustomerComplex.class, to = OrderComplex.class)
	})
	private final List<Order> orderList = new ArrayList<Order>();

	public void addOrder(Order order) {
		orderList.add( order );
	}

	public List<Order> getOrderList() {
		return orderList;
	}

	@Override
	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	@Override
	public String getMiddleName() {
		return middleName;
	}

	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

	@Override
	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	@Valid
	@ConvertGroup.List({
			@ConvertGroup(from = CustomerGetterBasic.class, to = OrderBasic.class),
			@ConvertGroup(from = CustomerGetterComplex.class, to = OrderComplex.class)
	})
	public Order getLastOrder() {
		return null;
	}
}
