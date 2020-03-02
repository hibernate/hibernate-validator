/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.cdi.internal.methodvalidation.getter;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.executable.ValidateOnExecution;

/**
 * @author Hardy Ferentschik
 */
@ValidateOnExecution
public class DeliveryService {

	@ValidateOnExecution
	public void findDelivery(@NotNull String id) {
	}

	@ValidateOnExecution
	@NotNull
	public Delivery getDelivery() {
		return null;
	}

	@NotNull
	public Delivery getAnotherDelivery() {
		return null;
	}
}
