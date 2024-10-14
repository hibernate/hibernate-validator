/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
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
