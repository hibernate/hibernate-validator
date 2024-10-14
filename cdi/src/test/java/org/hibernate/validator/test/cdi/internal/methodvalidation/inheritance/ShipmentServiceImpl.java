/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
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
