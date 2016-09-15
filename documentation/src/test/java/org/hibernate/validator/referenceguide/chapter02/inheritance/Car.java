//tag::include[]
package org.hibernate.validator.referenceguide.chapter02.inheritance;

//end::include[]

import javax.validation.constraints.NotNull;

//tag::include[]
public class Car {

	private String manufacturer;

	@NotNull
	public String getManufacturer() {
		return manufacturer;
	}

	//...
}
//end::include[]
