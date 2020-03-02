/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.messageinterpolation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;

import jakarta.validation.MessageInterpolator;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.metadata.ConstraintDescriptor;

import org.hibernate.validator.HibernateValidator;
import org.testng.annotations.Test;

public class DefaultLocaleMessageInterpolationTest {

	@Test
	public void testNoDefaultLocaleDefinedStillWorking() {
		ValidatorFactory validatorFactory = Validation.byProvider( HibernateValidator.class )
				.configure()
				.buildValidatorFactory();

		MessageInterpolator messageInterpolator = validatorFactory.getMessageInterpolator();

		assertThat( messageInterpolator.interpolate( "{jakarta.validation.constraints.AssertFalse.message}", new TestContext() ) )
						.isEqualTo( "must be false" );
	}

	@Test
	public void testDefaultLocaleHonored() {
		ValidatorFactory validatorFactory = Validation.byProvider( HibernateValidator.class )
				.configure()
				.defaultLocale( Locale.FRANCE )
				.buildValidatorFactory();

		MessageInterpolator messageInterpolator = validatorFactory.getMessageInterpolator();

		assertThat( messageInterpolator.interpolate( "{jakarta.validation.constraints.AssertFalse.message}", new TestContext() ) )
						.isEqualTo( "doit avoir la valeur faux" );

		validatorFactory = Validation.byProvider( HibernateValidator.class )
				.configure()
				.defaultLocale( Locale.ITALY )
				.buildValidatorFactory();

		messageInterpolator = validatorFactory.getMessageInterpolator();

		assertThat( messageInterpolator.interpolate( "{jakarta.validation.constraints.AssertFalse.message}", new TestContext() ) )
						.isEqualTo( "deve essere false" );
	}

	private static class TestContext implements MessageInterpolator.Context {

		@Override
		public ConstraintDescriptor<?> getConstraintDescriptor() {
			return null;
		}

		@Override
		public Object getValidatedValue() {
			return null;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T unwrap(Class<T> type) {
			return (T) this;
		}
	}
}
