/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.referenceguide.chapter12.failfast;

import static org.junit.Assert.assertEquals;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.hibernate.validator.HibernateValidator;

import org.junit.Test;

public class FailFastTest {

	@Test
	public void failFast() {
		//tag::include[]
		Validator validator = Validation.byProvider( HibernateValidator.class )
				.configure()
				.failFast( true )
				.buildValidatorFactory()
				.getValidator();

		Car car = new Car( null, false );

		Set<ConstraintViolation<Car>> constraintViolations = validator.validate( car );

		assertEquals( 1, constraintViolations.size() );
		//end::include[]
	}
}
