//tag::include[]
package org.hibernate.validator.referenceguide.chapter03.inheritance.parallel;

//end::include[]

//tag::include[]
public class RacingCar implements Car, Vehicle {

	@Override
	public void drive(int speedInMph) {
		//...
	}
}
//end::include[]
