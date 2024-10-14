/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.metadata;

import jakarta.validation.constraints.NotNull;

/**
 * @author Hardy Ferentschik
 */
public class Order {

	public interface OrderBasic {
	}

	public interface OrderComplex {
	}

	@NotNull(message = "Order number must be specified")
	Integer orderNumber;

	public Integer getOrderNumber() {
		return orderNumber;
	}

	public void setOrderNumber(Integer orderNumber) {
		this.orderNumber = orderNumber;
	}
}
