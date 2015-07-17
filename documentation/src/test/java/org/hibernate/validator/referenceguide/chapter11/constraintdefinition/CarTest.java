package org.hibernate.validator.referenceguide.chapter11.constraintdefinition;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.junit.BeforeClass;
import org.junit.Test;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.spi.constraintdefinition.ConstraintDefinitionContributor;

import static org.junit.Assert.assertEquals;

public class CarTest {

	private static Validator validator;

	public static class MyConstraintDefinitionContributor
			implements ConstraintDefinitionContributor {

		@Override
		public void collectConstraintDefinitions(ConstraintDefinitionBuilder builder) {
			builder.constraint( ValidPassengerCount.class )
					.validatedBy( ValidPassengerCountValidator.class );
		}
	}

	@BeforeClass
	public static void setUpValidator() {

		HibernateValidatorConfiguration configuration = Validation
				.byProvider( HibernateValidator.class )
				.configure();

		ConstraintDefinitionContributor contributor = new MyConstraintDefinitionContributor();
		configuration.addConstraintDefinitionContributor( contributor );

		validator = configuration.buildValidatorFactory().getValidator();
	}

	@Test
	public void testProgrammaticRegistrationOfConstraintValidator() throws Exception {
		Car car = new Car( 2 );
		car.addPassenger( new Person() );
		car.addPassenger( new Person() );
		car.addPassenger( new Person() );
		Set<ConstraintViolation<Car>> violations = validator.validate( car );

		assertEquals( 1, violations.size() );
	}
}

