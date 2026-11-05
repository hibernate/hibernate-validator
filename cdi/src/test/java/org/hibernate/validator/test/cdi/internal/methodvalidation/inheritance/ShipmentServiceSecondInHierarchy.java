package org.hibernate.validator.test.cdi.internal.methodvalidation.inheritance;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.executable.ExecutableType;
import jakarta.validation.executable.ValidateOnExecution;

/**
 * @author Gunnar Morling
 */
public interface ShipmentServiceSecondInHierarchy {
	@NotNull
	@ValidateOnExecution(type = ExecutableType.ALL)
	Shipment getShipment();
}
