package org.hibernate.validator.referenceguide.chapter06.custompath;

import java.util.Arrays;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CarTest {

	private static Validator validator;

	@BeforeClass
	public static void setUp() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	public void morePassengersThanSeats() {
		Car car = new Car( 2, Arrays.asList( "Bob", "Alice", "Bill" ) );

		Set<ConstraintViolation<Car>> constraintViolations =
				validator.validate( car );

		assertEquals( 1, constraintViolations.size() );

		ConstraintViolation<Car> violation = constraintViolations.iterator().next();
		assertEquals( "{my.custom.template}", violation.getMessage() );
		assertEquals( "passengers", violation.getPropertyPath().iterator().next().getName() );
	}
}
