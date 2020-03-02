/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.methodvalidation;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.constraints.Size;
import jakarta.validation.executable.ExecutableValidator;

import org.testng.annotations.Test;

public class TypeVariableMethodParameterResolutionTest {

	@Test
	public void testTypeVariableMethodParameterResolution() throws NoSuchMethodException, SecurityException {
		ExecutableValidator validator = Validation.buildDefaultValidatorFactory()
				.getValidator().forExecutables();

		Set<ConstraintViolation<Bean>> violations = validator.validateParameters( new Bean(), Bean.class.getMethod( "method", List.class ),
				new Object[]{ new ArrayList<>() } );
		assertThat( violations ).containsOnlyViolations( violationOf( Size.class ) );
	}

	@SuppressWarnings("unused")
	private class Bean {

		public <T extends List<?>> void method(@Size(min = 1) T myList) {
		}
	}
}
