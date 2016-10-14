//tag::include[]
package org.hibernate.validator.referenceguide.chapter05.groupinheritance;

//end::include[]
import javax.validation.constraints.AssertTrue;

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
