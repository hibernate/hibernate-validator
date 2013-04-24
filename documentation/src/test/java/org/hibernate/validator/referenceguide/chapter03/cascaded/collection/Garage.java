package org.hibernate.validator.referenceguide.chapter03.cascaded.collection;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class Garage {

	public boolean checkCars(@Valid @NotNull List<Car> cars) {
		//...
		return false;
	}
}
