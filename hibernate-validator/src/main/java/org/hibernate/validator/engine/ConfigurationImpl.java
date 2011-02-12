/*
* JBoss, Home of Professional Open Source
* Copyright 2010, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.engine;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
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

import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.engine.resolver.DefaultTraversableResolver;
import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;
import org.hibernate.validator.resourceloading.PlatformResourceBundleLocator;
import org.hibernate.validator.resourceloading.ResourceBundleLocator;
import org.hibernate.validator.util.LoggerFactory;
import org.hibernate.validator.util.Version;
import org.hibernate.validator.xml.ValidationBootstrapParameters;
import org.hibernate.validator.xml.ValidationXmlParser;

/**
 * Hibernate specific {@code Configuration} implementation.
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Kevin Pollet - SERLI - (kevin.pollet@serli.com)
 */
public class ConfigurationImpl implements HibernateValidatorConfiguration, ConfigurationState {

	static {
		Version.touch();
	}

	private static final Logger log = LoggerFactory.make();

	private final ResourceBundleLocator defaultResourceBundleLocator = new PlatformResourceBundleLocator(
			ResourceBundleMessageInterpolator.USER_VALIDATION_MESSAGES
	);
	private final MessageInterpolator defaultMessageInterpolator = new ResourceBundleMessageInterpolator(
			defaultResourceBundleLocator
	);
	private final TraversableResolver defaultTraversableResolver = new DefaultTraversableResolver();
	private final ConstraintValidatorFactory defaultConstraintValidatorFactory = new ConstraintValidatorFactoryImpl();
	private final ValidationProviderResolver providerResolver;

	private ValidationBootstrapParameters validationBootstrapParameters;
	private boolean ignoreXmlConfiguration = false;
	private Set<InputStream> configurationStreams = new HashSet<InputStream>();
	private ConstraintMapping mapping;
	private boolean failFast;

	public ConfigurationImpl(BootstrapState state) {
		if ( state.getValidationProviderResolver() == null ) {
			this.providerResolver = state.getDefaultValidationProviderResolver();
		}
		else {
			this.providerResolver = state.getValidationProviderResolver();
		}
		validationBootstrapParameters = new ValidationBootstrapParameters();
	}

	public ConfigurationImpl(ValidationProvider<?> provider) {
		if ( provider == null ) {
			throw new ValidationException( "Assertion error: inconsistent ConfigurationImpl construction" );
		}
		this.providerResolver = null;
		validationBootstrapParameters = new ValidationBootstrapParameters();
		validationBootstrapParameters.setProvider( provider );
	}

	public final HibernateValidatorConfiguration ignoreXmlConfiguration() {
		ignoreXmlConfiguration = true;
		return this;
	}

	public final ConfigurationImpl messageInterpolator(MessageInterpolator interpolator) {
		this.validationBootstrapParameters.setMessageInterpolator( interpolator );
		return this;
	}

	public final ConfigurationImpl traversableResolver(TraversableResolver resolver) {
		this.validationBootstrapParameters.setTraversableResolver( resolver );
		return this;
	}

	public final ConfigurationImpl constraintValidatorFactory(ConstraintValidatorFactory constraintValidatorFactory) {
		this.validationBootstrapParameters.setConstraintValidatorFactory( constraintValidatorFactory );
		return this;
	}

	public final HibernateValidatorConfiguration addMapping(InputStream stream) {
		if ( stream == null ) {
			throw new IllegalArgumentException( "The stream cannot be null." );
		}
		validationBootstrapParameters.addMapping( stream );
		return this;
	}

	public final HibernateValidatorConfiguration failFast( boolean failFast) {
		this.failFast = failFast;
		return this;
	}

	public final HibernateValidatorConfiguration addMapping(ConstraintMapping mapping) {
		if ( mapping == null ) {
			throw new IllegalArgumentException( "The mapping cannot be null." );
		}
		this.mapping = mapping;
		return this;
	}

	public final HibernateValidatorConfiguration addProperty(String name, String value) {
		if ( value != null ) {
			validationBootstrapParameters.addConfigProperty( name, value );
		}
		return this;
	}

	public final ValidatorFactory buildValidatorFactory() {
		parseValidationXml();
		ValidatorFactory factory = null;
		try {
			if ( isSpecificProvider() ) {
				factory = validationBootstrapParameters.getProvider().buildValidatorFactory( this );
			}
			else {
				final Class<? extends ValidationProvider<?>> providerClass = validationBootstrapParameters.getProviderClass();
				if ( providerClass != null ) {
					for ( ValidationProvider<?> provider : providerResolver.getValidationProviders() ) {
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
		}
		finally {
			// close all input streams opened by this configuration
			for ( InputStream in : configurationStreams ) {
				try {
					in.close();
				}
				catch ( IOException io ) {
					log.warn( "Unable to close input stream." );
				}
			}
		}

		// reset the param holder
		validationBootstrapParameters = new ValidationBootstrapParameters();
		return factory;
	}

	public final boolean isIgnoreXmlConfiguration() {
		return ignoreXmlConfiguration;
	}

	public final MessageInterpolator getMessageInterpolator() {
		return validationBootstrapParameters.getMessageInterpolator();
	}

	public final Set<InputStream> getMappingStreams() {
		return validationBootstrapParameters.getMappings();
	}

	public final boolean getFailFast() {
		return failFast;
	}

	public final ConstraintValidatorFactory getConstraintValidatorFactory() {
		return validationBootstrapParameters.getConstraintValidatorFactory();
	}

	public final TraversableResolver getTraversableResolver() {
		return validationBootstrapParameters.getTraversableResolver();
	}

	public final Map<String, String> getProperties() {
		return validationBootstrapParameters.getConfigProperties();
	}

	public final MessageInterpolator getDefaultMessageInterpolator() {
		return defaultMessageInterpolator;
	}

	public final TraversableResolver getDefaultTraversableResolver() {
		return defaultTraversableResolver;
	}

	public final ConstraintValidatorFactory getDefaultConstraintValidatorFactory() {
		return defaultConstraintValidatorFactory;
	}

	public final ResourceBundleLocator getDefaultResourceBundleLocator() {
		return defaultResourceBundleLocator;
	}

	public final ConstraintMapping getMapping() {
		return mapping;
	}

	private boolean isSpecificProvider() {
		return validationBootstrapParameters.getProvider() != null;
	}

	/**
	 * Tries to check whether a validation.xml file exists and parses it using JAXB
	 */
	private void parseValidationXml() {
		if ( ignoreXmlConfiguration ) {
			log.info( "Ignoring XML configuration." );
			// make sure we use the defaults in case they haven't been provided yet
			if ( validationBootstrapParameters.getMessageInterpolator() == null ) {
				validationBootstrapParameters.setMessageInterpolator( defaultMessageInterpolator );
			}
			if ( validationBootstrapParameters.getTraversableResolver() == null ) {
				validationBootstrapParameters.setTraversableResolver( defaultTraversableResolver );
			}
			if ( validationBootstrapParameters.getConstraintValidatorFactory() == null ) {
				validationBootstrapParameters.setConstraintValidatorFactory( defaultConstraintValidatorFactory );
			}
		}
		else {
			ValidationBootstrapParameters xmlParameters = new ValidationXmlParser().parseValidationXml();
			applyXmlSettings( xmlParameters );
		}
	}

	private void applyXmlSettings(ValidationBootstrapParameters xmlParameters) {
		validationBootstrapParameters.setProviderClass( xmlParameters.getProviderClass() );

		if ( validationBootstrapParameters.getMessageInterpolator() == null ) {
			if ( xmlParameters.getMessageInterpolator() != null ) {
				validationBootstrapParameters.setMessageInterpolator( xmlParameters.getMessageInterpolator() );
			}
			else {
				validationBootstrapParameters.setMessageInterpolator( defaultMessageInterpolator );
			}
		}

		if ( validationBootstrapParameters.getTraversableResolver() == null ) {
			if ( xmlParameters.getTraversableResolver() != null ) {
				validationBootstrapParameters.setTraversableResolver( xmlParameters.getTraversableResolver() );
			}
			else {
				validationBootstrapParameters.setTraversableResolver( defaultTraversableResolver );
			}
		}

		if ( validationBootstrapParameters.getConstraintValidatorFactory() == null ) {
			if ( xmlParameters.getConstraintValidatorFactory() != null ) {
				validationBootstrapParameters.setConstraintValidatorFactory( xmlParameters.getConstraintValidatorFactory() );
			}
			else {
				validationBootstrapParameters.setConstraintValidatorFactory( defaultConstraintValidatorFactory );
			}
		}

		validationBootstrapParameters.addAllMappings( xmlParameters.getMappings() );
		configurationStreams.addAll( xmlParameters.getMappings() );

		for ( Map.Entry<String, String> entry : xmlParameters.getConfigProperties().entrySet() ) {
			if ( validationBootstrapParameters.getConfigProperties().get( entry.getKey() ) == null ) {
				validationBootstrapParameters.addConfigProperty( entry.getKey(), entry.getValue() );
			}
		}
	}
}
