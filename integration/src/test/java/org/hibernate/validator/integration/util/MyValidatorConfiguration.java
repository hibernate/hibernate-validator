/*
* JBoss, Home of Professional Open Source
* Copyright 2012, Red Hat, Inc. and/or its affiliates, and individual contributors
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
