package org.hibernate.validator.referenceguide.chapter07;

import javax.validation.constraints.NotNull;

public interface Vehicle {

	public interface Basic {
	}

	@NotNull(groups = Vehicle.Basic.class)
	String getManufacturer();
}
