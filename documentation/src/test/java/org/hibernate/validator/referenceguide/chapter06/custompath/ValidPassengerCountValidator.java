/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
//tag::include[]
package org.hibernate.validator.referenceguide.chapter06.custompath;

//end::include[]
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

//tag::include[]
public class ValidPassengerCountValidator
		implements ConstraintValidator<ValidPassengerCount, Car> {

	@Override
	public void initialize(ValidPassengerCount constraintAnnotation) {
	}

	@Override
	public boolean isValid(Car car, ConstraintValidatorContext constraintValidatorContext) {
		if ( car == null ) {
			return true;
		}

		boolean isValid = car.getPassengers().size() <= car.getSeatCount();

		if ( !isValid ) {
			constraintValidatorContext.disableDefaultConstraintViolation();
			constraintValidatorContext
					.buildConstraintViolationWithTemplate( "{my.custom.template}" )
					.addPropertyNode( "passengers" ).addConstraintViolation();
		}

		return isValid;
	}
}
//end::include[]
