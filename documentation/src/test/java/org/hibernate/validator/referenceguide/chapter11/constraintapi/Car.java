package org.hibernate.validator.referenceguide.chapter11.constraintapi;

import java.util.List;

public class Car {

	private String manufacturer;

	private String licensePlate;

	private Person driver;

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
