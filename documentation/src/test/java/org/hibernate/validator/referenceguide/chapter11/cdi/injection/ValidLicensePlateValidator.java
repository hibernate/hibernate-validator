/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
//tag::include[]
package org.hibernate.validator.referenceguide.chapter11.cdi.injection;

//end::include[]
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

//tag::include[]
public class ValidLicensePlateValidator
		implements ConstraintValidator<ValidLicensePlate, String> {

	@Inject
	private VehicleRegistry vehicleRegistry;

	@PostConstruct
	public void postConstruct() {
		//do initialization logic...
	}

	@PreDestroy
	public void preDestroy() {
		//do destruction logic...
	}

	@Override
	public void initialize(ValidLicensePlate constraintAnnotation) {
	}

	@Override
	public boolean isValid(String licensePlate, ConstraintValidatorContext constraintContext) {
		return vehicleRegistry.isValidLicensePlate( licensePlate );
	}
}
//end::include[]
