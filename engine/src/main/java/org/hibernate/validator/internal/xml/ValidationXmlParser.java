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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import javax.validation.ConfigurationSource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

import org.hibernate.validator.internal.util.ReflectionHelper;
import org.hibernate.validator.internal.util.ResourceLoaderHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * Parser for <i>validation.xml</i> using JAXB.
 *
 * @author Hardy Ferentschik
 */
public class ValidationXmlParser {

	private static final Log log = LoggerFactory.make();
	private static final String VALIDATION_XML_FILE = "META-INF/validation.xml";
	private static final String VALIDATION_CONFIGURATION_XSD = "META-INF/validation-configuration-1.1.xsd";

	/**
	 * Tries to check whether a <i>validation.xml</i> file exists and parses it.
	 *
	 * @return The parameters parsed out of <i>validation.xml</i> wrapped in an instance of {@code ConfigurationImpl.ValidationBootstrapParameters}.
	 */
	public final ConfigurationSource parseValidationXml() {
		ValidationConfigType config = getValidationConfig();
		if ( config != null ) {
			Map<String, String> properties = new HashMap<String, String>();
			for ( PropertyType property : config.getProperty() ) {
				if ( log.isDebugEnabled() ) {
					log.debugf(
							"Found property '%s' with value '%s' in validation.xml.",
							property.getName(),
							property.getValue()
					);
				}
				properties.put( property.getName(), property.getValue() );
			}

			return new ValidationXmlConfigurationSource(
					config.getDefaultProvider(),
					config.getConstraintValidatorFactory(),
					config.getMessageInterpolator(),
					config.getTraversableResolver(),
					config.getParameterNameProvider(),
					new HashSet<String>( config.getConstraintMapping() ),
					properties
			);
		}
		else {
			return new ValidationXmlConfigurationSource();
		}
	}

	private ValidationConfigType getValidationConfig() {
		log.debugf( "Trying to load %s for XML based Validator configuration.", VALIDATION_XML_FILE );
		InputStream inputStream = ResourceLoaderHelper.getInputStreamForPath( VALIDATION_XML_FILE );
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

	private Schema getValidationConfigurationSchema() {
		ClassLoader loader = ReflectionHelper.getClassLoaderFromClass( ValidationXmlParser.class );
		URL schemaUrl = loader.getResource( VALIDATION_CONFIGURATION_XSD );
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
}
