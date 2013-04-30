package org.hibernate.validator.referenceguide.chapter10.cdi.methodvalidation.implicit;

import java.util.Date;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.Future;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.executable.ExecutableType;
import javax.validation.executable.ValidateOnExecution;

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
