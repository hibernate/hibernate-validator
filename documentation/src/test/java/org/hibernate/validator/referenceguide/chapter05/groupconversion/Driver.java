package org.hibernate.validator.referenceguide.chapter05.groupconversion;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class Driver {

	@NotNull
	private String name;

	@Min(
			value = 18,
			message = "You have to be 18 to drive a car",
			groups = DriverChecks.class
	)
	public int age;

	@AssertTrue(
			message = "You first have to pass the driving test",
			groups = DriverChecks.class
	)
	public boolean hasDrivingLicense;

	public Driver(String name) {
		this.name = name;
	}

	public void passedDrivingTest(boolean b) {
		hasDrivingLicense = b;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	// getters and setters ...
}
