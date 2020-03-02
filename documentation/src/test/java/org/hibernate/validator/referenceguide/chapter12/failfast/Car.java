// tag::include[]
package org.hibernate.validator.referenceguide.chapter12.failfast;

//end::include[]
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

//tag::include[]
public class Car {

	@NotNull
	private String manufacturer;

	@AssertTrue
	private boolean isRegistered;

	public Car(String manufacturer, boolean isRegistered) {
		this.manufacturer = manufacturer;
		this.isRegistered = isRegistered;
	}

	//getters and setters...
}
//end::include[]
