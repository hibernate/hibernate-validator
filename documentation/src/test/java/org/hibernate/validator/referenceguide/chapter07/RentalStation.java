package org.hibernate.validator.referenceguide.chapter07;

import java.util.Date;
import java.util.List;

public class RentalStation {

	public RentalStation() {
	}

	public RentalStation(String name) {
	}

	public List<Customer> getCustomers() {
		return null;
	}

	public void rentCar(Customer customer, Date startDate, int durationInDays) {
	}
}