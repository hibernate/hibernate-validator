package org.hibernate.validator.referenceguide.chapter12.dynamicpayload;

import java.util.Set;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.BeforeClass;
import org.junit.Test;

import org.hibernate.validator.engine.HibernateConstraintViolation;

import static org.junit.Assert.assertEquals;

public class DynamicPayLoadTest {

	private static Validator validator;

	@BeforeClass
	public static void setUpValidator() {
		ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
		validator = validatorFactory.getValidator();
	}

	@Test
	public void testDynamicPayloadAddedToConstraintViolation() throws Exception {
		//tag::include[]
		Car car = new Car( 2 );
		car.addPassenger( new Person() );
		car.addPassenger( new Person() );
		car.addPassenger( new Person() );
		Set<ConstraintViolation<Car>> constraintViolations = validator.validate( car );

		assertEquals( 1, constraintViolations.size() );

		ConstraintViolation<Car> constraintViolation = constraintViolations.iterator().next();
		@SuppressWarnings("unchecked")
		HibernateConstraintViolation<Car> hibernateConstraintViolation = constraintViolation.unwrap(
				HibernateConstraintViolation.class
		);
		String suggestedCar = hibernateConstraintViolation.getDynamicPayload( String.class );
		assertEquals( "Toyota Volta", suggestedCar );
		//end::include[]
	}
}

