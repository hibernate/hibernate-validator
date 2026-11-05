package org.hibernate.validator.referenceguide.chapter12.nodenameprovider;

import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

//tag::include[]
public class Person {
	@NotNull
	@JsonProperty("first_name")
	private final String firstName;

	@JsonProperty("last_name")
	private final String lastName;

	public Person(String firstName, String lastName) {
		this.firstName = firstName;
		this.lastName = lastName;
	}
}
//end::include[]
