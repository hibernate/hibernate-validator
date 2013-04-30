package org.hibernate.validator.referenceguide.chapter06.classlevel;

import java.util.Arrays;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ClassLevelConstraintTest {

	private static Validator validator;

	@BeforeClass
	public static void setUpValidator() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	public void testClassLevelConstraint() {
		Car car = new Car(
				2,
				Arrays.asList(
						new Person( "Alice" ),
						new Person( "Bob" ),
						new Person( "Bill" )
				)
		);

		Set<ConstraintViolation<Car>> constraintViolations = validator.validate( car );

		assertEquals( 1, constraintViolations.size() );
		assertEquals(
				"There must be not more passengers than seats.",
				constraintViolations.iterator().next().getMessage()
		);
	}
}
