// tag::include[]
package org.hibernate.validator.referenceguide.chapter11.failfast;

//end::include[]
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;

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
