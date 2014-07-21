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
package org.hibernate.validator.internal.xml;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
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

import org.xml.sax.SAXException;

import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.util.privilegedactions.GetClassLoader;
import org.hibernate.validator.internal.util.privilegedactions.GetResource;
import org.hibernate.validator.internal.util.privilegedactions.LoadClass;
import org.hibernate.validator.internal.util.privilegedactions.NewInstance;

/**
 * Parser for <i>validation.xml</i> using JAXB.
 *
 * @author Hardy Ferentschik
 */
public class ValidationXmlParser {

	private static final Log log = LoggerFactory.make();
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
				Class<ConstraintValidatorFactory> clazz = (Class<ConstraintValidatorFactory>) run(
						LoadClass.action(
								constraintFactoryClass, this.getClass()
						)
				);
				xmlParameters.setConstraintValidatorFactory(
						run( NewInstance.action( clazz, "constraint factory class" ) )
				);
				log.usingConstraintFactory( constraintFactoryClass );
			}
			catch ( ValidationException e ) {
				throw log.getUnableToInstantiateConstraintFactoryClassException( constraintFactoryClass, e );
			}
		}
	}

	private void setPropertiesFromXml(ValidationConfigType config, ValidationBootstrapParameters xmlParameters) {
		for ( PropertyType property : config.getProperty() ) {
			if ( log.isDebugEnabled() ) {
				log.debugf(
						"Found property '%s' with value '%s' in validation.xml.",
						property.getName(),
						property.getValue()
				);
			}
			xmlParameters.addConfigProperty( property.getName(), property.getValue() );
		}
	}

	private void setMappingStreamsFromXml(ValidationConfigType config, ValidationBootstrapParameters xmlParameters) {
		for ( String mappingFileName : config.getConstraintMapping() ) {
			log.debugf( "Trying to open input stream for %s.", mappingFileName);

			InputStream in = getInputStreamForPath( mappingFileName );
			if ( in == null ) {
				throw log.getUnableToOpenInputStreamForMappingFileException( mappingFileName );
			}
			xmlParameters.addMapping( in );
		}
	}

	private void setMessageInterpolatorFromXml(ValidationConfigType config, ValidationBootstrapParameters xmlParameters) {
		String messageInterpolatorClass = config.getMessageInterpolator();
		if ( messageInterpolatorClass != null ) {
			try {
				@SuppressWarnings("unchecked")
				Class<MessageInterpolator> clazz = (Class<MessageInterpolator>) run(
						LoadClass.action( messageInterpolatorClass, this.getClass() )
				);
				xmlParameters.setMessageInterpolator( clazz.newInstance() );
				log.usingMessageInterpolator( messageInterpolatorClass );
			}
			catch ( ValidationException e ) {
				throw log.getUnableToInstantiateMessageInterpolatorClassException( messageInterpolatorClass, e );
			}
			catch ( InstantiationException e ) {
				throw log.getUnableToInstantiateMessageInterpolatorClassException( messageInterpolatorClass, e );
			}
			catch ( IllegalAccessException e ) {
				throw log.getUnableToInstantiateMessageInterpolatorClassException( messageInterpolatorClass, e );
			}
		}
	}

	private void setTraversableResolverFromXml(ValidationConfigType config, ValidationBootstrapParameters xmlParameters) {
		String traversableResolverClass = config.getTraversableResolver();
		if ( traversableResolverClass != null ) {
			try {
				@SuppressWarnings("unchecked")
				Class<TraversableResolver> clazz = (Class<TraversableResolver>) run(
						LoadClass.action( traversableResolverClass, this.getClass() )
				);
				xmlParameters.setTraversableResolver( clazz.newInstance() );
				log.usingTraversableResolver( traversableResolverClass );
			}
			catch ( ValidationException e ) {
				throw log.getUnableToInstantiateTraversableResolverClassException( traversableResolverClass, e );
			}
			catch ( InstantiationException e ) {
				throw log.getUnableToInstantiateTraversableResolverClassException( traversableResolverClass, e );
			}
			catch ( IllegalAccessException e ) {
				throw log.getUnableToInstantiateTraversableResolverClassException( traversableResolverClass, e );
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void setProviderClassFromXml(ValidationConfigType config, ValidationBootstrapParameters xmlParamters) {
		String providerClassName = config.getDefaultProvider();
		if ( providerClassName != null ) {
			try {
				xmlParamters.setProviderClass(
						(Class<? extends ValidationProvider<?>>) run(
								LoadClass.action( providerClassName, this.getClass() )
						)
				);
				log.usingValidationProvider( providerClassName );
			}
			catch ( Exception e ) {
				throw log.getUnableToInstantiateValidationProviderClassException( providerClassName, e );
			}
		}
	}

	private ValidationConfigType getValidationConfig() {
		log.debugf( "Trying to load %s for XML based Validator configuration.", VALIDATION_XML_FILE );
		InputStream inputStream = getInputStreamForPath( VALIDATION_XML_FILE );
		if ( inputStream == null ) {
			log.debugf( "No %s found. Using annotation based configuration only.", VALIDATION_XML_FILE );
			return null;
		}

		log.parsingXMLFile( VALIDATION_XML_FILE );

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
			throw log.getUnableToParseValidationXmlFileException( VALIDATION_XML_FILE, e );
		}
		finally {
			try {
				inputStream.close();
			}
			catch ( IOException io ) {
				log.unableToCloseXMLFileInputStream( VALIDATION_XML_FILE );
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
		ClassLoader loader = run( GetClassLoader.fromContext() );

		if ( loader == null ) {
			log.debug( "No default context class loader, fall back to Bean Validation's loader" );
			loader = run( GetClassLoader.fromClass( ValidationXmlParser.class ) );
			isContextCL = false;
		}
		InputStream inputStream = loader.getResourceAsStream( inputPath );

		// try the current class loader
		if ( isContextCL && inputStream == null ) {
			loader = run( GetClassLoader.fromClass( ValidationXmlParser.class ) );
			inputStream = loader.getResourceAsStream( inputPath );
		}
		return inputStream;
	}

	private Schema getValidationConfigurationSchema() {
		ClassLoader loader = run( GetClassLoader.fromClass( ValidationXmlParser.class ) );
		URL schemaUrl = run( GetResource.action( loader, VALIDATION_CONFIGURATION_XSD ) );
		SchemaFactory sf = SchemaFactory.newInstance( javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI );
		Schema schema = null;
		try {
			schema = sf.newSchema( schemaUrl );
		}
		catch ( SAXException e ) {
			log.unableToCreateSchema( VALIDATION_CONFIGURATION_XSD, e.getMessage() );
		}
		return schema;
	}

	/**
	 * Runs the given privileged action, using a privileged block if required.
	 * <p>
	 * <b>NOTE:</b> This must never be changed into a publicly available method to avoid execution of arbitrary
	 * privileged actions within HV's protection domain.
	 */
	private static <T> T run(PrivilegedAction<T> action) {
		return System.getSecurityManager() != null ? AccessController.doPrivileged( action ) : action.run();
	}
}
