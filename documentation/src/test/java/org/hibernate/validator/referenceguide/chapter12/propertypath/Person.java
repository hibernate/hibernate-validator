package org.hibernate.validator.referenceguide.chapter12.propertypath;

import javax.validation.constraints.Size;

public class Person {

	@Size(min = 5)
	String name;

	Person(String name) {
		this.name = name;
	}
}
