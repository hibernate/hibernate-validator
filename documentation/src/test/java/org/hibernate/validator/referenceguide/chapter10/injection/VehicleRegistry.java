package org.hibernate.validator.referenceguide.chapter10.injection;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class VehicleRegistry {

	public boolean isValidLicensePlate(String licensePlate) {
		return true;
	}
}
