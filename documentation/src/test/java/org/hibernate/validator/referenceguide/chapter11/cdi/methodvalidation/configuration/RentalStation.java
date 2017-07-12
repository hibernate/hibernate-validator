//tag::include[]
package org.hibernate.validator.referenceguide.chapter11.cdi.methodvalidation.configuration;

//end::include[]

import java.util.Date;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.validation.Valid;
import javax.validation.constraints.Future;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.executable.ExecutableType;
import javax.validation.executable.ValidateOnExecution;

//tag::include[]
@ApplicationScoped
@ValidateOnExecution(type = ExecutableType.ALL)
public class RentalStation {

	@Valid
	public RentalStation() {
		//...
	}

	@NotNull
	@Valid
	@ValidateOnExecution(type = ExecutableType.NONE)
	public Car rentCar(
			@NotNull Customer customer,
			@NotNull @Future Date startDate,
			@Min(1) int durationInDays) {
		//...
		return null;
	}

	@NotNull
	public List<Car> getAvailableCars() {
		//...
		return null;
	}
}
//end::include[]
