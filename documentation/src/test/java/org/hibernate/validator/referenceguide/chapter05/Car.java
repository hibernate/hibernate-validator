/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
//tag::include[]
package org.hibernate.validator.referenceguide.chapter05;

//end::include[]
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

//tag::include[]
public class Car {
	@NotNull
	private String manufacturer;

	@NotNull
	@Size(min = 2, max = 14)
	private String licensePlate;

	@Min(2)
	private int seatCount;

	@AssertTrue(
			message = "The car has to pass the vehicle inspection first",
			groups = CarChecks.class
	)
	private boolean passedVehicleInspection;

	@Valid
	private Driver driver;

	public Car(String manufacturer, String licencePlate, int seatCount) {
		this.manufacturer = manufacturer;
		this.licensePlate = licencePlate;
		this.seatCount = seatCount;
	}

	public boolean isPassedVehicleInspection() {
		return passedVehicleInspection;
	}

	public void setPassedVehicleInspection(boolean passedVehicleInspection) {
		this.passedVehicleInspection = passedVehicleInspection;
	}

	public Driver getDriver() {
		return driver;
	}

	public void setDriver(Driver driver) {
		this.driver = driver;
	}

	// getters and setters ...
}
//end::include[]
