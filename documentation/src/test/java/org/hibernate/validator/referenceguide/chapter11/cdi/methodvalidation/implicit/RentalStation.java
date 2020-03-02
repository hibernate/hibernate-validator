//tag::include[]
package org.hibernate.validator.referenceguide.chapter11.cdi.methodvalidation.implicit;

//end::include[]

import java.util.Date;
import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.executable.ExecutableType;
import jakarta.validation.executable.ValidateOnExecution;

//tag::include[]
@ValidateOnExecution(type = ExecutableType.ALL)
public interface RentalStation {

	@NotNull
	@Valid
	Car rentCar(
			@NotNull Customer customer,
			@NotNull @Future Date startDate,
			@Min(1) int durationInDays);

	@NotNull
	List<Car> getAvailableCars();
}
//end::include[]
