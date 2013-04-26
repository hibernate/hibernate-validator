package org.hibernate.validator.referenceguide.chapter11.failfast;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;

public class Car {

	@NotNull
	private String manufacturer;

	@AssertTrue
	private boolean isRegistered;

	public Car(String manufacturer, boolean isRegistered) {
		this.manufacturer = manufacturer;
		this.isRegistered = isRegistered;
	}

	//getters and setters...
}