package org.hibernate.validator.integration.jbossas7;

import java.io.InputStream;
import javax.validation.Configuration;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.MessageInterpolator;
import javax.validation.TraversableResolver;
import javax.validation.ValidatorFactory;
import javax.validation.spi.ValidationProvider;

/**
 * @author Hardy Ferentschik
 */
public class MyValidatorConfiguration implements Configuration<MyValidatorConfiguration> {

	private final ValidationProvider provider;

	public MyValidatorConfiguration() {
		provider = null;
	}

	public MyValidatorConfiguration(ValidationProvider provider) {
		this.provider = provider;
	}

	public MyValidatorConfiguration ignoreXmlConfiguration() {
		throw new UnsupportedOperationException();
	}

	public MyValidatorConfiguration messageInterpolator(MessageInterpolator interpolator) {
		throw new UnsupportedOperationException();
	}

	public MyValidatorConfiguration traversableResolver(TraversableResolver resolver) {
		throw new UnsupportedOperationException();
	}

	public MyValidatorConfiguration constraintValidatorFactory(ConstraintValidatorFactory constraintValidatorFactory) {
		throw new UnsupportedOperationException();
	}

	public MyValidatorConfiguration addMapping(InputStream stream) {
		throw new UnsupportedOperationException();
	}

	public MyValidatorConfiguration addProperty(String name, String value) {
		throw new UnsupportedOperationException();
	}

	public MessageInterpolator getDefaultMessageInterpolator() {
		throw new UnsupportedOperationException();
	}

	public TraversableResolver getDefaultTraversableResolver() {
		throw new UnsupportedOperationException();
	}

	public ConstraintValidatorFactory getDefaultConstraintValidatorFactory() {
		throw new UnsupportedOperationException();
	}

	public ValidatorFactory buildValidatorFactory() {
		return provider.buildValidatorFactory( null );
	}
}


