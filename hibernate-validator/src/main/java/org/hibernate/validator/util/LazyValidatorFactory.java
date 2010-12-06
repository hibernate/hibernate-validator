/*
* JBoss, Home of Professional Open Source
* Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.validation.Configuration;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.MessageInterpolator;
import javax.validation.TraversableResolver;
import javax.validation.Validation;
import javax.validation.ValidationProviderResolver;
import javax.validation.Validator;
import javax.validation.ValidatorContext;
import javax.validation.ValidatorFactory;
import javax.validation.spi.ValidationProvider;

import org.hibernate.validator.HibernateValidator;

/**
 * This class lazily initialize the ValidatorFactory on the first usage
 * One benefit is that no domain class is loaded until the
 * ValidatorFactory is really needed.
 * Useful to avoid loading classes before JPA is initialized
 * and has enhanced its classes.
 *
 * When no Configuration is passed, the provider is Hibernate Validator
 *
 * This class is used by JBoss AS 6.
 *
 * Experimental, not considered a public API
 *
 * @author Emmanuel Bernard
 */
public class LazyValidatorFactory implements ValidatorFactory {

	private final Configuration<?> configuration;
	private volatile ValidatorFactory delegate; //use as a barrier

	/**
	 * Use the default ValidatorFactory creation routine
	 */
	public LazyValidatorFactory() {
		this( null );
	}

	public LazyValidatorFactory(Configuration<?> configuration) {
		this.configuration = configuration;
	}

	private ValidatorFactory getDelegate() {
		ValidatorFactory result = delegate;
		if ( result == null ) {
			synchronized ( this ) {
				result = delegate;
				if ( result == null ) {
					delegate = result = initFactory();
				}
			}
		}
		return result;
	}

	public Validator getValidator() {
		return getDelegate().getValidator();
	}

	//we can initialize several times that's ok

	private ValidatorFactory initFactory() {
		if ( configuration == null ) {
			return Validation
					.byDefaultProvider()
					.providerResolver( new HibernateProviderResolver() )
					.configure()
					.buildValidatorFactory();
		}
		else {
			return configuration.buildValidatorFactory();
		}
	}

	public ValidatorContext usingContext() {
		return getDelegate().usingContext();
	}

	public MessageInterpolator getMessageInterpolator() {
		return getDelegate().getMessageInterpolator();
	}

	public TraversableResolver getTraversableResolver() {
		return getDelegate().getTraversableResolver();
	}

	public ConstraintValidatorFactory getConstraintValidatorFactory() {
		return getDelegate().getConstraintValidatorFactory();
	}

	public <T> T unwrap(Class<T> clazz) {
		return getDelegate().unwrap( clazz );
	}

	private static class HibernateProviderResolver implements ValidationProviderResolver {
		private final List<ValidationProvider<?>> provider;

		private HibernateProviderResolver() {
			List<ValidationProvider<?>> provider = new ArrayList<ValidationProvider<?>>( 1 );
			provider.add( new HibernateValidator() );
			this.provider = Collections.unmodifiableList( provider );
		}

		public List<ValidationProvider<?>> getValidationProviders() {
			return provider;
		}
	}
}
