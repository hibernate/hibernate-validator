/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
//tag::include[]
package org.hibernate.validator.referenceguide.chapter05.groupsequenceprovider;

//end::include[]
import jakarta.validation.constraints.AssertFalse;

import org.hibernate.validator.group.GroupSequenceProvider;
import org.hibernate.validator.referenceguide.chapter05.Car;
import org.hibernate.validator.referenceguide.chapter05.RentalChecks;

//tag::include[]
@GroupSequenceProvider(RentalCarGroupSequenceProvider.class)
public class RentalCar extends Car {

	@AssertFalse(message = "The car is currently rented out", groups = RentalChecks.class)
	private boolean rented;

	public RentalCar(String manufacturer, String licencePlate, int seatCount) {
		super( manufacturer, licencePlate, seatCount );
	}

	public boolean isRented() {
		return rented;
	}

	public void setRented(boolean rented) {
		this.rented = rented;
	}
}
//end::include[]
