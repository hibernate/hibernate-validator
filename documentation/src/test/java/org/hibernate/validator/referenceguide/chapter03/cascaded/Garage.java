package org.hibernate.validator.referenceguide.chapter03.cascaded;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class Garage {

	@NotNull
	private String name;

	@Valid
	public Garage(String name) {
		this.name = name;
	}

	public boolean checkCar(@Valid @NotNull Car car) {
		//...
		return false;
	}
}
