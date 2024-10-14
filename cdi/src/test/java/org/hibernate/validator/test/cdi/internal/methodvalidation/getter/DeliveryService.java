/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.cdi.internal.methodvalidation.getter;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.executable.ExecutableType;
import jakarta.validation.executable.ValidateOnExecution;

/**
 * @author Hardy Ferentschik
 */
@ValidateOnExecution
public class DeliveryService {

	// This is not what we want to do but it works around what could possibly be a bug in the JDK 17
	@ValidateOnExecution(type = ExecutableType.NONE)
	public void findDelivery(@NotNull String id) {
	}

	// This is not what we want to do but it works around what could possibly be a bug in the JDK 17
	@ValidateOnExecution(type = ExecutableType.NONE)
	@NotNull
	public Delivery getDelivery() {
		return null;
	}

	@NotNull
	public Delivery getAnotherDelivery() {
		return null;
	}
}
