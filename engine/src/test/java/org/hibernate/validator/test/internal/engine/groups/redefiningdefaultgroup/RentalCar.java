/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.groups.redefiningdefaultgroup;

import jakarta.validation.GroupSequence;

/**
 * @author Hardy Ferentschik
 */
@GroupSequence({ RentalCar.class, CarChecks.class })
public class RentalCar extends Car {
	public RentalCar(String manufacturer, String licencePlate, int seatCount) {
		super( manufacturer, licencePlate, seatCount );
	}
}
