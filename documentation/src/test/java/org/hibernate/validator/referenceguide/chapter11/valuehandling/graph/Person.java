package org.hibernate.validator.referenceguide.chapter11.valuehandling.graph;

import javax.validation.constraints.Size;

//tag::include[]
public class Person {

	@Size(min = 3)
	private String name = "Bob";

}
//end::include[]
