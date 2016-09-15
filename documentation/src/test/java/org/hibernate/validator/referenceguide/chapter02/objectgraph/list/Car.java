//tag::include[]
package org.hibernate.validator.referenceguide.chapter02.objectgraph.list;

//end::include[]

import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

//tag::include[]
public class Car {

	@NotNull
	@Valid
	private List<Person> passengers = new ArrayList<Person>();

	//...
}
//end::include[]
