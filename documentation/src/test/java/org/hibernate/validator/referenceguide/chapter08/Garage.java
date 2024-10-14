/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.referenceguide.chapter08;

import java.util.List;

import org.hibernate.validator.referenceguide.chapter03.crossparameter.constrainttarget.Car;

public class Garage {

	public Car buildCar(List<Part> parts) {
		//...
		return null;
	}

	public Car paintCar(int color) {
		//...
		return null;
	}
}
