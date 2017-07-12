package org.hibernate.validator.referenceguide.chapter12.valuehandling.graph;

import java.util.Optional;

import javax.validation.Valid;

public class Container {

	//tag::include[]
	@Valid
	private Optional<Person> person = Optional.of( new Person() );
	//end::include[]

}
