//tag::include[]
package org.hibernate.validator.referenceguide.chapter02.objectgraph.containerelement;

//end::include[]

import jakarta.validation.constraints.NotNull;

//tag::include[]
public class Person {

	@NotNull
	private String name;

	//...
}
//end::include[]
