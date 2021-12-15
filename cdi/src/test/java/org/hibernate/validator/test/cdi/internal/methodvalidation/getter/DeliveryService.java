/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.cdi.internal.methodvalidation.getter;

import javax.validation.constraints.NotNull;
import javax.validation.executable.ExecutableType;
import javax.validation.executable.ValidateOnExecution;

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
