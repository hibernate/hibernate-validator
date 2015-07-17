package org.hibernate.validator.referenceguide.chapter04.complete;

import java.math.BigDecimal;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CarTest {

	private static Validator validator;

	@BeforeClass
	public static void setUpValidator() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	public void messageDescriptors() {
		Car car = new Car( null, "A", 1, 400.123456, BigDecimal.valueOf( 200000 ) );

		String message = validator.validateProperty( car, "manufacturer" )
				.iterator()
				.next()
				.getMessage();
		assertEquals( "may not be null", message );

		message = validator.validateProperty( car, "licensePlate" )
				.iterator()
				.next()
				.getMessage();
		assertEquals(
				"The license plate 'A' must be between 2 and 14 characters long",
				message
		);

		message = validator.validateProperty( car, "seatCount" ).iterator().next().getMessage();
		assertEquals( "There must be at least 2 seats", message );

		message = validator.validateProperty( car, "topSpeed" ).iterator().next().getMessage();
		assertEquals( "The top speed 400.12 is higher than 350", message );

		message = validator.validateProperty( car, "price" ).iterator().next().getMessage();
		assertEquals( "Price must not be higher than $100000", message );
	}
}
