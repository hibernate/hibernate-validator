/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.referenceguide.chapter12.constraintapi;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

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
