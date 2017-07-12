package org.hibernate.validator.referenceguide.chapter12.constraintapi;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * @author Hardy Ferentschik
 */
public class ValidPassengerCountValidator implements ConstraintValidator<ValidPassengerCount, Bus> {

	@Override
	public void initialize(ValidPassengerCount constraintAnnotation) {
	}

	@Override
	public boolean isValid(Bus car, ConstraintValidatorContext context) {
		if ( car == null ) {
			return true;
		}

		return car.getSeatCount() >= car.getPassengers().size();

	}
}


