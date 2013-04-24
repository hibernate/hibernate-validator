package org.hibernate.validator.referenceguide.chapter03.validation;

import java.util.Collections;
import java.util.List;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

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
