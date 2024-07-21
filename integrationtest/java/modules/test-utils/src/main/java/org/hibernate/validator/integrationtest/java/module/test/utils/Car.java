/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.integrationtest.java.module.test.utils;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import org.hibernate.validator.HibernateValidator;

public class Car {

	@NotNull
	String text;

	@Positive
	Integer integer = -1;


	public static boolean test() {
		Validator validator = Validation.byProvider( HibernateValidator.class )
				.configure()
				.buildValidatorFactory()
				.getValidator();

		assertThat( validator.validate( new Car() ) ).containsOnlyViolations(
				violationOf( NotNull.class ).withMessage( "must not be null" ),
				violationOf( Positive.class ).withMessage( "must be greater than 0" )
		);

		return true;
	}
}
