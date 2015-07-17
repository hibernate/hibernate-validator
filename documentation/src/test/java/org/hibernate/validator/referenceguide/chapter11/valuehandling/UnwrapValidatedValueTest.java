package org.hibernate.validator.referenceguide.chapter11.valuehandling;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.junit.Test;

import org.hibernate.validator.HibernateValidator;

import static org.junit.Assert.assertEquals;

public class UnwrapValidatedValueTest {

	@Test
	public void failFast() {
		Validator validator = Validation.byProvider( HibernateValidator.class )
				.configure()
				.addValidatedValueHandler( new PropertyValueUnwrapper() )
				.buildValidatorFactory()
				.getValidator();
	}
}
