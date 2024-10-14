/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.integrationtest.java.module.simple.constraint;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.integrationtest.java.module.simple.model.Car;

import org.testng.annotations.Test;

public class JavaModulePathIT {

	@Test
	public void test() {
		Validator validator = Validation.byProvider( HibernateValidator.class )
				.configure()
				.buildValidatorFactory()
				.getValidator();

		assertThat( validator.validate( new Car() ) ).containsOnlyViolations(
				violationOf( CarServiceConstraint.class ).withMessage( "CarServiceConstraint:message" ),
				violationOf( CarConstraint.class ).withMessage( "CarConstraint:message" )
		);
	}

}
