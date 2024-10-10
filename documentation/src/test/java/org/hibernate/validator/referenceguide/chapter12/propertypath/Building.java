/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
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
