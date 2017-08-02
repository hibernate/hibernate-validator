//tag::include[]
package org.hibernate.validator.referenceguide.chapter11.cdi.methodvalidation.implicit;

//end::include[]

import java.util.Date;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.validation.constraints.Min;
import javax.validation.executable.ExecutableType;
import javax.validation.executable.ValidateOnExecution;

//tag::include[]
@ApplicationScoped
@ValidateOnExecution(type = ExecutableType.IMPLICIT)
public class ExpressRentalStation implements RentalStation {

	@Override
	public Car rentCar(Customer customer, Date startDate, @Min(1) int durationInDays) {
		//...
		return null;
	}

	@Override
	public List<Car> getAvailableCars() {
		//...
		return null;
	}
}
//end::include[]
