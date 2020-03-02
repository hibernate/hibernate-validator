//tag::include[]
package org.hibernate.validator.referenceguide.chapter03.cascaded;

//end::include[]

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

//tag::include[]
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
//end::include[]
