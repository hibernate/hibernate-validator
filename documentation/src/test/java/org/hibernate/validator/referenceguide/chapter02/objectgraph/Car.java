package org.hibernate.validator.referenceguide.chapter02.objectgraph;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class Car {

	@NotNull
	@Valid
	private Person driver;

	//...
}