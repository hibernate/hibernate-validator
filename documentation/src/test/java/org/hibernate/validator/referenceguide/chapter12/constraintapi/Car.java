/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.referenceguide.chapter12.constraintapi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Car {

	private String manufacturer;

	private String licensePlate;

	private Person driver;

	private Map<Part, List<Manufacturer>> partManufacturers = new HashMap<>();

	public Car(String manufacturer) {
	}

	public void drive(int speedInMph) {
	}

	public Person getDriver() {
		return null;
	}

	public void load(List<Person> passengers, List<String> luggage) {
		//...
	}
}
