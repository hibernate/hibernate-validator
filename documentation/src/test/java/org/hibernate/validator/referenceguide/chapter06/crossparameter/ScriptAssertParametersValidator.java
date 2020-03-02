package org.hibernate.validator.referenceguide.chapter06.crossparameter;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraintvalidation.SupportedValidationTarget;
import jakarta.validation.constraintvalidation.ValidationTarget;

@SupportedValidationTarget(ValidationTarget.PARAMETERS)
public class ScriptAssertParametersValidator implements
		ConstraintValidator<ScriptAssert, Object[]> {

	@Override
	public void initialize(ScriptAssert constraintAnnotation) {
	}

	@Override
	public boolean isValid(Object[] value, ConstraintValidatorContext context) {
		return false;
	}
}
