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
package org.hibernate.validator.internal.engine;

import static org.hibernate.validator.internal.util.logging.Messages.MESSAGES;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.BootstrapConfiguration;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.MessageInterpolator;
import javax.validation.ParameterNameProvider;
import javax.validation.TraversableResolver;
import javax.validation.ValidationProviderResolver;
import javax.validation.ValidatorFactory;
import javax.validation.spi.BootstrapState;
import javax.validation.spi.ConfigurationState;
import javax.validation.spi.ValidationProvider;

import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.MethodValidationConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.internal.cfg.DefaultConstraintMapping;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorFactoryImpl;
import org.hibernate.validator.internal.engine.resolver.DefaultTraversableResolver;
import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.Version;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.xml.ValidationBootstrapParameters;
import org.hibernate.validator.internal.xml.ValidationXmlParser;
import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;
import org.hibernate.validator.resourceloading.PlatformResourceBundleLocator;
import org.hibernate.validator.spi.resourceloading.ResourceBundleLocator;

/**
 * Hibernate specific {@code Configuration} implementation.
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Kevin Pollet <kevin.pollet@serli.com> (C) 2011 SERLI
 * @author Chris Beckey cbeckey@paypal.com
 */
public class ConfigurationImpl implements HibernateValidatorConfiguration, ConfigurationState {

	static {
		Version.touch();
	}

	private static final Log log = LoggerFactory.make();

	private final ResourceBundleLocator defaultResourceBundleLocator;
	private final MessageInterpolator defaultMessageInterpolator;
	private final TraversableResolver defaultTraversableResolver;
	private final ConstraintValidatorFactory defaultConstraintValidatorFactory;
	private final ParameterNameProvider defaultParameterNameProvider;

	private ValidationProviderResolver providerResolver;
	private ValidationBootstrapParameters validationBootstrapParameters;
	private boolean ignoreXmlConfiguration = false;
	private Set<InputStream> configurationStreams = CollectionHelper.newHashSet();
	private Set<ConstraintMapping> programmaticMappings = CollectionHelper.newHashSet();
	private boolean failFast;
	private MethodValidationConfiguration methodValidationConfiguration = new MethodValidationConfigurationImpl();
	private BootstrapConfiguration bootstrapConfiguration;

	public ConfigurationImpl(BootstrapState state) {
		this();
		if ( state.getValidationProviderResolver() == null ) {
			this.providerResolver = state.getDefaultValidationProviderResolver();
		}
		else {
			this.providerResolver = state.getValidationProviderResolver();
		}
	}

	public ConfigurationImpl(ValidationProvider<?> provider) {
		this();
		if ( provider == null ) {
			throw log.getInconsistentConfigurationException();
		}
		this.providerResolver = null;
		validationBootstrapParameters.setProvider( provider );
	}

	private ConfigurationImpl() {
		this.validationBootstrapParameters = new ValidationBootstrapParameters();
		this.defaultResourceBundleLocator = new PlatformResourceBundleLocator( ResourceBundleMessageInterpolator.USER_VALIDATION_MESSAGES );
		this.defaultTraversableResolver = new DefaultTraversableResolver();
		this.defaultConstraintValidatorFactory = new ConstraintValidatorFactoryImpl();
		this.defaultParameterNameProvider = new DefaultParameterNameProvider();
		this.defaultMessageInterpolator = new ResourceBundleMessageInterpolator( defaultResourceBundleLocator );
	}

	public final HibernateValidatorConfiguration ignoreXmlConfiguration() {
		ignoreXmlConfiguration = true;
		return this;
	}

	public final ConfigurationImpl messageInterpolator(MessageInterpolator interpolator) {
		if ( log.isDebugEnabled() ) {
			if ( interpolator != null ) {
				log.debug( "Setting custom MessageInterpolator of type " + interpolator.getClass().getName() );
			}
		}
		this.validationBootstrapParameters.setMessageInterpolator( interpolator );
		return this;
	}

	public final ConfigurationImpl traversableResolver(TraversableResolver resolver) {
		if ( log.isDebugEnabled() ) {
			if ( resolver != null ) {
				log.debug( "Setting custom TraversableResolver of type " + resolver.getClass().getName() );
			}
		}
		this.validationBootstrapParameters.setTraversableResolver( resolver );
		return this;
	}

	public final ConfigurationImpl constraintValidatorFactory(ConstraintValidatorFactory constraintValidatorFactory) {
		if ( log.isDebugEnabled() ) {
			if ( constraintValidatorFactory != null ) {
				log.debug(
						"Setting custom ConstraintValidatorFactory of type " + constraintValidatorFactory.getClass()
								.getName()
				);
			}
		}
		this.validationBootstrapParameters.setConstraintValidatorFactory( constraintValidatorFactory );
		return this;
	}

	@Override
	public HibernateValidatorConfiguration parameterNameProvider(ParameterNameProvider parameterNameProvider) {
		if ( log.isDebugEnabled() ) {
			if ( parameterNameProvider != null ) {
				log.debug(
						"Setting custom ParameterNameProvider of type " + parameterNameProvider.getClass()
								.getName()
				);
			}
		}
		this.validationBootstrapParameters.setParameterNameProvider( parameterNameProvider );
		return this;
	}

	public final HibernateValidatorConfiguration addMapping(InputStream stream) {
		Contracts.assertNotNull( stream, MESSAGES.inputStreamCannotBeNull() );

		validationBootstrapParameters.addMapping( stream.markSupported() ? stream : new BufferedInputStream( stream ) );
		return this;
	}

	public final HibernateValidatorConfiguration failFast(boolean failFast) {
		this.failFast = failFast;
		return this;
	}

	
	@Override
	public HibernateValidatorConfiguration setMethodValidationConfiguration(MethodValidationConfiguration mvc) {
		// the MethodValidationConfiguration cannot be null, it will cause NPE later if it is
		if(mvc != null)
			this.methodValidationConfiguration = mvc;
		else if ( log.isDebugEnabled() ) {
			log.warn( "Attempt to set the MethodValidationConfiguration to null is being ignored." );
		}

		return this;
	}

	@Override
	public MethodValidationConfiguration getMethodValidationConfiguration() {
		return this.methodValidationConfiguration;
	}

	public final ConstraintMapping createConstraintMapping() {
		return new DefaultConstraintMapping();
	}

	public final HibernateValidatorConfiguration addMapping(ConstraintMapping mapping) {
		Contracts.assertNotNull( mapping, MESSAGES.parameterMustNotBeNull( "mapping" ) );

		this.programmaticMappings.add( mapping );
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
						throw log.getUnableToFindProviderException( providerClass );
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
					log.unableToCloseInputStream();
				}
			}
		}

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

	@Override
	public BootstrapConfiguration getBootstrapConfiguration() {
		if ( bootstrapConfiguration == null ) {
			bootstrapConfiguration = new ValidationXmlParser().parseValidationXml();
		}
		return bootstrapConfiguration;
	}

	@Override
	public ParameterNameProvider getParameterNameProvider() {
		return validationBootstrapParameters.getParameterNameProvider();
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

	@Override
	public ParameterNameProvider getDefaultParameterNameProvider() {
		return defaultParameterNameProvider;
	}

	public final Set<ConstraintMapping> getProgrammaticMappings() {
		return programmaticMappings;
	}

	private boolean isSpecificProvider() {
		return validationBootstrapParameters.getProvider() != null;
	}

	/**
	 * Tries to check whether a validation.xml file exists and parses it
	 */
	private void parseValidationXml() {
		if ( ignoreXmlConfiguration ) {
			log.ignoringXmlConfiguration();

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
			if ( validationBootstrapParameters.getParameterNameProvider() == null ) {
				validationBootstrapParameters.setParameterNameProvider( defaultParameterNameProvider );
			}
		}
		else {
			ValidationBootstrapParameters xmlParameters = new ValidationBootstrapParameters( getBootstrapConfiguration() );
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

		if ( validationBootstrapParameters.getParameterNameProvider() == null ) {
			if ( xmlParameters.getParameterNameProvider() != null ) {
				validationBootstrapParameters.setParameterNameProvider( xmlParameters.getParameterNameProvider() );
			}
			else {
				validationBootstrapParameters.setParameterNameProvider( defaultParameterNameProvider );
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
