/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
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
