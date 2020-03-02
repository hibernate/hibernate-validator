//tag::include[]
package org.hibernate.validator.referenceguide.chapter11.cdi.methodvalidation.configuration;

//end::include[]

import java.util.Date;
import java.util.List;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.executable.ExecutableType;
import jakarta.validation.executable.ValidateOnExecution;

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
