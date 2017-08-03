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
