//tag::include[]
package org.hibernate.validator.referenceguide.chapter11.cdi.methodvalidation;

//end::include[]

import java.util.Date;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

//tag::include[]
@RequestScoped
public class RentCarRequest {

	@Inject
	private RentalStation rentalStation;

	public void rentCar(String customerId, Date startDate, int duration) {
		//causes ConstraintViolationException
		rentalStation.rentCar( null, null, -1 );
	}
}
//end::include[]
