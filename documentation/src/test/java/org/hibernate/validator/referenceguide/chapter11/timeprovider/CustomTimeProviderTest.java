package org.hibernate.validator.referenceguide.chapter11.timeprovider;

import javax.validation.Validation;
import javax.validation.ValidatorFactory;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.spi.time.TimeProvider;
import org.junit.Test;

public class CustomTimeProviderTest {

	@Test
	public void setupValidator() {
		TimeProvider timeProvider = new CustomTimeProvider();

		//tag::setupValidator[]
		ValidatorFactory validatorFactory = Validation.byProvider( HibernateValidator.class )
				.configure()
				.timeProvider( timeProvider )
				.buildValidatorFactory();
		//end::setupValidator[]
	}
}
