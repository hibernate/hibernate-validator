package org.hibernate.validator.referenceguide.chapter12.classloading;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.hibernate.validator.HibernateValidator;
import org.junit.Test;

public class ClassLoadingTest {

	@Test
	public void setupValidator() {
		ClassLoader classLoader = ClassLoadingTest.class.getClassLoader();

		//tag::setupValidator[]
		Validator validator = Validation.byProvider( HibernateValidator.class )
				.configure()
				.externalClassLoader( classLoader )
				.buildValidatorFactory()
				.getValidator();
		//end::setupValidator[]
	}
}
