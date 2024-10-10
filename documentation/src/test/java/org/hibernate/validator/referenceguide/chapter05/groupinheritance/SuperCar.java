/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
//tag::include[]
package org.hibernate.validator.referenceguide.chapter05.groupinheritance;

//end::include[]
import jakarta.validation.constraints.AssertTrue;

import org.hibernate.validator.referenceguide.chapter05.Car;

//tag::include[]
public class SuperCar extends Car {

	@AssertTrue(
			message = "Race car must have a safety belt",
			groups = RaceCarChecks.class
	)
	private boolean safetyBelt;

	// getters and setters ...

	//end::include[]

	public SuperCar(String manufacturer, String licencePlate, int seatCount) {
		super( manufacturer, licencePlate, seatCount );
	}

	public boolean isSafetyBelt() {
		return safetyBelt;
	}

	public void setSafetyBelt(boolean safetyBelt) {
		this.safetyBelt = safetyBelt;
	}

	//tag::include[]
}
//end::include[]
