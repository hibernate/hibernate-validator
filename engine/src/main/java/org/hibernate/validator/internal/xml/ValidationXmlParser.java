/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.xml;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.BootstrapConfiguration;
import javax.validation.executable.ExecutableType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;

import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.util.privilegedactions.GetClassLoader;
import org.hibernate.validator.internal.util.privilegedactions.NewJaxbContext;
import org.hibernate.validator.internal.util.privilegedactions.SetContextClassLoader;
import org.hibernate.validator.internal.util.privilegedactions.Unmarshal;
import org.hibernate.validator.internal.xml.binding.DefaultValidatedExecutableTypesType;
import org.hibernate.validator.internal.xml.binding.ExecutableValidationType;
import org.hibernate.validator.internal.xml.binding.PropertyType;
import org.hibernate.validator.internal.xml.binding.ValidationConfigType;

import org.xml.sax.SAXException;

/**
 * Parser for <i>validation.xml</i> using JAXB.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 */
public class ValidationXmlParser {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private static final String VALIDATION_XML_FILE = "META-INF/validation.xml";
	private static final Map<String, String> SCHEMAS_BY_VERSION = Collections.unmodifiableMap( getSchemasByVersion() );

	private final ClassLoader externalClassLoader;

	private static Map<String, String> getSchemasByVersion() {
		Map<String, String> schemasByVersion = CollectionHelper.newHashMap( 3 );

		schemasByVersion.put( "1.0", "META-INF/validation-configuration-1.0.xsd" );
		schemasByVersion.put( "1.1", "META-INF/validation-configuration-1.1.xsd" );
		schemasByVersion.put( "2.0", "META-INF/validation-configuration-2.0.xsd" );

		return schemasByVersion;
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
		InputStream in = getValidationXmlInputStream();
		if ( in == null ) {
			return BootstrapConfigurationImpl.getDefaultBootstrapConfiguration();
		}

		ClassLoader previousTccl = run( GetClassLoader.fromContext() );

		try {
			run( SetContextClassLoader.action( ValidationXmlParser.class.getClassLoader() ) );

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

			// The validation is done first as we manipulate the XML document before pushing it to the unmarshaller
			// and it might not be valid anymore as we might have switched the namespace to the latest namespace
			// supported.
			Schema schema = getSchema( xmlParserHelper, schemaVersion );
			Validator validator = schema.newValidator();
			validator.validate( new StreamSource( new CloseIgnoringInputStream( in ) ) );

			in.reset();

			xmlEventReader = xmlParserHelper.createXmlEventReader( VALIDATION_XML_FILE, new CloseIgnoringInputStream( in ) );
			ValidationConfigType validationConfig = unmarshal( xmlEventReader );
			xmlEventReader.close();

			return createBootstrapConfiguration( validationConfig );
		}
		catch (XMLStreamException | IOException | SAXException e) {
			throw LOG.getUnableToParseValidationXmlFileException( VALIDATION_XML_FILE, e );
		}
		finally {
			run( SetContextClassLoader.action( previousTccl ) );
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

		return xmlParserHelper.getSchema( schemaResource );
	}

	private ValidationConfigType unmarshal(XMLEventReader xmlEventReader) {
		LOG.parsingXMLFile( VALIDATION_XML_FILE );

		try {
			// JAXBContext#newInstance() requires several permissions internally and doesn't use any privileged blocks
			// itself; Wrapping it here avoids that all calling code bases need to have these permissions as well
			JAXBContext jc = run( NewJaxbContext.action( ValidationConfigType.class ) );
			Unmarshaller unmarshaller = jc.createUnmarshaller();

			// Unmashaller#unmarshal() requires several permissions internally and doesn't use any privileged blocks
			// itself; Wrapping it here avoids that all calling code bases need to have these permissions as well
			JAXBElement<ValidationConfigType> root = run( Unmarshal.action( unmarshaller, xmlEventReader, ValidationConfigType.class ) );
			return root.getValue();
		}
		catch (Exception e) {
			throw LOG.getUnableToParseValidationXmlFileException( VALIDATION_XML_FILE, e );
		}
	}

	private void closeStream(InputStream inputStream) {
		try {
			inputStream.close();
		}
		catch (IOException io) {
			LOG.unableToCloseXMLFileInputStream( VALIDATION_XML_FILE );
		}
	}

	private BootstrapConfiguration createBootstrapConfiguration(ValidationConfigType config) {
		Map<String, String> properties = new HashMap<>();
		for ( PropertyType property : config.getProperty() ) {
			if ( LOG.isDebugEnabled() ) {
				LOG.debugf(
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
				config.getClockProvider(),
				getScriptEvaluatorFactoryClassProperty( config.getProperty() ),
				getValueExtractorClassNames( config ),
				defaultValidatedExecutableTypes,
				executableValidationEnabled,
				new HashSet<>( config.getConstraintMapping() ),
				properties
		);
	}

	private String getScriptEvaluatorFactoryClassProperty(List<PropertyType> properties) {
		return properties.stream()
				.filter( property -> HibernateValidatorConfiguration.SCRIPT_EVALUATOR_FACTORY_CLASSNAME.equals( property.getName() ) )
				.map( PropertyType::getValue )
				.findFirst().orElse( null );
	}

	private Set<String> getValueExtractorClassNames(ValidationConfigType config) {
		Set<String> valueExtractorClassNames = CollectionHelper.newHashSet( config.getValueExtractor().size() );
		for ( String className : config.getValueExtractor() ) {
			if ( !valueExtractorClassNames.add( className ) ) {
				throw LOG.getDuplicateDefinitionsOfValueExtractorException( className );
			}
		}
		return valueExtractorClassNames;
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
	private static <T> T run(PrivilegedAction<T> action) {
		return System.getSecurityManager() != null ? AccessController.doPrivileged( action ) : action.run();
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
