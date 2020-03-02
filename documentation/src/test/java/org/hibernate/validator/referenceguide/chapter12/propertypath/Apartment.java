package org.hibernate.validator.referenceguide.chapter12.propertypath;

import jakarta.validation.Valid;

public class Apartment {

	@Valid
	Person resident;

	Apartment(Person resident) {
		this.resident = resident;
	}
}
