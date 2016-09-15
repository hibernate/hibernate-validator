package org.hibernate.validator.referenceguide.chapter06.crossparameter;

import java.util.List;
import javax.validation.ConstraintTarget;

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
