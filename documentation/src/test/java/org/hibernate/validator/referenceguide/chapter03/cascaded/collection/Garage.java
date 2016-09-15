//tag::include[]
package org.hibernate.validator.referenceguide.chapter03.cascaded.collection;

//end::include[]

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

//tag::include[]
public class Garage {

	public boolean checkCars(@Valid @NotNull List<Car> cars) {
		//...
		return false;
	}
}
//end::include[]
