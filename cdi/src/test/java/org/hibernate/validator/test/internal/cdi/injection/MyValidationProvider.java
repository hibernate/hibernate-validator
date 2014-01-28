/*
* JBoss, Home of Professional Open Source
* Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual contributors
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.hibernate.validator.test.internal.cdi.injection;

import java.io.InputStream;
import java.util.Set;
import javax.enterprise.inject.Alternative;
import javax.validation.BootstrapConfiguration;
import javax.validation.Configuration;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.ConstraintViolation;
import javax.validation.MessageInterpolator;
import javax.validation.ParameterNameProvider;
import javax.validation.TraversableResolver;
import javax.validation.Validator;
import javax.validation.ValidatorContext;
import javax.validation.ValidatorFactory;
import javax.validation.executable.ExecutableValidator;
import javax.validation.metadata.BeanDescriptor;
import javax.validation.spi.BootstrapState;
import javax.validation.spi.ConfigurationState;
import javax.validation.spi.ValidationProvider;

import org.hibernate.validator.internal.engine.ValidatorFactoryImpl;

/**
 * A {@link ValidationProvider} for testing purposes.
 *
 * @author Gunnar Morling
 */
public class MyValidationProvider implements ValidationProvider<MyValidationProvider.MyConfiguration> {

	@Override
	public MyConfiguration createSpecializedConfiguration(BootstrapState state) {
		return null;
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
		public BootstrapConfiguration getBootstrapConfiguration() {
			return null;
		}

		@Override
		public ValidatorFactory buildValidatorFactory() {
			return null;
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
