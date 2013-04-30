package org.hibernate.validator.referenceguide.chapter05;

import javax.validation.constraints.NotNull;

public class Person {

	@NotNull
	private String name;

	public Person(String name) {
		this.name = name;
	}

	// getters and setters ...
}
