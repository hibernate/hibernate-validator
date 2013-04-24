package org.hibernate.validator.referenceguide.chapter03.inheritance.parallel;

public class RacingCar implements Car, Vehicle {

	@Override
	public void drive(int speedInMph) {
		//...
	}
}
