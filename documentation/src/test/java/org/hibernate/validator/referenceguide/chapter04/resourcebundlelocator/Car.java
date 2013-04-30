package org.hibernate.validator.referenceguide.chapter04.resourcebundlelocator;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;

public class Car {

	@NotNull
	private String licensePlate;

	@Max(300)
	private int topSpeed = 400;

}
