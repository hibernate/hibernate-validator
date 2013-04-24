package org.hibernate.validator.referenceguide.chapter03.inheritance.returnvalue;

import java.util.List;
import javax.validation.constraints.NotNull;

public interface Vehicle {

	@NotNull
	List<Person> getPassengers();
}