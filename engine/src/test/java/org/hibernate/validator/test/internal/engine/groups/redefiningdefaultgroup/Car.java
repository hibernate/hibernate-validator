/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.groups.redefiningdefaultgroup;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * An example entity class enriched with constraint annotations from
 * the Bean Validation API (<a href="http://jcp.org/en/jsr/detail?id=380">JSR
 * 380</a>). Have a look at {@link org.hibernate.validator.quickstart.CarTest} to learn, how the Bean Validation
 * API can be used to validate {@code Car} instances.
 *
 * @author Gunnar Morling
 * @author Hardy Ferentschik
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 */
public class Car {

	//The definition of the message in the constraints is just for testing purpose.
	//In a real world scenario you would place your messages into resource bundles.

	/**
	 * By annotating the field with @NotNull we specify, that null is not a valid
	 * value.
	 */
	@NotNull(message = "must not be null")
	private String manufacturer;

	/**
	 * This String field shall not only not allowed to be null, it shall also between
	 * 2 and 14 characters long.
	 */
	@NotNull
	@Size(min = 2, max = 14, message = "size must be between {min} and {max}")
	private String licensePlate;

	/**
	 * This int field shall have a value of at least 2.
	 */
	@Min(value = 2, message = "must be greater than or equal to {value}")
	private int seatCount;

	@AssertTrue(message = "The car has to pass the vehicle inspection first", groups = CarChecks.class)
	private boolean passedVehicleInspection;

	@Valid
	private Driver driver;

	public Car(String manufacturer, String licencePlate, int seatCount) {
		this.manufacturer = manufacturer;
		this.licensePlate = licencePlate;
		this.seatCount = seatCount;
	}

	public String getManufacturer() {
		return manufacturer;
	}

	public void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}

	public String getLicensePlate() {
		return licensePlate;
	}

	public void setLicensePlate(String licensePlate) {
		this.licensePlate = licensePlate;
	}

	public int getSeatCount() {
		return seatCount;
	}

	public void setSeatCount(int seatCount) {
		this.seatCount = seatCount;
	}

	public boolean getPassedVehicleInspection() {
		return passedVehicleInspection;
	}

	public void setPassedVehicleInspection(boolean passed) {
		this.passedVehicleInspection = passed;
	}

	public Driver getDriver() {
		return driver;
	}

	public void setDriver(Driver driver) {
		this.driver = driver;
	}
}
