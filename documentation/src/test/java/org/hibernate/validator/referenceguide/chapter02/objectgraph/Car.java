//tag::include[]
package org.hibernate.validator.referenceguide.chapter02.objectgraph;

//end::include[]

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

//tag::include[]
public class Car {

	@NotNull
	@Valid
	private Person driver;

	//...
}
//end::include[]

