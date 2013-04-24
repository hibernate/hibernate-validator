package org.hibernate.validator.referenceguide.chapter03.returnvalue;

import java.util.List;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class RentalStation {

	@ValidRentalStation
	public RentalStation() {
		//...
	}

	@NotNull
	@Size(min = 1)
	public List<Customer> getCustomers() {
		//...
		return null;
	}
}