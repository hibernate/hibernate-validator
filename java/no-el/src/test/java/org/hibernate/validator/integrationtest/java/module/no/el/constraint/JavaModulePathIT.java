/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.integrationtest.java.module.no.el.constraint;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.integrationtest.java.module.no.el.model.Car;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
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
