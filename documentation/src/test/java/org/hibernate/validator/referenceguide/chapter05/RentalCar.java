//tag::include[]
package org.hibernate.validator.referenceguide.chapter05;

//end::include[]

import javax.validation.GroupSequence;
import javax.validation.constraints.AssertFalse;

//tag::include[]
@GroupSequence({ RentalChecks.class, CarChecks.class, RentalCar.class })
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
