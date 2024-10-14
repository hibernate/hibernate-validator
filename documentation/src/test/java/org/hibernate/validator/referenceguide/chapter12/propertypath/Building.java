/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.referenceguide.chapter12.propertypath;

import java.util.HashSet;
import java.util.Set;

import jakarta.validation.Valid;

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
