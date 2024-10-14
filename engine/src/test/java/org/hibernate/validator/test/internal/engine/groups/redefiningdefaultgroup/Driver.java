/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.groups.redefiningdefaultgroup;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;

/**
 * @author Hardy Ferentschik
 */
public class Driver extends Person {
	@Min(value = 18, message = "You have to be 18 to drive a car", groups = DriverChecks.class)
	private int age;

	@AssertTrue(message = "You first have to pass the driving test", groups = DriverChecks.class)
	private boolean hasDrivingLicense;

	public Driver(String name) {
		super( name );
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
}
