package org.hibernate.validator.referenceguide.chapter03.cascaded.containerelement;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class Car {

	@NotNull
	private String manufacturer;

	@NotNull
	@Size(min = 2, max = 14)
	private String licensePlate;

	public Car(String manufacturer, String licencePlate) {
		this.manufacturer = manufacturer;
		this.licensePlate = licencePlate;
	}

	//getters and setters ...
}
