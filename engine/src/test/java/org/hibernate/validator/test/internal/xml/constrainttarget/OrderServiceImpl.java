/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.xml.constrainttarget;

/**
 * @author Gunnar Morling
 */
public class OrderServiceImpl implements OrderService {

	@Override
	public int getNumberOfOrders(int customerId, boolean someFlag) {
		return 1;
	}

	@Override
	public int placeOrder(int customerId, String item, int quantity) {
		return 1;
	}

	@Override
	public int cancelOrder(int orderId, String reason) {
		return 1;
	}
}
