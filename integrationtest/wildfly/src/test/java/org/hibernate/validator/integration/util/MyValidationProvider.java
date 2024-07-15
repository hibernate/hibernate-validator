/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.integration.util;

import jakarta.validation.ClockProvider;
import jakarta.validation.Configuration;
import jakarta.validation.ConstraintValidatorFactory;
import jakarta.validation.MessageInterpolator;
import jakarta.validation.ParameterNameProvider;
import jakarta.validation.TraversableResolver;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorContext;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.spi.BootstrapState;
import jakarta.validation.spi.ConfigurationState;
import jakarta.validation.spi.ValidationProvider;

/**
 * @author Hardy Ferentschik
 */
public class MyValidationProvider implements ValidationProvider<MyValidatorConfiguration> {

	@Override
	public MyValidatorConfiguration createSpecializedConfiguration(BootstrapState state) {
		return MyValidatorConfiguration.class.cast( new MyValidatorConfiguration( this ) );
	}

	@Override
	public Configuration<?> createGenericConfiguration(BootstrapState state) {
		return new MyValidatorConfiguration( this );
	}

	@Override
	public ValidatorFactory buildValidatorFactory(ConfigurationState configurationState) {
		return new DummyValidatorFactory();
	}

	public static class DummyValidatorFactory implements ValidatorFactory {

		@Override
		public Validator getValidator() {
			return new MyValidator();
		}

		@Override
		public ValidatorContext usingContext() {
			throw new UnsupportedOperationException();
		}

		@Override
		public MessageInterpolator getMessageInterpolator() {
			throw new UnsupportedOperationException();
		}

		@Override
		public TraversableResolver getTraversableResolver() {
			throw new UnsupportedOperationException();
		}

		@Override
		public ConstraintValidatorFactory getConstraintValidatorFactory() {
			throw new UnsupportedOperationException();
		}

		@Override
		public <T> T unwrap(Class<T> type) {
			throw new UnsupportedOperationException();
		}

		@Override
		public ParameterNameProvider getParameterNameProvider() {
			throw new UnsupportedOperationException();
		}

		@Override
		public ClockProvider getClockProvider() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void close() {
		}
	}
}
