package org.hibernate.validator.referenceguide.chapter02.objectgraph.list;

import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class Car {

	@NotNull
	@Valid
	private List<Person> passengers = new ArrayList<Person>();

	//...
}