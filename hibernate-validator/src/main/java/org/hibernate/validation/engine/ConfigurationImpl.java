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
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.Configuration;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.MessageInterpolator;
import javax.validation.TraversableResolver;
import javax.validation.ValidationException;
import javax.validation.ValidationProviderResolver;
import javax.validation.ValidatorFactory;
import javax.validation.spi.BootstrapState;
import javax.validation.spi.ConfigurationState;
import javax.validation.spi.ValidationProvider;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.slf4j.Logger;
import org.xml.sax.SAXException;

import org.hibernate.validation.engine.resolver.DefaultTraversableResolver;
import org.hibernate.validation.util.LoggerFactory;
import org.hibernate.validation.util.ReflectionHelper;
import org.hibernate.validation.util.Version;
import org.hibernate.validation.xml.PropertyType;
import org.hibernate.validation.xml.ValidationConfigType;

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
	private static final String VALIDATION_XML_FILE = "/META-INF/validation.xml";
	private static final String VALIDATION_CONFIGURATION_XSD = "META-INF/validation-configuration-1.0.xsd";
	private static final MessageInterpolator defaultMessageInterpolator = new ResourceBundleMessageInterpolator();
	private static final TraversableResolver defaultTraversableResolver = new DefaultTraversableResolver();
	private static final ConstraintValidatorFactory defaultValidatorFactory = new ConstraintValidatorFactoryImpl();

	private final ValidationProviderResolver providerResolver;

	private ParameterHolder parameterHolder;
	private boolean ignoreXmlConfiguration = false;

	public ConfigurationImpl(BootstrapState state) {
		if ( state.getValidationProviderResolver() == null ) {
			this.providerResolver = new DefaultValidationProviderResolver();
		}
		else {
			this.providerResolver = state.getValidationProviderResolver();
		}
		parameterHolder = new ParameterHolder();
	}

	public ConfigurationImpl(ValidationProvider provider) {
		if ( provider == null ) {
			throw new ValidationException( "Assertion error: inconsistent ConfigurationImpl construction" );
		}
		this.providerResolver = null;
		parameterHolder = new ParameterHolder();
		parameterHolder.provider = provider;
	}

	public HibernateValidatorConfiguration ignoreXmlConfiguration() {
		ignoreXmlConfiguration = true;
		return this;
	}

	public ConfigurationImpl messageInterpolator(MessageInterpolator interpolator) {
		this.parameterHolder.messageInterpolator = interpolator;
		return this;
	}

	public ConfigurationImpl traversableResolver(TraversableResolver resolver) {
		this.parameterHolder.traversableResolver = resolver;
		return this;
	}

	public ConfigurationImpl constraintValidatorFactory(ConstraintValidatorFactory constraintValidatorFactory) {
		this.parameterHolder.constraintValidatorFactory = constraintValidatorFactory;
		return this;
	}

	public HibernateValidatorConfiguration addMapping(InputStream stream) {
		parameterHolder.mappings.add( stream );
		return this;
	}

	public HibernateValidatorConfiguration addProperty(String name, String value) {
		if ( value != null ) {
			parameterHolder.configProperties.put( name, value );
		}
		return this;
	}

	public ValidatorFactory buildValidatorFactory() {
		parseValidationXml();
		if ( isSpecificProvider() ) {
			return parameterHolder.provider.buildValidatorFactory( this );
		}
		else {
			if ( parameterHolder.providerClass != null ) {
				for ( ValidationProvider provider : providerResolver.getValidationProviders() ) {
					if ( provider.isSuitable( parameterHolder.providerClass ) ) {
						return provider.buildValidatorFactory( this );
					}
				}
				throw new ValidationException( "Unable to find provider: " + parameterHolder.providerClass );
			}
			else {
				List<ValidationProvider> providers = providerResolver.getValidationProviders();
				assert providers.size() != 0; // I run therefore I am
				return providers.get( 0 ).buildValidatorFactory( this );
			}
		}
	}

	public boolean isIgnoreXmlConfiguration() {
		return ignoreXmlConfiguration;
	}

	public MessageInterpolator getMessageInterpolator() {
		return parameterHolder.messageInterpolator;
	}

	public Set<InputStream> getMappingStreams() {
		return parameterHolder.mappings;
	}

	public ConstraintValidatorFactory getConstraintValidatorFactory() {
		return parameterHolder.constraintValidatorFactory;
	}

	public TraversableResolver getTraversableResolver() {
		return parameterHolder.traversableResolver;
	}

	public Map<String, String> getProperties() {
		return parameterHolder.configProperties;
	}

	public MessageInterpolator getDefaultMessageInterpolator() {
		return defaultMessageInterpolator;
	}

	private boolean isSpecificProvider() {
		return parameterHolder.provider != null;
	}

	/**
	 * Tries to check whether a validation.xml file exists and parses it using JAXB
	 */
	private void parseValidationXml() {
		if ( ignoreXmlConfiguration ) {
			log.info( "Ignoring XML configuration." );
			return;
		}

		ValidationConfigType config = getValidationConfig();
		if ( config == null ) {
			return;
		}

		// collect the paramters from the xml file
		ParameterHolder xmlParameters = new ParameterHolder();
		setProviderClassFromXml( config, xmlParameters );
		setMessageInterpolatorFromXml( config, xmlParameters );
		setTraversableResolverFromXml( config, xmlParameters );
		setConstraintFactoryFromXml( config, xmlParameters );
		setMappingStreamsFromXml( config, xmlParameters );
		setPropertiesFromXml( config, xmlParameters );

		applyXmlSettings( xmlParameters );
	}

	private void applyXmlSettings(ParameterHolder xmlParameters) {
		parameterHolder.providerClass = xmlParameters.providerClass;

		if ( parameterHolder.messageInterpolator == null ) {
			if ( xmlParameters.messageInterpolator != null ) {
				parameterHolder.messageInterpolator = xmlParameters.messageInterpolator;
			}
			else {
				parameterHolder.messageInterpolator = defaultMessageInterpolator;
			}
		}

		if ( parameterHolder.traversableResolver == null ) {
			if ( xmlParameters.traversableResolver != null ) {
				parameterHolder.traversableResolver = xmlParameters.traversableResolver;
			}
			else {
				parameterHolder.traversableResolver = defaultTraversableResolver;
			}
		}

		if ( parameterHolder.constraintValidatorFactory == null ) {
			if ( xmlParameters.constraintValidatorFactory != null ) {
				parameterHolder.constraintValidatorFactory = xmlParameters.constraintValidatorFactory;
			}
			else {
				parameterHolder.constraintValidatorFactory = defaultValidatorFactory;
			}
		}

		parameterHolder.mappings.addAll( xmlParameters.mappings );

		for ( Map.Entry<String, String> entry : xmlParameters.configProperties.entrySet() ) {
			if ( parameterHolder.configProperties.get( entry.getKey() ) == null ) {
				parameterHolder.configProperties.put( entry.getKey(), entry.getValue() );
			}
		}
	}

	private void setConstraintFactoryFromXml(ValidationConfigType config, ParameterHolder xmlParameters) {
		String constraintFactoryClass = config.getConstraintValidatorFactory();
		if ( constraintFactoryClass != null ) {
			try {
				@SuppressWarnings("unchecked")
				Class<ConstraintValidatorFactory> clazz = ( Class<ConstraintValidatorFactory> ) ReflectionHelper.classForName(
						constraintFactoryClass, this.getClass()
				);
				xmlParameters.constraintValidatorFactory = clazz.newInstance();
				log.info( "Using {} as constraint factory.", constraintFactoryClass );
			}
			catch ( Exception e ) {
				throw new ValidationException( "Unable to instantiate message interpolator class " + constraintFactoryClass + "." );
			}
		}
	}

	private void setPropertiesFromXml(ValidationConfigType config, ParameterHolder xmlParameters) {
		for ( PropertyType property : config.getProperty() ) {
			if ( log.isDebugEnabled() ) {
				log.debug(
						"Found property '{}' with value '{}' in validation.xml.",
						property.getName(),
						property.getValue()
				);
			}
			xmlParameters.configProperties.put( property.getName(), property.getValue() );
		}
	}

	private void setMappingStreamsFromXml(ValidationConfigType config, ParameterHolder xmlParameters) {
		for ( String mappingFileName : config.getConstraintMapping() ) {
			if ( log.isDebugEnabled() ) {
				log.debug(
						"Trying to open input stream for {}.", mappingFileName
				);
				InputStream in = getInputStreamForPath( mappingFileName );
				if ( in == null ) {
					throw new ValidationException( " Unable to open input stream for mapping file." );
				}
				xmlParameters.mappings.add( in );
			}
		}
	}

	private void setMessageInterpolatorFromXml(ValidationConfigType config, ParameterHolder xmlParameters) {
		String messageInterpolatorClass = config.getMessageInterpolator();
		if ( messageInterpolatorClass != null ) {
			try {
				@SuppressWarnings("unchecked")
				Class<MessageInterpolator> clazz = ( Class<MessageInterpolator> ) ReflectionHelper.classForName(
						messageInterpolatorClass, this.getClass()
				);
				xmlParameters.messageInterpolator = clazz.newInstance();
				log.info( "Using {} as message interpolator.", messageInterpolatorClass );
			}
			catch ( Exception e ) {
				throw new ValidationException( "Unable to instantiate message interpolator class " + messageInterpolatorClass + "." );
			}
		}
	}

	private void setTraversableResolverFromXml(ValidationConfigType config, ParameterHolder xmlParameters) {
		String traversableResolverClass = config.getTraversableResolver();
		if ( traversableResolverClass != null ) {
			try {
				@SuppressWarnings("unchecked")
				Class<TraversableResolver> clazz = ( Class<TraversableResolver> ) ReflectionHelper.classForName(
						traversableResolverClass, this.getClass()
				);
				xmlParameters.traversableResolver = clazz.newInstance();
				log.info( "Using {} as traversable resolver.", traversableResolverClass );
			}
			catch ( Exception e ) {
				throw new ValidationException( "Unable to instantiate message interpolator class " + traversableResolverClass + "." );
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void setProviderClassFromXml(ValidationConfigType config, ParameterHolder xmlParamters) {
		String providerClassName = config.getDefaultProvider();
		if ( providerClassName != null ) {
			try {
				xmlParamters.providerClass = ( Class<? extends Configuration<?>> ) ReflectionHelper.classForName(
						providerClassName, this.getClass()
				);
				log.info( "Using {} as validation provider.", providerClassName );
			}
			catch ( Exception e ) {
				throw new ValidationException( "Unable to instantiate validation provider class " + providerClassName + "." );
			}
		}
	}

	private ValidationConfigType getValidationConfig() {
		InputStream inputStream = getInputStreamForPath( VALIDATION_XML_FILE );
		if ( inputStream == null ) {
			log.info( "No {} found. Using defaults.", VALIDATION_XML_FILE );
			return null;
		}

		ValidationConfigType validationConfig = null;
		Schema schema = getValidationConfigurationSchema();
		try {
			JAXBContext jc = JAXBContext.newInstance( ValidationConfigType.class );
			Unmarshaller unmarshaller = jc.createUnmarshaller();
			unmarshaller.setSchema( schema );
			StreamSource stream = new StreamSource( inputStream );
			JAXBElement<ValidationConfigType> root = unmarshaller.unmarshal( stream, ValidationConfigType.class );
			validationConfig = root.getValue();
		}
		catch ( JAXBException e ) {
			log.error( "Error parsing validation.xml: {}", e.getMessage() );
		}
		return validationConfig;
	}

	private InputStream getInputStreamForPath(String path) {
		InputStream inputStream = this.getClass().getResourceAsStream( path );
		// try absolute path
		if ( inputStream == null && !path.startsWith( "/" ) ) {
			inputStream = this.getClass().getResourceAsStream( "/" + path );
		}

		if ( inputStream == null && log.isDebugEnabled() ) {
			log.debug( "Unable to open input stream for {}.", path );
		}
		return inputStream;
	}

	private Schema getValidationConfigurationSchema() {
		URL schemaUrl = this.getClass().getClassLoader().getResource( VALIDATION_CONFIGURATION_XSD );
		SchemaFactory sf = SchemaFactory.newInstance( javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI );
		Schema schema = null;
		try {
			schema = sf.newSchema( schemaUrl );
		}
		catch ( SAXException e ) {
			log.warn( "Unable to create schema for {}: {}", VALIDATION_XML_FILE, e.getMessage() );
		}
		return schema;
	}

	private class ParameterHolder {
		ConstraintValidatorFactory constraintValidatorFactory;
		MessageInterpolator messageInterpolator;
		TraversableResolver traversableResolver;
		ValidationProvider provider;
		Class<? extends Configuration<?>> providerClass = null;
		final Map<String, String> configProperties = new HashMap<String, String>();
		final Set<InputStream> mappings = new HashSet<InputStream>();
	}
}
