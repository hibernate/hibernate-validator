package org.hibernate.validator.referenceguide.chapter11.propertypath;

import javax.validation.Valid;

public class Apartment {

	@Valid
	Person resident;

	Apartment(Person resident) {
		this.resident = resident;
	}
}
