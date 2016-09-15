package org.hibernate.validator.referenceguide.chapter11.valuehandling;

import javax.validation.Validation;
import javax.validation.Validator;

import org.hibernate.validator.HibernateValidator;
import org.junit.Test;

public class UnwrapValidatedValueTest {

	@Test
	public void unwrapValidated() {
		//tag::unwrapValidated[]
		Validator validator = Validation.byProvider( HibernateValidator.class )
				.configure()
				.addValidatedValueHandler( new PropertyValueUnwrapper() )
				.buildValidatorFactory()
				.getValidator();
		//end::unwrapValidated[]
	}
}
