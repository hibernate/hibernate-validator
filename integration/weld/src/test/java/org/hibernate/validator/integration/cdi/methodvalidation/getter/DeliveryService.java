package org.hibernate.validator.integration.cdi.methodvalidation.getter;

import javax.validation.constraints.NotNull;
import javax.validation.executable.ValidateOnExecution;

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


