//tag::include[]
package org.hibernate.validator.referenceguide.chapter09;

//end::include[]

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorFactory;

//tag::include[]
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
//end::include[]
