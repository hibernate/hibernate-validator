/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.cdi.internal.methodvalidation.inheritance;

import jakarta.validation.executable.ExecutableType;
import jakarta.validation.executable.ValidateOnExecution;

/**
 * @author Gunnar Morling
 */
@ValidateOnExecution(type = ExecutableType.IMPLICIT)
public class ShipmentServiceImpl implements ShipmentServiceFirstInHierarchy {

	@Override
	public Shipment getShipment() {
		return null;
	}
}
