/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.referenceguide.chapter03.cascaded;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.executable.ExecutableValidator;

import org.junit.BeforeClass;
import org.junit.Test;

public class GarageTest {

	private static ExecutableValidator executableValidator;

	@BeforeClass
	public static void setUpValidator() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		executableValidator = factory.getValidator().forExecutables();
	}

	@Test
	public void cascadedMethodParameterValidation() throws Exception {
		//cascaded method parameter
		Garage object = new Garage( "Bob's Auto Shop" );
		Method method = Garage.class.getMethod( "checkCar", Car.class );
		Object[] parameterValues = { new Car( "Morris", "A" ) };

		Set<ConstraintViolation<Garage>> violations = executableValidator.validateParameters(
				object,
				method,
				parameterValues
		);

		//Car#licensePlate is too short
		assertEquals( 1, violations.size() );
		ConstraintViolation<Garage> violation = violations.iterator().next();
		assertEquals(
				Size.class,
				violation.getConstraintDescriptor().getAnnotation().annotationType()
		);
	}

	@Test
	public void cascadedConstructorReturnValueValidation() throws Exception {
		//cascaded constructor return value
		Constructor<Garage> constructor = Garage.class.getConstructor( String.class );
		Garage createdObject = new Garage( null );

		Set<ConstraintViolation<Garage>> violations = executableValidator.validateConstructorReturnValue(
				constructor,
				createdObject
		);

		//Garage#name is null
		assertEquals( 1, violations.size() );
		ConstraintViolation<Garage> violation = violations.iterator().next();
		assertEquals(
				NotNull.class,
				violation.getConstraintDescriptor().getAnnotation().annotationType()
		);
	}
}
