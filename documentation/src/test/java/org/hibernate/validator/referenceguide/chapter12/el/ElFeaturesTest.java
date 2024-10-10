/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.referenceguide.chapter12.el;

import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.messageinterpolation.ExpressionLanguageFeatureLevel;

import org.junit.Test;

public class ElFeaturesTest {

	@SuppressWarnings("unused")
	@Test
	public void testConstraints() throws Exception {
		//tag::constraints[]
		ValidatorFactory validatorFactory = Validation.byProvider( HibernateValidator.class )
				.configure()
				.constraintExpressionLanguageFeatureLevel( ExpressionLanguageFeatureLevel.VARIABLES )
				.buildValidatorFactory();
		//end::constraints[]
	}

	@SuppressWarnings("unused")
	@Test
	public void testCustomViolations() throws Exception {
		//tag::customViolations[]
		ValidatorFactory validatorFactory = Validation.byProvider( HibernateValidator.class )
				.configure()
				.customViolationExpressionLanguageFeatureLevel( ExpressionLanguageFeatureLevel.VARIABLES )
				.buildValidatorFactory();
		//end::customViolations[]
	}
}
