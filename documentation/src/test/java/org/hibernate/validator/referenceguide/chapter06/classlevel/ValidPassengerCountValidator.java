package org.hibernate.validator.referenceguide.chapter06.classlevel;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ValidPassengerCountValidator
		implements ConstraintValidator<ValidPassengerCount, Car> {

	@Override
	public void initialize(ValidPassengerCount constraintAnnotation) {
	}

	@Override
	public boolean isValid(Car car, ConstraintValidatorContext context) {
		if ( car == null ) {
			return true;
		}

		return car.getPassengers().size() <= car.getSeatCount();
	}
}
