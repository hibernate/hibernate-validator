/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.integrationtest.java.module.no.el.constraint;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.integrationtest.java.module.no.el.model.Car;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;

import org.testng.annotations.Test;

public class JavaModulePathIT {

	@Test
	public void test() {
		Validator validator = Validation.byProvider( HibernateValidator.class )
				.configure()
				.messageInterpolator( new ParameterMessageInterpolator() )
				.buildValidatorFactory()
				.getValidator();

		assertThat( validator.validate( new Car() ) ).containsOnlyViolations(
				violationOf( CarServiceConstraint.class ).withMessage( "CarServiceConstraint:message" ),
				violationOf( CarConstraint.class ).withMessage( "CarConstraint:message" )
		);
	}

}
