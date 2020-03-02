/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.integration.util;

import java.io.InputStream;

import jakarta.validation.BootstrapConfiguration;
import jakarta.validation.ClockProvider;
import jakarta.validation.Configuration;
import jakarta.validation.ConstraintValidatorFactory;
import jakarta.validation.MessageInterpolator;
import jakarta.validation.ParameterNameProvider;
import jakarta.validation.TraversableResolver;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.spi.ValidationProvider;
import jakarta.validation.valueextraction.ValueExtractor;

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
	public MyValidatorConfiguration parameterNameProvider(ParameterNameProvider parameterNameProvider) {
		throw new UnsupportedOperationException();
	}

	@Override
	public MyValidatorConfiguration clockProvider(ClockProvider clockProvider) {
		throw new UnsupportedOperationException();
	}

	@Override
	public MyValidatorConfiguration addValueExtractor(ValueExtractor<?> extractor) {
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
	public ParameterNameProvider getDefaultParameterNameProvider() {
		throw new UnsupportedOperationException();
	}

	@Override
	public ClockProvider getDefaultClockProvider() {
		throw new UnsupportedOperationException();
	}

	@Override
	public ValidatorFactory buildValidatorFactory() {
		return provider.buildValidatorFactory( null );
	}

	@Override
	public BootstrapConfiguration getBootstrapConfiguration() {
		throw new UnsupportedOperationException();
	}
}
