package org.hibernate.validator.referenceguide.chapter11.failfast;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.junit.Test;

import org.hibernate.validator.HibernateValidator;

import static org.junit.Assert.assertEquals;

public class FailFastTest {

	@Test
	public void failFast() {
		Validator validator = Validation.byProvider( HibernateValidator.class )
				.configure()
				.failFast( true )
				.buildValidatorFactory()
				.getValidator();

		Car car = new Car( null, false );

		Set<ConstraintViolation<Car>> constraintViolations = validator.validate( car );

		assertEquals( 1, constraintViolations.size() );
	}
}
