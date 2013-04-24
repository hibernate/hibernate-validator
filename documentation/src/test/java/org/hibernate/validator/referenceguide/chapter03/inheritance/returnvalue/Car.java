package org.hibernate.validator.referenceguide.chapter03.inheritance.returnvalue;

import java.util.List;
import javax.validation.constraints.Size;

public class Car implements Vehicle {

	@Override
	@Size(min = 1)
	public List<Person> getPassengers() {
		//...
		return null;
	}
}
