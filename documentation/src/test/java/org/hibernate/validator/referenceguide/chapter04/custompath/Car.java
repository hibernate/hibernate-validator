package org.hibernate.validator.referenceguide.chapter04.custompath;

import java.util.List;

@ValidPassengerCount
public class Car {

	private final int seatCount;
	private final List<String> passengers;

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
