//tag::include[]
package org.hibernate.validator.referenceguide.chapter05.groupconversion;

//end::include[]

import jakarta.validation.GroupSequence;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.groups.ConvertGroup;
import jakarta.validation.groups.Default;

//tag::include[]
@GroupSequence({ CarChecks.class, Car.class })
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
	@ConvertGroup(from = Default.class, to = DriverChecks.class)
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
