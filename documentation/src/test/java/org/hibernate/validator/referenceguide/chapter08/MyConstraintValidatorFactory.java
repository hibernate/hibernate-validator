package org.hibernate.validator.referenceguide.chapter08;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorFactory;

public class MyConstraintValidatorFactory implements ConstraintValidatorFactory {

	@Override
	public <T extends ConstraintValidator<?, ?>> T getInstance(Class<T> key) {
		//...
		return null;
	}

	@Override
	public void releaseInstance(ConstraintValidator<?, ?> instance) {
		//...
	}
}
