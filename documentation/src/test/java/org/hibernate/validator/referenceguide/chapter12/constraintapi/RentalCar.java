/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.referenceguide.chapter12.constraintapi;

public class RentalCar extends Car {

	private String rentalStation;

	public RentalCar(String manufacturer) {
		super( manufacturer );
	}

	public String getRentalStation() {
		return rentalStation;
	}
}
