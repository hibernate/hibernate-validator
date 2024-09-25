/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
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
