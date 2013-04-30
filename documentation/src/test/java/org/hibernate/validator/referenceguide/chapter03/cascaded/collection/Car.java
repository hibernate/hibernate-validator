package org.hibernate.validator.referenceguide.chapter03.cascaded.collection;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

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
