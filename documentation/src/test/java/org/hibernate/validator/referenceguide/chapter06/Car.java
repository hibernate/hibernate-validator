//tag::include[]
package org.hibernate.validator.referenceguide.chapter06;

//end::include[]

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

//tag::include[]
public class Car {

	@NotNull
	private String manufacturer;

	@NotNull
	@Size(min = 2, max = 14)
	@CheckCase(CaseMode.UPPER)
	private String licensePlate;

	@Min(2)
	private int seatCount;

	public Car(String manufacturer, String licencePlate, int seatCount) {
		this.manufacturer = manufacturer;
		this.licensePlate = licencePlate;
		this.seatCount = seatCount;
	}

	//getters and setters ...
}
//end::include[]
