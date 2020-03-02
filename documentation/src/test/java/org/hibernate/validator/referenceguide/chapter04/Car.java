//tag::include[]
package org.hibernate.validator.referenceguide.chapter04;

//end::include[]

import jakarta.validation.constraints.NotNull;

//tag::include[]
public class Car {

	@NotNull(message = "The manufacturer name must not be null")
	private String manufacturer;

	//constructor, getters and setters ...
}
//end::include[]
