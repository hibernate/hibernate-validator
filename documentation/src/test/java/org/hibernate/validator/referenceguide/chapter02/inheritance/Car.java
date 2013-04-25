package org.hibernate.validator.referenceguide.chapter02.inheritance;

import javax.validation.constraints.NotNull;

public class Car {

	private String manufacturer;

	@NotNull
	public String getManufacturer() {
		return manufacturer;
	}

	//...
}
