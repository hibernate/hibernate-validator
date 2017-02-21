package org.hibernate.validator.referenceguide.chapter02.typeargument.custom;

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
				.addValueExtractor( new GearBoxExtractor() )
				.buildValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	public void validateCustomTypeArgumentConstraint() {
		//tag::validateCustomTypeArgumentConstraint[]
		Car car = new Car();
		car.setGearBox( new GearBox<>( new Gear.AcmeGear() ) );

		Set<ConstraintViolation<Car>> constraintViolations = validator.validate( car );
		assertEquals( 1, constraintViolations.size() );
		assertEquals( "Gear is not providing enough torque.", constraintViolations.iterator().next().getMessage() );
		assertEquals( "gearBox", constraintViolations.iterator().next().getPropertyPath().toString() );
		//end::validateCustomTypeArgumentConstraint[]
	}

}
