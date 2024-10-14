/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.xml.constrainttarget;

/**
 * @author Gunnar Morling
 */
public interface OrderService {

	int getNumberOfOrders(int customerId, boolean someFlag);

	int placeOrder(int customerId, String item, int quantity);

	int cancelOrder(int orderId, String reason);
}
