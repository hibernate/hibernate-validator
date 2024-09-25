/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
//tag::include[]
package org.hibernate.validator.referenceguide.chapter03.cascaded;

//end::include[]
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

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
