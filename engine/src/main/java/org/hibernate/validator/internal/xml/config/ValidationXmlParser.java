/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.xml.config;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.util.Map;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;

import jakarta.validation.BootstrapConfiguration;

import org.hibernate.validator.internal.util.actions.GetClassLoader;
import org.hibernate.validator.internal.util.actions.SetContextClassLoader;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.xml.CloseIgnoringInputStream;
import org.hibernate.validator.internal.xml.XmlParserHelper;

import org.xml.sax.SAXException;

/**
 * Parser for <i>validation.xml</i>.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 */
public class ValidationXmlParser {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private static final String VALIDATION_XML_FILE = "META-INF/validation.xml";
	private static final Map<String, String> SCHEMAS_BY_VERSION = Map.of(
			"1.0", "META-INF/validation-configuration-1.0.xsd",
			"1.1", "META-INF/validation-configuration-1.1.xsd",
			"2.0", "META-INF/validation-configuration-2.0.xsd",
			"3.0", "META-INF/validation-configuration-3.0.xsd",
			"3.1", "META-INF/validation-configuration-3.1.xsd",
			"4.0", "META-INF/validation-configuration-4.0.xsd"
	);

	private final ClassLoader externalClassLoader;

	public ValidationXmlParser(ClassLoader externalClassLoader) {
		this.externalClassLoader = externalClassLoader;
	}

	/**
	 * Tries to check whether a <i>validation.xml</i> file exists and parses it.
	 *
	 * @return The parameters parsed out of <i>validation.xml</i> wrapped in an instance of {@code ConfigurationImpl.ValidationBootstrapParameters}.
	 */
	public final BootstrapConfiguration parseValidationXml() {
		InputStream in = getValidationXmlInputStream();
		if ( in == null ) {
			return BootstrapConfigurationImpl.getDefaultBootstrapConfiguration();
		}

		ClassLoader previousTccl = GetClassLoader.fromContext();

		try {
			SetContextClassLoader.action( ValidationXmlParser.class.getClassLoader() );

			// HV-970 The parser helper is only loaded if there actually is a validation.xml file;
			// this avoids accessing javax.xml.stream.* (which does not exist on Android) when not actually
			// working with the XML configuration
			XmlParserHelper xmlParserHelper = new XmlParserHelper();

			// the InputStream supports mark and reset
			in.mark( Integer.MAX_VALUE );

			XMLEventReader xmlEventReader = xmlParserHelper.createXmlEventReader( VALIDATION_XML_FILE, new CloseIgnoringInputStream( in ) );
			String schemaVersion = xmlParserHelper.getSchemaVersion( VALIDATION_XML_FILE, xmlEventReader );
			xmlEventReader.close();

			in.reset();

			// The validation is done first as StAX builders used below are assuming that the XML file is correct and don't
			// do any validation of the input.
			Schema schema = getSchema( xmlParserHelper, schemaVersion );
			Validator validator = schema.newValidator();
			validator.validate( new StreamSource( new CloseIgnoringInputStream( in ) ) );

			in.reset();

			xmlEventReader = xmlParserHelper.createXmlEventReader( VALIDATION_XML_FILE, new CloseIgnoringInputStream( in ) );

			ValidationConfigStaxBuilder validationConfigStaxBuilder = new ValidationConfigStaxBuilder( xmlEventReader );

			xmlEventReader.close();
			in.reset();

			return validationConfigStaxBuilder.build();
		}
		catch (XMLStreamException | IOException | SAXException e) {
			throw LOG.getUnableToParseValidationXmlFileException( VALIDATION_XML_FILE, e );
		}
		finally {
			SetContextClassLoader.action( previousTccl );
			closeStream( in );
		}
	}

	private InputStream getValidationXmlInputStream() {
		LOG.debugf( "Trying to load %s for XML based Validator configuration.", VALIDATION_XML_FILE );
		InputStream inputStream = ResourceLoaderHelper.getResettableInputStreamForPath( VALIDATION_XML_FILE, externalClassLoader );

		if ( inputStream != null ) {
			return inputStream;
		}
		else {
			LOG.debugf( "No %s found. Using annotation based configuration only.", VALIDATION_XML_FILE );
			return null;
		}
	}

	private Schema getSchema(XmlParserHelper xmlParserHelper, String schemaVersion) {
		String schemaResource = SCHEMAS_BY_VERSION.get( schemaVersion );

		if ( schemaResource == null ) {
			throw LOG.getUnsupportedSchemaVersionException( VALIDATION_XML_FILE, schemaVersion );
		}

		Schema schema = xmlParserHelper.getSchema( schemaResource );

		if ( schema == null ) {
			throw LOG.unableToGetXmlSchema( schemaResource );
		}

		return schema;
	}

	private void closeStream(InputStream inputStream) {
		try {
			inputStream.close();
		}
		catch (IOException io) {
			LOG.unableToCloseXMLFileInputStream( VALIDATION_XML_FILE );
		}
	}

}
