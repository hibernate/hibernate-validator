package org.hibernate.validator.referenceguide.chapter11.cdi.injection;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class VehicleRegistry {

	public boolean isValidLicensePlate(String licensePlate) {
		return true;
	}
}
