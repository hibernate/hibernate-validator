/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.referenceguide.chapter06;

import static org.junit.Assert.assertEquals;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.BeforeClass;
import org.junit.Test;

public class CarTest {

	private static Validator validator;

	@BeforeClass
	public static void setUp() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	public void testCheckCaseConstraint() {
		//tag::testCheckCaseConstraint[]
		//invalid license plate
		Car car = new Car( "Morris", "dd-ab-123", 4 );
		Set<ConstraintViolation<Car>> constraintViolations =
				validator.validate( car );
		assertEquals( 1, constraintViolations.size() );
		assertEquals(
				"Case mode must be UPPER.",
				constraintViolations.iterator().next().getMessage()
		);

		//valid license plate
		car = new Car( "Morris", "DD-AB-123", 4 );

		constraintViolations = validator.validate( car );

		assertEquals( 0, constraintViolations.size() );
		//end::testCheckCaseConstraint[]
	}
}
