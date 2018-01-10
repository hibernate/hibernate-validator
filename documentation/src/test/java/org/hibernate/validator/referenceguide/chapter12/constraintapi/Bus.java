package org.hibernate.validator.referenceguide.chapter12.constraintapi;

import java.util.ArrayList;
import java.util.List;

@ValidPassengerCount
public class Bus {
	private final int seatCount;
	private final List<Person> passengers;

	public Bus(int seatCount) {
		this.seatCount = seatCount;
		this.passengers = new ArrayList<Person>( seatCount );
	}

	public int getSeatCount() {
		return seatCount;
	}

	public List<Person> getPassengers() {
		return passengers;
	}

	public void addPassenger(Person passenger) {
		passengers.add( passenger );
	}
}

