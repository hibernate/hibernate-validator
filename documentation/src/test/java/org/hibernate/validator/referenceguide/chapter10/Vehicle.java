//tag::include[]
package org.hibernate.validator.referenceguide.chapter10;

//end::include[]

import jakarta.validation.constraints.NotNull;

//tag::include[]
public interface Vehicle {

	interface Basic {
	}

	@NotNull(groups = Vehicle.Basic.class)
	String getManufacturer();
}
//end::include[]
