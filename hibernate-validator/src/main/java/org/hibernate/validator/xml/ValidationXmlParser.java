/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *       hibernate-validator/src/main/docbook/en-US/modules/integration.xml~
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
package org.hibernate.validator.xml;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.MessageInterpolator;
import javax.validation.TraversableResolver;
import javax.validation.ValidationException;
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

import org.hibernate.validator.util.LoggerFactory;
import org.hibernate.validator.util.ReflectionHelper;

/**
 * Parser for <i>validation.xml</i> using JAXB.
 *
 * @author Hardy Ferentschik
 */
public class ValidationXmlParser {

	private static final Logger log = LoggerFactory.make();
	private static final String VALIDATION_XML_FILE = "META-INF/validation.xml";
	private static final String VALIDATION_CONFIGURATION_XSD = "META-INF/validation-configuration-1.0.xsd";


	/**
	 * Tries to check whether a validation.xml file exists and parses it using JAXB.
	 *
	 * @return The parameters parsed out of <i>validation.xml</i> wrapped in an instance of <code>ConfigurationImpl.ValidationBootstrapParameters</code>.
	 */
	public final ValidationBootstrapParameters parseValidationXml() {
		ValidationConfigType config = getValidationConfig();
		ValidationBootstrapParameters xmlParameters = new ValidationBootstrapParameters();
		if ( config != null ) {
			// collect the parameters from the xml file
			setProviderClassFromXml( config, xmlParameters );
			setMessageInterpolatorFromXml( config, xmlParameters );
			setTraversableResolverFromXml( config, xmlParameters );
			setConstraintFactoryFromXml( config, xmlParameters );
			setMappingStreamsFromXml( config, xmlParameters );
			setPropertiesFromXml( config, xmlParameters );
		}
		return xmlParameters;
	}

	private void setConstraintFactoryFromXml(ValidationConfigType config, ValidationBootstrapParameters xmlParameters) {
		String constraintFactoryClass = config.getConstraintValidatorFactory();
		if ( constraintFactoryClass != null ) {
			try {
				@SuppressWarnings("unchecked")
				Class<ConstraintValidatorFactory> clazz = (Class<ConstraintValidatorFactory>) ReflectionHelper.loadClass(
						constraintFactoryClass, this.getClass()
				);
				xmlParameters.setConstraintValidatorFactory(
						ReflectionHelper.newInstance(
								clazz, "constraint factory class"
						)
				);
				log.info( "Using {} as constraint factory.", constraintFactoryClass );
			}
			catch ( ValidationException e ) {
				throw new ValidationException(
						"Unable to instantiate constraint factory class " + constraintFactoryClass + ".", e
				);
			}
		}
	}

	private void setPropertiesFromXml(ValidationConfigType config, ValidationBootstrapParameters xmlParameters) {
		for ( PropertyType property : config.getProperty() ) {
			if ( log.isDebugEnabled() ) {
				log.debug(
						"Found property '{}' with value '{}' in validation.xml.",
						property.getName(),
						property.getValue()
				);
			}
			xmlParameters.addConfigProperty( property.getName(), property.getValue() );
		}
	}

	private void setMappingStreamsFromXml(ValidationConfigType config, ValidationBootstrapParameters xmlParameters) {
		for ( String mappingFileName : config.getConstraintMapping() ) {
			if ( log.isDebugEnabled() ) {
				log.debug(
						"Trying to open input stream for {}.", mappingFileName
				);
			}
			InputStream in = getInputStreamForPath( mappingFileName );
			if ( in == null ) {
				throw new ValidationException( "Unable to open input stream for mapping file " + mappingFileName + "." );
			}
			xmlParameters.addMapping( in );
		}
	}

	private void setMessageInterpolatorFromXml(ValidationConfigType config, ValidationBootstrapParameters xmlParameters) {
		String messageInterpolatorClass = config.getMessageInterpolator();
		if ( messageInterpolatorClass != null ) {
			try {
				@SuppressWarnings("unchecked")
				Class<MessageInterpolator> clazz = (Class<MessageInterpolator>) ReflectionHelper.loadClass(
						messageInterpolatorClass, this.getClass()
				);
				xmlParameters.setMessageInterpolator( clazz.newInstance() );
				log.info( "Using {} as message interpolator.", messageInterpolatorClass );
			}
			catch ( ValidationException e ) {
				throw new ValidationException(
						"Unable to instantiate message interpolator class " + messageInterpolatorClass + ".", e
				);
			}
			catch ( InstantiationException e ) {
				throw new ValidationException(
						"Unable to instantiate message interpolator class " + messageInterpolatorClass + ".", e
				);
			}
			catch ( IllegalAccessException e ) {
				throw new ValidationException(
						"Unable to instantiate message interpolator class " + messageInterpolatorClass + ".", e
				);
			}
		}
	}

	private void setTraversableResolverFromXml(ValidationConfigType config, ValidationBootstrapParameters xmlParameters) {
		String traversableResolverClass = config.getTraversableResolver();
		if ( traversableResolverClass != null ) {
			try {
				@SuppressWarnings("unchecked")
				Class<TraversableResolver> clazz = (Class<TraversableResolver>) ReflectionHelper.loadClass(
						traversableResolverClass, this.getClass()
				);
				xmlParameters.setTraversableResolver( clazz.newInstance() );
				log.info( "Using {} as traversable resolver.", traversableResolverClass );
			}
			catch ( ValidationException e ) {
				throw new ValidationException(
						"Unable to instantiate traversable resolver class " + traversableResolverClass + ".", e
				);
			}
			catch ( InstantiationException e ) {
				throw new ValidationException(
						"Unable to instantiate traversable resolver class " + traversableResolverClass + ".", e
				);
			}
			catch ( IllegalAccessException e ) {
				throw new ValidationException(
						"Unable to instantiate traversable resolver class " + traversableResolverClass + ".", e
				);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void setProviderClassFromXml(ValidationConfigType config, ValidationBootstrapParameters xmlParamters) {
		String providerClassName = config.getDefaultProvider();
		if ( providerClassName != null ) {
			try {
				xmlParamters.setProviderClass(
						(Class<? extends ValidationProvider<?>>) ReflectionHelper.loadClass(
								providerClassName, this.getClass()
						)
				);
				log.info( "Using {} as validation provider.", providerClassName );
			}
			catch ( Exception e ) {
				throw new ValidationException(
						"Unable to instantiate validation provider class " + providerClassName + ".", e
				);
			}
		}
	}

	private ValidationConfigType getValidationConfig() {
		log.debug( "Trying to load {} for XML based Validator configuration.", VALIDATION_XML_FILE );
		InputStream inputStream = getInputStreamForPath( VALIDATION_XML_FILE );
		if ( inputStream == null ) {
			log.debug( "No {} found. Using annotation based configuration only", VALIDATION_XML_FILE );
			return null;
		}

		log.info( "{} found. Parsing XML based configuration.", VALIDATION_XML_FILE );

		ValidationConfigType validationConfig;
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
			log.error( "Error parsing {}: {}", VALIDATION_XML_FILE, e.getMessage() );
			throw new ValidationException( "Unable to parse " + VALIDATION_XML_FILE, e );
		}
		finally {
			try {
				inputStream.close();
			}
			catch ( IOException io ) {
				log.warn( "Unable to close input stream for " + VALIDATION_XML_FILE );
			}
		}
		return validationConfig;
	}

	private InputStream getInputStreamForPath(String path) {
		//TODO not sure if it's the right thing to removing '/'
		String inputPath = path;
		if ( inputPath.startsWith( "/" ) ) {
			inputPath = inputPath.substring( 1 );
		}

		boolean isContextCL = true;
		// try the context class loader first
		ClassLoader loader = ReflectionHelper.getClassLoaderFromContext();

		if ( loader == null ) {
			log.debug( "No default context class loader, fall back to Bean Validation's loader" );
			loader = ReflectionHelper.getClassLoaderFromClass( ValidationXmlParser.class );
			isContextCL = false;
		}
		InputStream inputStream = loader.getResourceAsStream( inputPath );

		// try the current class loader
		if ( isContextCL && inputStream == null ) {
			loader = ReflectionHelper.getClassLoaderFromClass( ValidationXmlParser.class );
			inputStream = loader.getResourceAsStream( inputPath );
		}
		return inputStream;
	}

	private Schema getValidationConfigurationSchema() {
		ClassLoader loader = ReflectionHelper.getClassLoaderFromClass( ValidationXmlParser.class );
		URL schemaUrl = loader.getResource( VALIDATION_CONFIGURATION_XSD );
		SchemaFactory sf = SchemaFactory.newInstance( javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI );
		Schema schema = null;
		try {
			schema = sf.newSchema( schemaUrl );
		}
		catch ( SAXException e ) {
			log.warn( "Unable to create schema for {}: {}", VALIDATION_CONFIGURATION_XSD, e.getMessage() );
		}
		return schema;
	}
}
