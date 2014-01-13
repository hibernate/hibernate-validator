package org.hibernate.validator.referenceguide.chapter04.complete;

import java.math.BigDecimal;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class Car {

	@NotNull
	private String manufacturer;

	@Size(
			min = 2,
			max = 14,
			message = "The license plate '${validatedValue}' must be between {min} and {max} characters long"
	)
	private String licensePlate;

	@Min(
			value = 2,
			message = "There must be at least {value} seat${value > 1 ? 's' : ''}"
	)
	private int seatCount;

	@DecimalMax(
			value = "350",
			message = "The top speed ${formatter.format('%1$.2f', validatedValue)} is higher " +
					"than {value}"
	)
	private double topSpeed;

	@DecimalMax(value = "100000", message = "Price must not be higher than ${value}")
	private BigDecimal price;

	public Car(
			String manufacturer,
			String licensePlate,
			int seatCount,
			double topSpeed,
			BigDecimal price) {
		this.manufacturer = manufacturer;
		this.licensePlate = licensePlate;
		this.seatCount = seatCount;
		this.topSpeed = topSpeed;
		this.price = price;
	}

	//getters and setters ...
}
