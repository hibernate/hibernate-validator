/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.cdi.internal.methodvalidation.inheritance;

import javax.validation.constraints.NotNull;
import javax.validation.executable.ExecutableType;
import javax.validation.executable.ValidateOnExecution;

/**
 * @author Gunnar Morling
 */
public interface ShipmentServiceSecondInHierarchy {
	@NotNull
	@ValidateOnExecution(type = ExecutableType.ALL)
	Shipment getShipment();
}
