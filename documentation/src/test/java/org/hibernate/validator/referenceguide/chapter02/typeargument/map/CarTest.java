package org.hibernate.validator.referenceguide.chapter02.typeargument.map;

import static org.junit.Assert.assertEquals;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

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
	public void validateMapTypeArgumentConstraint() {
		//tag::validateMapTypeArgumentConstraint[]
		Car car = new Car();
		car.setFuelConsumption( Car.FuelConsumption.HIGHWAY, 20 );

		Set<ConstraintViolation<Car>> constraintViolations = validator.validate( car );

		assertEquals( 1, constraintViolations.size() );
		assertEquals( "20 is outside the max fuel consumption.", constraintViolations.iterator().next().getMessage() );
		//end::validateMapTypeArgumentConstraint[]
	}

}
