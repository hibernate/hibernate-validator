package org.hibernate.validator.referenceguide.chapter05.groupconversion;

import javax.validation.GroupSequence;
import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.validation.groups.ConvertGroup;
import javax.validation.groups.Default;

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
