/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.constraints;

import org.hibernate.validator.constraints.Range;

/**
 * @author Hardy Ferentschik
 */
public class Elevator {

	@Range(min = -2, max = 50, message = "Invalid floor")
	private int currentFloor;

	public int getCurrentFloor() {
		return currentFloor;
	}

	public void setCurrentFloor(int currentFloor) {
		this.currentFloor = currentFloor;
	}
}
