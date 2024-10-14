/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
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
