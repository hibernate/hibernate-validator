/**
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hibernate.validator.test.internal.engine.groups.redefiningdefaultgroup;

import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * An example entity class enriched with constraint annotations from
 * the Bean Validation API (<a href="http://jcp.org/en/jsr/detail?id=303">JSR
 * 303</a>). Have a look at {@link org.hibernate.validator.quickstart.CarTest} to learn, how the Bean Validation
 * API can be used to validate {@code Car} instances.
 *
 * @author Gunnar Morling
 * @author Hardy Ferentschik
 * @author Kevin Pollet <kevin.pollet@serli.com> (C) 2011 SERLI
 */
public class Car {

	//The definition of the message in the constraints is just for testing purpose.
	//In a real world scenario you would place your messages into resource bundles.

	/**
	 * By annotating the field with @NotNull we specify, that null is not a valid
	 * value.
	 */
	@NotNull(message = "may not be null")
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
