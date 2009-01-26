// $Id$
/*
* JBoss, Home of Professional Open Source
* Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.hibernate.validation.impl;

import java.io.InputStream;
import java.util.List;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.MessageInterpolator;
import javax.validation.TraversableResolver;
import javax.validation.ValidationException;
import javax.validation.ValidationProviderResolver;
import javax.validation.ValidatorFactory;
import javax.validation.ValidatorFactoryBuilder;
import javax.validation.bootstrap.DefaultValidationProviderResolver;
import javax.validation.spi.BootstrapState;
import javax.validation.spi.ValidationProvider;
import javax.validation.spi.ValidatorFactoryConfiguration;

import org.hibernate.validation.HibernateValidatorFactoryBuilder;
import org.hibernate.validation.Version;

/**
 * @author Emmanuel Bernard
 */
public class ValidatorFactoryBuilderImpl implements HibernateValidatorFactoryBuilder, ValidatorFactoryConfiguration {

	static {
		Version.touch();
	}

	//FIXME not sure why it is like that. We should cache these instances somehow. Static?
	private final MessageInterpolator defaultMessageInterpolator = new ResourceBundleMessageInterpolator();
	private final TraversableResolver defaultTraversableResolver = new DefaultTraversableResolver();

	private MessageInterpolator messageInterpolator;
	private ConstraintValidatorFactory constraintValidatorFactory = new ConstraintValidatorFactoryImpl();
	private String configurationFile = "META-INF/validation.xml";
	private final ValidationProvider provider;
	private final ValidationProviderResolver providerResolver;
	private TraversableResolver traversableResolver;

	public ValidatorFactoryBuilderImpl(BootstrapState state) {
		if ( state.getValidationProviderResolver() == null ) {
			this.providerResolver = new DefaultValidationProviderResolver();
		}
		else {
			this.providerResolver = state.getValidationProviderResolver();
		}
		this.provider = null;
		this.messageInterpolator = defaultMessageInterpolator;
		this.traversableResolver = defaultTraversableResolver;
	}

	public ValidatorFactoryBuilderImpl(ValidationProvider provider) {
		if ( provider == null ) {
			throw new ValidationException( "Assertion error: inconsistent ValidatorFactoryBuilderImpl construction" );
		}
		this.provider = provider;
		this.providerResolver = null;
		this.messageInterpolator = defaultMessageInterpolator;
		this.traversableResolver = defaultTraversableResolver;
	}

	public ValidatorFactoryBuilderImpl messageInterpolator(MessageInterpolator interpolator) {
		this.messageInterpolator = interpolator;
		return this;
	}

	public ValidatorFactoryBuilderImpl traversableResolver(TraversableResolver resolver) {
		this.traversableResolver = resolver;
		return this;
	}

	public ValidatorFactoryBuilderImpl constraintValidatorFactory(ConstraintValidatorFactory constraintValidatorFactory) {
		this.constraintValidatorFactory = constraintValidatorFactory;
		return this;
	}

	public ValidatorFactory build() {
		if ( isSpecificProvider() ) {
			return provider.buildValidatorFactory( this );
		}
		else {
			//read provider name from configuration
			Class<? extends ValidatorFactoryBuilder<?>> providerClass = null;

			if ( providerClass != null ) {
				for ( ValidationProvider provider : providerResolver.getValidationProviders() ) {
					if ( provider.isSuitable( providerClass ) ) {
						return provider.buildValidatorFactory( this );
					}
				}
				throw new ValidationException( "Unable to find provider: " + providerClass );
			}
			else {
				List<ValidationProvider> providers = providerResolver.getValidationProviders();
				assert providers.size() != 0; //I run therefore I am
				return providers.get( 0 ).buildValidatorFactory( this );
			}
		}
	}

	private boolean isSpecificProvider() {
		return provider != null;
	}

	public MessageInterpolator getMessageInterpolator() {
		return messageInterpolator;
	}

	public ConstraintValidatorFactory getConstraintValidatorFactory() {
		return constraintValidatorFactory;
	}

	public TraversableResolver getTraversableResolver() {
		return traversableResolver;
	}

	public ValidatorFactoryBuilderImpl configure(InputStream stream) {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	public MessageInterpolator getDefaultMessageInterpolator() {
		return defaultMessageInterpolator;
	}

	public InputStream getConfigurationStream() {
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}
}
