/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.cdi.internal.methodvalidation.inheritance;

/**
 * @author Hardy Ferentschik
 */
public interface ShipmentServiceFirstInHierarchy extends ShipmentServiceSecondInHierarchy {
	@Override
	Shipment getShipment();
}
