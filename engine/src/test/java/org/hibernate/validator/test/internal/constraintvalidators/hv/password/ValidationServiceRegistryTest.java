/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.constraintvalidators.hv.password;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import jakarta.validation.Validation;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorFactory;
import org.hibernate.validator.spi.password.PasswordStrengthEstimator;
import org.hibernate.validator.spi.password.PasswordStrengthResult;
import org.hibernate.validator.spi.scripting.ScriptEvaluatorFactory;

import org.testng.annotations.Test;

public class ValidationServiceRegistryTest {

	@Test
	public void registeredServiceIsRetrievable() {
		PasswordStrengthEstimator estimator = password -> new PasswordStrengthResult() {
			@Override
			public int score() {
				return 3;
			}

			@Override
			public String feedback() {
				return null;
			}
		};

		HibernateValidatorFactory factory = Validation.byProvider( HibernateValidator.class )
				.configure()
				.addValidationService( PasswordStrengthEstimator.class, estimator )
				.buildValidatorFactory()
				.unwrap( HibernateValidatorFactory.class );

		PasswordStrengthEstimator retrieved = factory.getValidationService( PasswordStrengthEstimator.class );
		assertNotNull( retrieved );

		PasswordStrengthResult result = retrieved.estimate( "test".toCharArray() );
		assertEquals( result.score(), 3 );
	}

	@Test
	public void unregisteredServiceReturnsNull() {
		HibernateValidatorFactory factory = Validation.byProvider( HibernateValidator.class )
				.configure()
				.buildValidatorFactory()
				.unwrap( HibernateValidatorFactory.class );

		PasswordStrengthEstimator retrieved = factory.getValidationService( PasswordStrengthEstimator.class );
		assertNull( retrieved );
	}

	@Test
	public void scriptEvaluatorFactoryIsAccessibleAsService() {
		HibernateValidatorFactory factory = Validation.byProvider( HibernateValidator.class )
				.configure()
				.buildValidatorFactory()
				.unwrap( HibernateValidatorFactory.class );

		ScriptEvaluatorFactory scriptFactory = factory.getValidationService( ScriptEvaluatorFactory.class );
		assertNotNull( scriptFactory );
	}
}
