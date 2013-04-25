package org.hibernate.validator.referenceguide.chapter02.inheritance;

import javax.validation.constraints.NotNull;

public class RentalCar extends Car {

	private String rentalStation;

	@NotNull
	public String getRentalStation() {
		return rentalStation;
	}

	//...
}
