package org.hibernate.validator.referenceguide.chapter02.containerelement.map;

import static org.junit.Assert.assertEquals;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.hibernate.validator.HibernateValidator;
import org.junit.BeforeClass;
import org.junit.Test;

public class CarTest {

	private static Validator validator;

	@BeforeClass
	public static void setUpValidator() {
		ValidatorFactory factory = Validation.byProvider( HibernateValidator.class )
				.configure()
				.buildValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	public void validateMapValueContainerElementConstraint() {
		//tag::validateMapValueContainerElementConstraint[]
		Car car = new Car();
		car.setFuelConsumption( Car.FuelConsumption.HIGHWAY, 20 );

		Set<ConstraintViolation<Car>> constraintViolations = validator.validate( car );

		assertEquals( 1, constraintViolations.size() );

		ConstraintViolation<Car> constraintViolation =
				constraintViolations.iterator().next();
		assertEquals(
				"20 is outside the max fuel consumption.",
				constraintViolation.getMessage()
		);
		assertEquals(
				"fuelConsumption[HIGHWAY].<map value>",
				constraintViolation.getPropertyPath().toString()
		);
		//end::validateMapValueContainerElementConstraint[]
	}

	@Test
	public void validateMapKeyContainerElementConstraint() {
		//tag::validateMapKeyContainerElementConstraint[]
		Car car = new Car();
		car.setFuelConsumption( null, 5 );

		Set<ConstraintViolation<Car>> constraintViolations = validator.validate( car );

		assertEquals( 1, constraintViolations.size() );

		ConstraintViolation<Car> constraintViolation =
				constraintViolations.iterator().next();
		assertEquals(
				"must not be null",
				constraintViolation.getMessage()
		);
		assertEquals(
				"fuelConsumption<K>[].<map key>",
				constraintViolation.getPropertyPath().toString()
		);
		//end::validateMapKeyContainerElementConstraint[]
	}
}
