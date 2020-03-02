//tag::include[]
package org.hibernate.validator.referenceguide.chapter03.inheritance.parameter;

//end::include[]

import jakarta.validation.constraints.Max;

//tag::include[]
public class Car implements Vehicle {

	@Override
	public void drive(@Max(55) int speedInMph) {
		//...
	}
}
//end::include[]
