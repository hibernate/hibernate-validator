/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.referenceguide.chapter06.crossparameter;

import java.util.List;

import jakarta.validation.ConstraintTarget;

public class ScriptAssertTest {

	//tag::buildCar[]
	@ScriptAssert(script = "arg1.size() <= arg0", validationAppliesTo = ConstraintTarget.PARAMETERS)
	public Car buildCar(int seatCount, List<Passenger> passengers) {
		//...
		return null;
	}
	//end::buildCar[]

	private static class Car {
	}

	private static class Passenger {
	}
}
