/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
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
