/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.referenceguide.chapter06.custompath;

import java.util.List;

@ValidPassengerCount
public class Car {

	private int seatCount;
	private List<String> passengers;

	public Car(int seatCount, List<String> passengers) {
		this.seatCount = seatCount;
		this.passengers = passengers;
	}

	public int getSeatCount() {
		return seatCount;
	}

	public List<String> getPassengers() {
		return passengers;
	}
}
