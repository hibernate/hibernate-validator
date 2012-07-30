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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.validation.ConfigurationSource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;

import org.hibernate.validator.internal.util.ResourceLoaderHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * Parser for <i>validation.xml</i> using JAXB.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 */
public class ValidationXmlParser {

	private static final Log log = LoggerFactory.make();

	private static final String VALIDATION_XML_FILE = "META-INF/validation.xml";
	private static final ConcurrentMap<String, String> SCHEMAS_BY_VERSION = new ConcurrentHashMap<String, String>(
			2,
			0.75f,
			1
	);

	private XmlParserHelper xmlParserHelper = new XmlParserHelper();

	static {
		SCHEMAS_BY_VERSION.put( "1.0", "META-INF/validation-configuration-1.0.xsd" );
		SCHEMAS_BY_VERSION.put( "1.1", "META-INF/validation-configuration-1.1.xsd" );
	}

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
		BufferedInputStream inputStream = getInputStream();

		if ( inputStream == null ) {
			log.debugf( "No %s found. Using annotation based configuration only.", VALIDATION_XML_FILE );
			return null;
		}

		String schemaVersion = xmlParserHelper.getSchemaVersion( VALIDATION_XML_FILE, inputStream );
		String schemaResourceName = getSchemaResourceName( schemaVersion );
		Schema schema = xmlParserHelper.getSchema( schemaResourceName );

		ValidationConfigType validationConfig = unmarshal( inputStream, schema );

		closeStream( inputStream );

		return validationConfig;
	}

	private String getSchemaResourceName(String schemaVersion) {
		String schemaResource = SCHEMAS_BY_VERSION.get( schemaVersion );

		if ( schemaResource == null ) {
			throw log.getUnsupportedSchemaVersionException( VALIDATION_XML_FILE, schemaVersion );
		}

		return schemaResource;
	}

	private BufferedInputStream getInputStream() {
		log.debugf( "Trying to load %s for XML based Validator configuration.", VALIDATION_XML_FILE );
		InputStream inputStream = ResourceLoaderHelper.getInputStreamForPath( VALIDATION_XML_FILE );
		return inputStream != null ? new BufferedInputStream( inputStream ) : null;
	}

	private ValidationConfigType unmarshal(InputStream inputStream, Schema schema) {
		log.parsingXMLFile( VALIDATION_XML_FILE );

		try {
			JAXBContext jc = JAXBContext.newInstance( ValidationConfigType.class );
			Unmarshaller unmarshaller = jc.createUnmarshaller();
			unmarshaller.setSchema( schema );
			StreamSource stream = new StreamSource( inputStream );
			JAXBElement<ValidationConfigType> root = unmarshaller.unmarshal( stream, ValidationConfigType.class );
			return root.getValue();
		}
		catch ( JAXBException e ) {
			throw log.getUnableToParseValidationXmlFileException( VALIDATION_XML_FILE, e );
		}
	}

	private void closeStream(BufferedInputStream inputStream) {
		try {
			inputStream.close();
		}
		catch ( IOException io ) {
			log.unableToCloseXMLFileInputStream( VALIDATION_XML_FILE );
		}
	}
}
