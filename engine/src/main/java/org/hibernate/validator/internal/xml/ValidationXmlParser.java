/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.xml;

import java.io.IOException;
import java.io.InputStream;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.validation.BootstrapConfiguration;
import javax.validation.executable.ExecutableType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLEventReader;
import javax.xml.validation.Schema;

import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.util.privilegedactions.NewJaxbContext;
import org.hibernate.validator.internal.util.privilegedactions.Unmarshal;

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

	private final ClassLoader externalClassLoader;

	static {
		SCHEMAS_BY_VERSION.put( "1.0", "META-INF/validation-configuration-1.0.xsd" );
		SCHEMAS_BY_VERSION.put( "1.1", "META-INF/validation-configuration-1.1.xsd" );
	}

	public ValidationXmlParser(ClassLoader externalClassLoader) {
		this.externalClassLoader = externalClassLoader;
	}

	/**
	 * Tries to check whether a <i>validation.xml</i> file exists and parses it.
	 *
	 * @return The parameters parsed out of <i>validation.xml</i> wrapped in an instance of {@code ConfigurationImpl.ValidationBootstrapParameters}.
	 */
	public final BootstrapConfiguration parseValidationXml() {
		InputStream inputStream = getValidationXmlInputStream();
		if ( inputStream == null ) {
			return BootstrapConfigurationImpl.getDefaultBootstrapConfiguration();
		}

		try {
			// HV-970 The parser helper is only loaded if there actually is a validation.xml file;
			// this avoids accessing javax.xml.stream.* (which does not exist on Android) when not actually
			// working with the XML configuration
			XmlParserHelper xmlParserHelper = new XmlParserHelper();
			XMLEventReader xmlEventReader = xmlParserHelper.createXmlEventReader( VALIDATION_XML_FILE, inputStream );

			String schemaVersion = xmlParserHelper.getSchemaVersion( VALIDATION_XML_FILE, xmlEventReader );
			Schema schema = getSchema( xmlParserHelper, schemaVersion );
			ValidationConfigType validationConfig = unmarshal( xmlEventReader, schema );

			return createBootstrapConfiguration( validationConfig );
		}
		finally {
			closeStream( inputStream );
		}
	}

	private InputStream getValidationXmlInputStream() {
		log.debugf( "Trying to load %s for XML based Validator configuration.", VALIDATION_XML_FILE );
		InputStream inputStream = ResourceLoaderHelper.getResettableInputStreamForPath( VALIDATION_XML_FILE, externalClassLoader );

		if ( inputStream != null ) {
			return inputStream;
		}
		else {
			log.debugf( "No %s found. Using annotation based configuration only.", VALIDATION_XML_FILE );
			return null;
		}
	}

	private Schema getSchema(XmlParserHelper xmlParserHelper, String schemaVersion) {
		String schemaResource = SCHEMAS_BY_VERSION.get( schemaVersion );

		if ( schemaResource == null ) {
			throw log.getUnsupportedSchemaVersionException( VALIDATION_XML_FILE, schemaVersion );
		}

		return xmlParserHelper.getSchema( schemaResource );
	}

	private ValidationConfigType unmarshal(XMLEventReader xmlEventReader, Schema schema) {
		log.parsingXMLFile( VALIDATION_XML_FILE );

		try {
			// JAXBContext#newInstance() requires several permissions internally and doesn't use any privileged blocks
			// itself; Wrapping it here avoids that all calling code bases need to have these permissions as well
			JAXBContext jc = run( NewJaxbContext.action( ValidationConfigType.class ) );
			Unmarshaller unmarshaller = jc.createUnmarshaller();
			unmarshaller.setSchema( schema );

			// Unmashaller#unmarshal() requires several permissions internally and doesn't use any privileged blocks
			// itself; Wrapping it here avoids that all calling code bases need to have these permissions as well
			JAXBElement<ValidationConfigType> root = run( Unmarshal.action( unmarshaller, xmlEventReader, ValidationConfigType.class ) );
			return root.getValue();
		}
		catch ( Exception e ) {
			throw log.getUnableToParseValidationXmlFileException( VALIDATION_XML_FILE, e );
		}
	}

	private void closeStream(InputStream inputStream) {
		try {
			inputStream.close();
		}
		catch ( IOException io ) {
			log.unableToCloseXMLFileInputStream( VALIDATION_XML_FILE );
		}
	}

	private BootstrapConfiguration createBootstrapConfiguration(ValidationConfigType config) {
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

		ExecutableValidationType executableValidationType = config.getExecutableValidation();
		EnumSet<ExecutableType> defaultValidatedExecutableTypes = executableValidationType == null
				? getValidatedExecutableTypes( null )
				: getValidatedExecutableTypes( executableValidationType.getDefaultValidatedExecutableTypes() );
		boolean executableValidationEnabled = executableValidationType == null || executableValidationType.getEnabled();

		return new BootstrapConfigurationImpl(
				config.getDefaultProvider(),
				config.getConstraintValidatorFactory(),
				config.getMessageInterpolator(),
				config.getTraversableResolver(),
				config.getParameterNameProvider(),
				defaultValidatedExecutableTypes,
				executableValidationEnabled,
				new HashSet<String>( config.getConstraintMapping() ),
				properties
		);
	}

	/**
	 * Returns an enum set with the executable types corresponding to the given
	 * XML configuration, considering the special elements
	 * {@link ExecutableType#ALL} and {@link ExecutableType#NONE}.
	 *
	 * @param validatedExecutables Schema type with executable types.
	 *
	 * @return An enum set representing the given executable types.
	 */
	private EnumSet<ExecutableType> getValidatedExecutableTypes(DefaultValidatedExecutableTypesType validatedExecutables) {
		if ( validatedExecutables == null ) {
			return null;
		}

		EnumSet<ExecutableType> executableTypes = EnumSet.noneOf( ExecutableType.class );
		executableTypes.addAll( validatedExecutables.getExecutableType() );

		return executableTypes;
	}

	/**
	 * Runs the given privileged action, using a privileged block if required.
	 * <p>
	 * <b>NOTE:</b> This must never be changed into a publicly available method to avoid execution of arbitrary
	 * privileged actions within HV's protection domain.
	 */
	private <T> T run(PrivilegedExceptionAction<T> action) throws Exception {
		return System.getSecurityManager() != null ? AccessController.doPrivileged( action ) : action.run();
	}
}
