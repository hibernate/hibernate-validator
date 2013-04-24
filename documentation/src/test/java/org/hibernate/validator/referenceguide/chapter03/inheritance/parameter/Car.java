package org.hibernate.validator.referenceguide.chapter03.inheritance.parameter;

import javax.validation.constraints.Max;

public class Car implements Vehicle {

	@Override
	public void drive(@Max(55) int speedInMph) {
		//...
	}
}
