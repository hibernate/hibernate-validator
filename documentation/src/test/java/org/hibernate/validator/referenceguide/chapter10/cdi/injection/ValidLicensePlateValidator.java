package org.hibernate.validator.referenceguide.chapter10.cdi.injection;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

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
