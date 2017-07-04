//tag::include[]
package org.hibernate.validator.referenceguide.chapter02.containerelement.optional;

//end::include[]

import java.util.Optional;

//tag::include[]
public class Car {

	private Optional<@MinTowingCapacity(1000) Integer> towingCapacity = Optional.empty();

	public void setTowingCapacity(Integer alias) {
		towingCapacity = Optional.of( alias );
	}

	//...

}
//end::include[]
