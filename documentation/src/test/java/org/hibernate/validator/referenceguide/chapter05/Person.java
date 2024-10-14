/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
//tag::include[]
package org.hibernate.validator.referenceguide.chapter05;

//end::include[]
import jakarta.validation.constraints.NotNull;

//tag::include[]
public class Person {

	@NotNull
	private String name;

	public Person(String name) {
		this.name = name;
	}

	// getters and setters ...
}
//end::include[]
