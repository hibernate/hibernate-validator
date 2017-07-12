package org.hibernate.validator.referenceguide.chapter08;

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

	public void addCars(List<Car> cars) {
	}
}
