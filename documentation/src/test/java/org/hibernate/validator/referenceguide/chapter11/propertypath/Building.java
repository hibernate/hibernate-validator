package org.hibernate.validator.referenceguide.chapter11.propertypath;

import java.util.HashSet;
import java.util.Set;

import javax.validation.Valid;

public class Building {

	@Valid
	private Set<Apartment> apartments = new HashSet<Apartment>();

	public Set<Apartment> getApartments() {
		return apartments;
	}

	public void setApartments(Set<Apartment> apartments) {
		this.apartments = apartments;
	}
}
