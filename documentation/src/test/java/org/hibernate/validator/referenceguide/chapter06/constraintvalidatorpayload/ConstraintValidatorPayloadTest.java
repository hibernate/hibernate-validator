package org.hibernate.validator.referenceguide.chapter06.constraintvalidatorpayload;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorFactory;
import org.junit.Test;

@SuppressWarnings("unused")
public class ConstraintValidatorPayloadTest {

	@Test
	public void setConstraintValidatorPayloadDuringValidatorFactoryInitialization() {
		//tag::setConstraintValidatorPayloadDuringValidatorFactoryInitialization[]
		ValidatorFactory validatorFactory = Validation.byProvider( HibernateValidator.class )
				.configure()
				.constraintValidatorPayload( "US" )
				.buildValidatorFactory();

		Validator validator = validatorFactory.getValidator();
		//end::setConstraintValidatorPayloadDuringValidatorFactoryInitialization[]
	}

	@Test
	public void setConstraintValidatorPayloadInValidatorContext() {
		//tag::setConstraintValidatorPayloadInValidatorContext[]
		HibernateValidatorFactory hibernateValidatorFactory = Validation.byDefaultProvider()
				.configure()
				.buildValidatorFactory()
				.unwrap( HibernateValidatorFactory.class );

		Validator validator = hibernateValidatorFactory.usingContext()
				.constraintValidatorPayload( "US" )
				.getValidator();

		// [...] US specific validation checks

		validator = hibernateValidatorFactory.usingContext()
				.constraintValidatorPayload( "FR" )
				.getValidator();

		// [...] France specific validation checks

		//end::setConstraintValidatorPayloadInValidatorContext[]
	}
}
