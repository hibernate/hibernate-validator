package org.hibernate.validator.referenceguide.chapter12.failfast;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.hibernate.validator.HibernateValidator;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FailFastTest {

	@Test
	public void failFast() {
		//tag::include[]
		Validator validator = Validation.byProvider( HibernateValidator.class )
				.configure()
				.failFast( true )
				.buildValidatorFactory()
				.getValidator();

		Car car = new Car( null, false );

		Set<ConstraintViolation<Car>> constraintViolations = validator.validate( car );

		assertEquals( 1, constraintViolations.size() );
		//end::include[]
	}
}
