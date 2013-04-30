package org.hibernate.validator.referenceguide.chapter10.cdi.methodvalidation.implicit;

import java.util.Date;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.validation.constraints.Min;
import javax.validation.executable.ExecutableType;
import javax.validation.executable.ValidateOnExecution;

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
