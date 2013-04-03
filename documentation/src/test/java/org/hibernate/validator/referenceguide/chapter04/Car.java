package org.hibernate.validator.referenceguide.chapter04;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class Car {

	@NotNull
	private final String manufacturer;

	@NotNull
	@Size(min = 2, max = 14)
	@CheckCase(CaseMode.UPPER)
	private final String licensePlate;

	@Min(2)
	private final int seatCount;

	public Car ( String manufacturer, String licencePlate, int seatCount ) {
		this.manufacturer = manufacturer;
		this.licensePlate = licencePlate;
		this.seatCount = seatCount;
	}

	//getters and setters ...
}
