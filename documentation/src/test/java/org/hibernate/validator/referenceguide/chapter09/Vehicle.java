//tag::include[]
package org.hibernate.validator.referenceguide.chapter09;

//end::include[]

import javax.validation.constraints.NotNull;

//tag::include[]
public interface Vehicle {

	public interface Basic {
	}

	@NotNull(groups = Vehicle.Basic.class)
	String getManufacturer();
}
//end::include[]
