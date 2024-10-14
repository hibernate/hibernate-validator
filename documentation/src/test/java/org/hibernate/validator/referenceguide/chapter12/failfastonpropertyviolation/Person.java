/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
// tag::include[]
package org.hibernate.validator.referenceguide.chapter12.failfastonpropertyviolation;

//end::include[]
import jakarta.validation.constraints.NotNull;

//tag::include[]
public class Person {

	@NotNull
	private String firstName;

	@NotNull
	private String lastName;

	public Person(@NotNull final String firstName, @NotNull final String lastName) {
		this.firstName = firstName;
		this.lastName = lastName;
	}

	//getters, setters, equals and hashcode...
}
//end::include[]
