/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
//tag::include[]
package org.hibernate.validator.referenceguide.chapter03.validation;

//end::include[]
import java.util.Collections;
import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

//tag::include[]
public class Car {

	public Car(@NotNull String manufacturer) {
		//...
	}

	@ValidRacingCar
	public Car(String manufacturer, String team) {
		//...
	}

	public void drive(@Max(75) int speedInMph) {
		//...
	}

	@Size(min = 1)
	public List<Passenger> getPassengers() {
		//...
		return Collections.emptyList();
	}
}
//end::include[]
