/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
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
