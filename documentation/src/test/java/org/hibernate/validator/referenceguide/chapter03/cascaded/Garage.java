//tag::include[]
package org.hibernate.validator.referenceguide.chapter03.cascaded;

//end::include[]

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

//tag::include[]
public class Garage {

	@NotNull
	private String name;

	@Valid
	public Garage(String name) {
		this.name = name;
	}

	public boolean checkCar(@Valid @NotNull Car car) {
		//...
		return false;
	}
}
//end::include[]
