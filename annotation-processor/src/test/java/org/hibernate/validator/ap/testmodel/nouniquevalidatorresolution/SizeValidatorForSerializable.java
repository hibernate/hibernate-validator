package org.hibernate.validator.ap.testmodel.nouniquevalidatorresolution;

import java.io.Serializable;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class SizeValidatorForSerializable implements ConstraintValidator<Size, Serializable> {

	@Override
	public void initialize(Size constraintAnnotation) {
	}

	@Override
	public boolean isValid(Serializable object, ConstraintValidatorContext constraintContext) {
		return true;
	}
}
