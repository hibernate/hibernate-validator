package org.hibernate.validator.referenceguide.chapter02.typeargument.list;

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
	public void validateListTypeArgumentConstraint() {
		//tag::validateListTypeArgumentConstraint[]
		Car car = new Car();
		car.addPart( "Wheel" );
		car.addPart( null );

		Set<ConstraintViolation<Car>> constraintViolations = validator.validate( car );

		assertEquals( 1, constraintViolations.size() );
		assertEquals(
				"'null' is not a valid car part.",
				constraintViolations.iterator().next().getMessage()
		);
		assertEquals( "parts[1]", constraintViolations.iterator().next().getPropertyPath().toString() );
		//end::validateListTypeArgumentConstraint[]
	}

}
