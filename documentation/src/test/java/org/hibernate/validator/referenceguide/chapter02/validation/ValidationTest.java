package org.hibernate.validator.referenceguide.chapter02.validation;

import static org.junit.Assert.assertEquals;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.BeforeClass;
import org.junit.Test;

public class ValidationTest {

	private static Validator validator;

	@BeforeClass
	public static void setUpValidator() {
		//tag::setUpValidator[]
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
		//end::setUpValidator[]
	}

	@Test
	public void validate() {
		//tag::validate[]
		Car car = new Car( null, true );

		Set<ConstraintViolation<Car>> constraintViolations = validator.validate( car );

		assertEquals( 1, constraintViolations.size() );
		assertEquals( "must not be null", constraintViolations.iterator().next().getMessage() );
		//end::validate[]
	}

	@Test
	public void validateProperty() {
		//tag::validateProperty[]
		Car car = new Car( null, true );

		Set<ConstraintViolation<Car>> constraintViolations = validator.validateProperty(
				car,
				"manufacturer"
		);

		assertEquals( 1, constraintViolations.size() );
		assertEquals( "must not be null", constraintViolations.iterator().next().getMessage() );
		//end::validateProperty[]
	}

	@Test
	public void validateValue() {
		//tag::validateValue[]
		Set<ConstraintViolation<Car>> constraintViolations = validator.validateValue(
				Car.class,
				"manufacturer",
				null
		);

		assertEquals( 1, constraintViolations.size() );
		assertEquals( "must not be null", constraintViolations.iterator().next().getMessage() );
		//end::validateValue[]
	}

}
