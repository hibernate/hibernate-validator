/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.bootstrap;

import jakarta.validation.constraints.NotNull;

/**
 * @author Hardy Ferentschik
 */
public class Order {
	@NotNull
	Integer orderNumber;

	public Integer getOrderNumber() {
		return orderNumber;
	}

	public void setOrderNumber(Integer orderNumber) {
		this.orderNumber = orderNumber;
	}
}
