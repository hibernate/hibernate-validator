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
package org.hibernate.validation.xml;

import java.io.InputStream;
import java.net.URL;
import java.security.AccessController;
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

import org.hibernate.validation.util.LoggerFactory;
import org.hibernate.validation.util.ReflectionHelper;
import org.hibernate.validation.util.NewInstance;
import org.hibernate.validation.xml.PropertyType;
import org.hibernate.validation.xml.ValidationConfigType;

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
	public ValidationBootstrapParameters parseValidationXml() {
		ValidationConfigType config = getValidationConfig();
		ValidationBootstrapParameters xmlParameters = new ValidationBootstrapParameters();
		if ( config != null ) {
			// collect the paramters from the xml file
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
				Class<ConstraintValidatorFactory> clazz = ( Class<ConstraintValidatorFactory> ) ReflectionHelper.classForName(
						constraintFactoryClass, this.getClass()
				);
				NewInstance<ConstraintValidatorFactory> newInstance = NewInstance.action( clazz, "constraint factory class" );
				if ( System.getSecurityManager() != null ) {
					xmlParameters.constraintValidatorFactory = AccessController.doPrivileged( newInstance );
				}
				else {
					xmlParameters.constraintValidatorFactory = newInstance.run();
				}
				log.info( "Using {} as constraint factory.", constraintFactoryClass );
			}
			catch ( ClassNotFoundException e ) {
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
			xmlParameters.configProperties.put( property.getName(), property.getValue() );
		}
	}

	private void setMappingStreamsFromXml(ValidationConfigType config, ValidationBootstrapParameters xmlParameters) {
		for ( JAXBElement<String> mappingFileName : config.getConstraintMapping() ) {
			if ( log.isDebugEnabled() ) {
				log.debug(
						"Trying to open input stream for {}.", mappingFileName.getValue()
				);
			}
			InputStream in = getInputStreamForPath( mappingFileName.getValue() );
			if ( in == null ) {
				throw new ValidationException( "Unable to open input stream for mapping file " + mappingFileName.getValue() + "." );
			}
			xmlParameters.mappings.add( in );
		}
	}

	private void setMessageInterpolatorFromXml(ValidationConfigType config, ValidationBootstrapParameters xmlParameters) {
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
			catch ( ClassNotFoundException e ) {
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
				Class<TraversableResolver> clazz = ( Class<TraversableResolver> ) ReflectionHelper.classForName(
						traversableResolverClass, this.getClass()
				);
				xmlParameters.traversableResolver = clazz.newInstance();
				log.info( "Using {} as traversable resolver.", traversableResolverClass );
			}
			catch ( ClassNotFoundException e ) {
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
				xmlParamters.providerClass = ( Class<? extends ValidationProvider<?>> ) ReflectionHelper.classForName(
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
			log.debug( "No {} found. Using annotation based configuration only", VALIDATION_XML_FILE );
			return null;
		}

		log.info( "{} found.", VALIDATION_XML_FILE );

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
			log.error( "Error parsing validation.xml: {}", e.getMessage() );
			throw new ValidationException( "Unable to parse " + VALIDATION_XML_FILE );
		}
		return validationConfig;
	}

	private InputStream getInputStreamForPath(String path) {
		//TODO not sure it's the right thing to do (ie removing '/'
		//remove heading '/'
		if ( path.startsWith( "/" ) ) {
			path = path.substring( 1 );
		}
		// try the context class loader first
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		boolean isContextCL = true;
		if (loader == null) {
			log.debug( "No default context class loader, fallbacking to Bean Validation's loader" );
			loader = ValidationXmlParser.class.getClassLoader();
			isContextCL = false;
		}
		InputStream inputStream = loader.getResourceAsStream( path );

		// try the current class loader
		if ( isContextCL && inputStream == null ) {
			inputStream = ValidationXmlParser.class.getClassLoader().getResourceAsStream( path );
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
			log.warn( "Unable to create schema for {}: {}", VALIDATION_CONFIGURATION_XSD, e.getMessage() );
		}
		return schema;
	}
}
