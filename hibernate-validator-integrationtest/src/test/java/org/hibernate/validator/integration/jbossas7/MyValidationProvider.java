package org.hibernate.validator.integration.jbossas7;

import javax.validation.Configuration;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.MessageInterpolator;
import javax.validation.TraversableResolver;
import javax.validation.Validator;
import javax.validation.ValidatorContext;
import javax.validation.ValidatorFactory;
import javax.validation.spi.BootstrapState;
import javax.validation.spi.ConfigurationState;
import javax.validation.spi.ValidationProvider;

/**
 * @author Hardy Ferentschik
 */
public class MyValidationProvider implements ValidationProvider<MyValidatorConfiguration> {

	public MyValidatorConfiguration createSpecializedConfiguration(BootstrapState state) {
		return MyValidatorConfiguration.class.cast( new MyValidatorConfiguration( this ) );
	}

	public Configuration<?> createGenericConfiguration(BootstrapState state) {
		return new MyValidatorConfiguration( this );
	}

	public ValidatorFactory buildValidatorFactory(ConfigurationState configurationState) {
		return new DummyValidatorFactory();
	}

	public static class DummyValidatorFactory implements ValidatorFactory {

		public Validator getValidator() {
			throw new UnsupportedOperationException();
		}

		public ValidatorContext usingContext() {
			throw new UnsupportedOperationException();
		}

		public MessageInterpolator getMessageInterpolator() {
			throw new UnsupportedOperationException();
		}

		public TraversableResolver getTraversableResolver() {
			throw new UnsupportedOperationException();
		}

		public ConstraintValidatorFactory getConstraintValidatorFactory() {
			throw new UnsupportedOperationException();
		}

		public <T> T unwrap(Class<T> type) {
			throw new UnsupportedOperationException();
		}
	}
}



