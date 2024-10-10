/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
//tag::include[]
package org.hibernate.validator.referenceguide.chapter05.groupconversion;

//end::include[]
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

//tag::include[]
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
//end::include[]
