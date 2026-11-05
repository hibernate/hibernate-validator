package org.hibernate.validator.test.cdi.internal.methodvalidation.inheritance;

/**
 * @author Hardy Ferentschik
 */
public interface ShipmentServiceFirstInHierarchy extends ShipmentServiceSecondInHierarchy {
	@Override
	Shipment getShipment();
}
