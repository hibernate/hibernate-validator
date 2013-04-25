package org.hibernate.validator.referenceguide.chapter04;

import javax.validation.constraints.NotNull;

public class Car {

	@NotNull(message = "The manufacturer name must not be null")
	private String manufacturer;

	//constructor, getters and setters ...
}
