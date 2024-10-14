/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.referenceguide.chapter11.cdi.injection;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class VehicleRegistry {

	public boolean isValidLicensePlate(String licensePlate) {
		return true;
	}
}
