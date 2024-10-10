/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
//tag::include[]
package org.hibernate.validator.referenceguide.chapter02.fieldlevel;

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
