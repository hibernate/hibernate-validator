/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.referenceguide.chapter03.inheritance.parameter;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

	@Test
	public void illegalParameterConstraints() throws Exception {
		assertThatThrownBy( () -> {
			Car object = new Car();
			Method method = Car.class.getMethod( "drive", int.class );
			Object[] parameterValues = { };
			executableValidator.validateParameters(
					object,
					method,
					parameterValues
			);
		} ).isInstanceOf( ConstraintDeclarationException.class );
	}
}
