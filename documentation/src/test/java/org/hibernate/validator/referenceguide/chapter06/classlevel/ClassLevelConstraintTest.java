/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.referenceguide.chapter06.classlevel;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.BeforeClass;
import org.junit.Test;

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
