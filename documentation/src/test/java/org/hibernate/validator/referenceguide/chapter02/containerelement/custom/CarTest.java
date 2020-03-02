package org.hibernate.validator.referenceguide.chapter02.containerelement.custom;

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
				.addValueExtractor( new GearBoxValueExtractor() )
				.buildValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	public void validateCustomContainerElementConstraint() {
		//tag::validateCustomContainerElementConstraint[]
		Car car = new Car();
		car.setGearBox( new GearBox<>( new Gear.AcmeGear() ) );

		Set<ConstraintViolation<Car>> constraintViolations = validator.validate( car );
		assertEquals( 1, constraintViolations.size() );

		ConstraintViolation<Car> constraintViolation =
				constraintViolations.iterator().next();
		assertEquals(
				"Gear is not providing enough torque.",
				constraintViolation.getMessage()
		);
		assertEquals(
				"gearBox",
				constraintViolation.getPropertyPath().toString()
		);
		//end::validateCustomContainerElementConstraint[]
	}

}
