/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.cdi.internal.injection;

import java.io.InputStream;
import java.util.Set;

import jakarta.enterprise.inject.Alternative;
import jakarta.validation.BootstrapConfiguration;
import jakarta.validation.ClockProvider;
import jakarta.validation.Configuration;
import jakarta.validation.ConstraintValidatorFactory;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.MessageInterpolator;
import jakarta.validation.ParameterNameProvider;
import jakarta.validation.TraversableResolver;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorContext;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.executable.ExecutableValidator;
import jakarta.validation.metadata.BeanDescriptor;
import jakarta.validation.spi.BootstrapState;
import jakarta.validation.spi.ConfigurationState;
import jakarta.validation.spi.ValidationProvider;
import jakarta.validation.valueextraction.ValueExtractor;

import org.hibernate.validator.internal.engine.ValidatorFactoryImpl;

/**
 * A {@link ValidationProvider} for testing purposes.
 *
 * @author Gunnar Morling
 */
public class MyValidationProvider implements ValidationProvider<MyValidationProvider.MyConfiguration> {

	@Override
	public MyConfiguration createSpecializedConfiguration(BootstrapState state) {
		return new MyConfiguration();
	}

	@Override
	public Configuration<?> createGenericConfiguration(BootstrapState state) {
		return null;
	}

	@Override
	public ValidatorFactory buildValidatorFactory(ConfigurationState configurationState) {
		return new MyValidatorFactory( configurationState );
	}

	public static class MyConfiguration implements Configuration<MyConfiguration> {

		@Override
		public MyConfiguration ignoreXmlConfiguration() {
			return null;
		}

		@Override
		public MyConfiguration messageInterpolator(MessageInterpolator interpolator) {
			return null;
		}

		@Override
		public MyConfiguration traversableResolver(TraversableResolver resolver) {
			return null;
		}

		@Override
		public MyConfiguration constraintValidatorFactory(ConstraintValidatorFactory constraintValidatorFactory) {
			return null;
		}

		@Override
		public MyConfiguration parameterNameProvider(ParameterNameProvider parameterNameProvider) {
			return null;
		}

		@Override
		public MyConfiguration clockProvider(ClockProvider clockProvider) {
			return null;
		}

		@Override
		public MyConfiguration addValueExtractor(ValueExtractor<?> extractor) {
			return null;
		}

		@Override
		public MyConfiguration addMapping(InputStream stream) {
			return null;
		}

		@Override
		public MyConfiguration addProperty(String name, String value) {
			return null;
		}

		@Override
		public MessageInterpolator getDefaultMessageInterpolator() {
			return null;
		}

		@Override
		public TraversableResolver getDefaultTraversableResolver() {
			return null;
		}

		@Override
		public ConstraintValidatorFactory getDefaultConstraintValidatorFactory() {
			return null;
		}

		@Override
		public ParameterNameProvider getDefaultParameterNameProvider() {
			return null;
		}

		@Override
		public ClockProvider getDefaultClockProvider() {
			return null;
		}

		@Override
		public BootstrapConfiguration getBootstrapConfiguration() {
			return null;
		}

		@Override
		public ValidatorFactory buildValidatorFactory() {
			return new MyValidatorFactory();
		}
	}

	@Alternative
	public static class MyValidatorFactory implements ValidatorFactory {

		private final ValidatorFactory delegate;

		// Only for making the class proxyable
		MyValidatorFactory() {
			this.delegate = null;
		}

		public MyValidatorFactory(ConfigurationState configurationState) {
			delegate = new ValidatorFactoryImpl( configurationState );
		}

		@Override
		public Validator getValidator() {
			return new MyValidator( delegate.getValidator() );
		}

		@Override
		public ValidatorContext usingContext() {
			return delegate.usingContext();
		}

		@Override
		public MessageInterpolator getMessageInterpolator() {
			return delegate.getMessageInterpolator();
		}

		@Override
		public TraversableResolver getTraversableResolver() {
			return delegate.getTraversableResolver();
		}

		@Override
		public ConstraintValidatorFactory getConstraintValidatorFactory() {
			return delegate.getConstraintValidatorFactory();
		}

		@Override
		public ParameterNameProvider getParameterNameProvider() {
			return delegate.getParameterNameProvider();
		}

		@Override
		public ClockProvider getClockProvider() {
			return delegate.getClockProvider();
		}

		@Override
		@SuppressWarnings("unchecked")
		public <T> T unwrap(Class<T> type) {
			if ( type == MyValidatorFactory.class ) {
				return (T) this;
			}
			else {
				throw new IllegalArgumentException( "Unsupported type for unwrapping: " + type );
			}
		}

		@Override
		public void close() {
			delegate.close();
		}
	}

	@Alternative
	public static class MyValidator implements Validator {

		private final Validator delegate;

		// Only for making this class proxyable
		MyValidator() {
			this.delegate = null;
		}

		public MyValidator(Validator delegate) {
			this.delegate = delegate;
		}

		@Override
		public <T> Set<ConstraintViolation<T>> validate(T object, Class<?>... groups) {
			return delegate.validate( object, groups );
		}

		@Override
		public <T> Set<ConstraintViolation<T>> validateProperty(T object, String propertyName, Class<?>... groups) {
			return delegate.validateProperty( object, propertyName, groups );
		}

		@Override
		public <T> Set<ConstraintViolation<T>> validateValue(Class<T> beanType, String propertyName, Object value,
				Class<?>... groups) {
			return delegate.validateValue( beanType, propertyName, value, groups );
		}

		@Override
		public BeanDescriptor getConstraintsForClass(Class<?> clazz) {
			return delegate.getConstraintsForClass( clazz );
		}

		@Override
		public <T> T unwrap(Class<T> type) {
			return delegate.unwrap( type );
		}

		@Override
		public ExecutableValidator forExecutables() {
			return delegate.forExecutables();
		}
	}
}
