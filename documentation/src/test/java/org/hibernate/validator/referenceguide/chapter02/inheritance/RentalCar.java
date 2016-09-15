//tag::include[]
package org.hibernate.validator.referenceguide.chapter02.inheritance;

//end::include[]

import javax.validation.constraints.NotNull;

//tag::include[]
public class RentalCar extends Car {

	private String rentalStation;

	@NotNull
	public String getRentalStation() {
		return rentalStation;
	}

	//...
}
//end::include[]
