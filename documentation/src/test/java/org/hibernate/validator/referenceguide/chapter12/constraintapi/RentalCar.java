/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
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
