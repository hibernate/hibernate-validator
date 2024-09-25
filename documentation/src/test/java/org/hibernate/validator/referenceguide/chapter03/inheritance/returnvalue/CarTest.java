/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.referenceguide.chapter03.inheritance.returnvalue;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.Size;
import jakarta.validation.executable.ExecutableValidator;

import org.junit.BeforeClass;
import org.junit.Test;

public class CarTest {

	private static ExecutableValidator executableValidator;

	@BeforeClass
	public static void setUpValidator() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		executableValidator = factory.getValidator().forExecutables();
	}

	@Test
	public void returnValueConstraintsAddUp() throws Exception {
		Car object = new Car();
		Method method = Car.class.getMethod( "getPassengers" );
		Object returnValue = Collections.<Person>emptyList();
		Set<ConstraintViolation<Car>> violations = executableValidator.validateReturnValue(
				object,
				method,
				returnValue
		);

		assertEquals( 1, violations.size() );
		assertEquals(
				Size.class,
				violations.iterator()
						.next()
						.getConstraintDescriptor()
						.getAnnotation()
						.annotationType()
		);
	}
}
