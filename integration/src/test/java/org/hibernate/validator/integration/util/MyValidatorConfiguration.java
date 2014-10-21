/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.integration.util;

import java.io.InputStream;
import javax.validation.BootstrapConfiguration;
import javax.validation.Configuration;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.MessageInterpolator;
import javax.validation.ParameterNameProvider;
import javax.validation.TraversableResolver;
import javax.validation.ValidatorFactory;
import javax.validation.spi.ValidationProvider;

/**
 * @author Hardy Ferentschik
 */
public class MyValidatorConfiguration implements Configuration<MyValidatorConfiguration> {

	private final ValidationProvider<?> provider;

	public MyValidatorConfiguration() {
		provider = null;
	}

	public MyValidatorConfiguration(ValidationProvider<?> provider) {
		this.provider = provider;
	}

	@Override
	public MyValidatorConfiguration ignoreXmlConfiguration() {
		throw new UnsupportedOperationException();
	}

	@Override
	public MyValidatorConfiguration messageInterpolator(MessageInterpolator interpolator) {
		throw new UnsupportedOperationException();
	}

	@Override
	public MyValidatorConfiguration traversableResolver(TraversableResolver resolver) {
		throw new UnsupportedOperationException();
	}

	@Override
	public MyValidatorConfiguration constraintValidatorFactory(ConstraintValidatorFactory constraintValidatorFactory) {
		throw new UnsupportedOperationException();
	}

	@Override
	public MyValidatorConfiguration addMapping(InputStream stream) {
		throw new UnsupportedOperationException();
	}

	@Override
	public MyValidatorConfiguration addProperty(String name, String value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public MessageInterpolator getDefaultMessageInterpolator() {
		throw new UnsupportedOperationException();
	}

	@Override
	public TraversableResolver getDefaultTraversableResolver() {
		throw new UnsupportedOperationException();
	}

	@Override
	public ConstraintValidatorFactory getDefaultConstraintValidatorFactory() {
		throw new UnsupportedOperationException();
	}

	@Override
	public ValidatorFactory buildValidatorFactory() {
		return provider.buildValidatorFactory( null );
	}

	@Override
	public MyValidatorConfiguration parameterNameProvider(ParameterNameProvider parameterNameProvider) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ParameterNameProvider getDefaultParameterNameProvider() {
		throw new UnsupportedOperationException();
	}

	@Override
	public BootstrapConfiguration getBootstrapConfiguration() {
		throw new UnsupportedOperationException();
	}
}
