//tag::include[]
package org.hibernate.validator.referenceguide.chapter12.constraintapi;

//end::include[]

import javax.validation.constraints.NotNull;

//tag::include[]
public class Manufacturer {

	@NotNull
	private String name;

	//...
}
//end::include[]
