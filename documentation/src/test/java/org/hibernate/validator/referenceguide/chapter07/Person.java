package org.hibernate.validator.referenceguide.chapter07;

import javax.validation.constraints.NotNull;

public class Person {

	public interface Basic {
	}

	@NotNull
	private String name;

	//getters and setters ...
}
