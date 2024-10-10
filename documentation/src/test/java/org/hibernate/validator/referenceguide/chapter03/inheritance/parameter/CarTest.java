/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.referenceguide.chapter03.inheritance.parameter;

import java.lang.reflect.Method;

import jakarta.validation.ConstraintDeclarationException;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
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

	@Test(expected = ConstraintDeclarationException.class)
	public void illegalParameterConstraints() throws Exception {
		Car object = new Car();
		Method method = Car.class.getMethod( "drive", int.class );
		Object[] parameterValues = { };
		executableValidator.validateParameters(
				object,
				method,
				parameterValues
		);
	}
}
