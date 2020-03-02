//tag::include[]
package org.hibernate.validator.referenceguide.chapter03.returnvalue;

//end::include[]

import java.util.List;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

//tag::include[]
public class RentalStation {

	@ValidRentalStation
	public RentalStation() {
		//...
	}

	@NotNull
	@Size(min = 1)
	public List<@NotNull Customer> getCustomers() {
		//...
		return null;
	}
}
//end::include[]
