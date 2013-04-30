package org.hibernate.validator.referenceguide.chapter10.cdi.methodvalidation;

import java.util.Date;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

@RequestScoped
public class RentCarRequest {

	@Inject
	private RentalStation rentalStation;

	public void rentCar(String customerId, Date startDate, int duration) {
		//causes ConstraintViolationException
		rentalStation.rentCar( null, null, -1 );
	}
}
