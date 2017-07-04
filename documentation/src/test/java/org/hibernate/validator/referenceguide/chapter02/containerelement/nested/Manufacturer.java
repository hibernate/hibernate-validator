//tag::include[]
package org.hibernate.validator.referenceguide.chapter02.containerelement.nested;

//end::include[]

import javax.validation.constraints.NotNull;

//tag::include[]
public class Manufacturer {

	@NotNull
	private String name;

	//...
}
//end::include[]
