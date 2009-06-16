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
package org.hibernate.validation.engine;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.MessageInterpolator;
import javax.validation.TraversableResolver;
import javax.validation.ValidationException;
import javax.validation.ValidationProviderResolver;
import javax.validation.ValidatorFactory;
import javax.validation.spi.BootstrapState;
import javax.validation.spi.ConfigurationState;
import javax.validation.spi.ValidationProvider;

import org.slf4j.Logger;

import org.hibernate.validation.engine.resolver.DefaultTraversableResolver;
import org.hibernate.validation.engine.xml.ValidationBootstrapParameters;
import org.hibernate.validation.engine.xml.ValidationXmlParser;
import org.hibernate.validation.util.LoggerFactory;
import org.hibernate.validation.util.Version;

/**
 * Hibernate specific <code>Configuration</code> implementation.
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 */
public class ConfigurationImpl implements HibernateValidatorConfiguration, ConfigurationState {

	static {
		Version.touch();
	}

	private static final Logger log = LoggerFactory.make();

	private final MessageInterpolator defaultMessageInterpolator = new ResourceBundleMessageInterpolator();
	private final TraversableResolver defaultTraversableResolver = new DefaultTraversableResolver();
	private final ConstraintValidatorFactory defaultValidatorFactory = new ConstraintValidatorFactoryImpl();
	private final ValidationProviderResolver providerResolver;

	private ValidationBootstrapParameters validationBootstrapParameters;
	private boolean ignoreXmlConfiguration = false;

	public ConfigurationImpl(BootstrapState state) {
		if ( state.getValidationProviderResolver() == null ) {
			this.providerResolver = state.getDefaultValidationProviderResolver();
		}
		else {
			this.providerResolver = state.getValidationProviderResolver();
		}
		validationBootstrapParameters = new ValidationBootstrapParameters();
	}

	public ConfigurationImpl(ValidationProvider provider) {
		if ( provider == null ) {
			throw new ValidationException( "Assertion error: inconsistent ConfigurationImpl construction" );
		}
		this.providerResolver = null;
		validationBootstrapParameters = new ValidationBootstrapParameters();
		validationBootstrapParameters.provider = provider;
	}

	public HibernateValidatorConfiguration ignoreXmlConfiguration() {
		ignoreXmlConfiguration = true;
		return this;
	}

	public ConfigurationImpl messageInterpolator(MessageInterpolator interpolator) {
		this.validationBootstrapParameters.messageInterpolator = interpolator;
		return this;
	}

	public ConfigurationImpl traversableResolver(TraversableResolver resolver) {
		this.validationBootstrapParameters.traversableResolver = resolver;
		return this;
	}

	public ConfigurationImpl constraintValidatorFactory(ConstraintValidatorFactory constraintValidatorFactory) {
		this.validationBootstrapParameters.constraintValidatorFactory = constraintValidatorFactory;
		return this;
	}

	public HibernateValidatorConfiguration addMapping(InputStream stream) {
		validationBootstrapParameters.mappings.add( stream );
		return this;
	}

	public HibernateValidatorConfiguration addProperty(String name, String value) {
		if ( value != null ) {
			validationBootstrapParameters.configProperties.put( name, value );
		}
		return this;
	}

	public ValidatorFactory buildValidatorFactory() {
		parseValidationXml();
		ValidatorFactory factory = null;
		if ( isSpecificProvider() ) {
			factory = validationBootstrapParameters.provider.buildValidatorFactory( this );
		}
		else {
			final Class<? extends ValidationProvider<?>> providerClass = validationBootstrapParameters.providerClass;
			if ( providerClass != null ) {
				for ( ValidationProvider provider : providerResolver.getValidationProviders() ) {
					if ( providerClass.isAssignableFrom( provider.getClass() ) ) {
						factory = provider.buildValidatorFactory( this );
						break;
					}
				}
				if ( factory == null ) {
					throw new ValidationException( "Unable to find provider: " + providerClass );
				}
			}
			else {
				List<ValidationProvider<?>> providers = providerResolver.getValidationProviders();
				assert providers.size() != 0; // I run therefore I am
				factory = providers.get( 0 ).buildValidatorFactory( this );
			}
		}

		// reset the param holder
		validationBootstrapParameters = new ValidationBootstrapParameters();
		return factory;
	}

	public boolean isIgnoreXmlConfiguration() {
		return ignoreXmlConfiguration;
	}

	public MessageInterpolator getMessageInterpolator() {
		return validationBootstrapParameters.messageInterpolator;
	}

	public Set<InputStream> getMappingStreams() {
		return validationBootstrapParameters.mappings;
	}

	public ConstraintValidatorFactory getConstraintValidatorFactory() {
		return validationBootstrapParameters.constraintValidatorFactory;
	}

	public TraversableResolver getTraversableResolver() {
		return validationBootstrapParameters.traversableResolver;
	}

	public Map<String, String> getProperties() {
		return validationBootstrapParameters.configProperties;
	}

	public MessageInterpolator getDefaultMessageInterpolator() {
		return defaultMessageInterpolator;
	}

	private boolean isSpecificProvider() {
		return validationBootstrapParameters.provider != null;
	}

	/**
	 * Tries to check whether a validation.xml file exists and parses it using JAXB
	 */
	private void parseValidationXml() {
		if ( ignoreXmlConfiguration ) {
			log.info( "Ignoring XML configuration." );
			return;
		}

		ValidationBootstrapParameters xmlParameters = new ValidationXmlParser().parseValidationXml();
		applyXmlSettings( xmlParameters );
	}

	private void applyXmlSettings(ValidationBootstrapParameters xmlParameters) {
		validationBootstrapParameters.providerClass = xmlParameters.providerClass;

		if ( validationBootstrapParameters.messageInterpolator == null ) {
			if ( xmlParameters.messageInterpolator != null ) {
				validationBootstrapParameters.messageInterpolator = xmlParameters.messageInterpolator;
			}
			else {
				validationBootstrapParameters.messageInterpolator = defaultMessageInterpolator;
			}
		}

		if ( validationBootstrapParameters.traversableResolver == null ) {
			if ( xmlParameters.traversableResolver != null ) {
				validationBootstrapParameters.traversableResolver = xmlParameters.traversableResolver;
			}
			else {
				validationBootstrapParameters.traversableResolver = defaultTraversableResolver;
			}
		}

		if ( validationBootstrapParameters.constraintValidatorFactory == null ) {
			if ( xmlParameters.constraintValidatorFactory != null ) {
				validationBootstrapParameters.constraintValidatorFactory = xmlParameters.constraintValidatorFactory;
			}
			else {
				validationBootstrapParameters.constraintValidatorFactory = defaultValidatorFactory;
			}
		}

		validationBootstrapParameters.mappings.addAll( xmlParameters.mappings );

		for ( Map.Entry<String, String> entry : xmlParameters.configProperties.entrySet() ) {
			if ( validationBootstrapParameters.configProperties.get( entry.getKey() ) == null ) {
				validationBootstrapParameters.configProperties.put( entry.getKey(), entry.getValue() );
			}
		}
	}
}
