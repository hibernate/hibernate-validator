package org.hibernate.validator.referenceguide.chapter03.groups.groupsequenceprovider;

import javax.validation.constraints.AssertFalse;

import org.hibernate.validator.group.GroupSequenceProvider;
import org.hibernate.validator.referenceguide.chapter03.groups.Car;
import org.hibernate.validator.referenceguide.chapter03.groups.RentalChecks;

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


