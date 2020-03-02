/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.xml.mapping;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashMap;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;

import org.hibernate.validator.internal.engine.ConstraintCreationContext;
import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptions;
import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptionsImpl;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement;
import org.hibernate.validator.internal.properties.javabean.JavaBeanHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.util.privilegedactions.GetClassLoader;
import org.hibernate.validator.internal.util.privilegedactions.SetContextClassLoader;
import org.hibernate.validator.internal.xml.CloseIgnoringInputStream;
import org.hibernate.validator.internal.xml.XmlParserHelper;
import org.xml.sax.SAXException;

/**
 * XML parser for validation-mapping files.
 *
 * @author Hardy Ferentschik
 * @author Marko Bekhta
 */
public class MappingXmlParser {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private final Set<Class<?>> processedClasses = newHashSet();
	private final ConstraintCreationContext constraintCreationContext;
	private final AnnotationProcessingOptionsImpl annotationProcessingOptions;
	private final JavaBeanHelper javaBeanHelper;
	private final Map<Class<?>, List<Class<?>>> defaultSequences;
	private final Map<Class<?>, Set<ConstrainedElement>> constrainedElements;

	private final XmlParserHelper xmlParserHelper;

	private final ClassLoadingHelper classLoadingHelper;

	private static final Map<String, String> SCHEMAS_BY_VERSION = Collections.unmodifiableMap( getSchemasByVersion() );

	private static Map<String, String> getSchemasByVersion() {
		Map<String, String> schemasByVersion = new HashMap<>();

		schemasByVersion.put( "1.0", "META-INF/validation-mapping-1.0.xsd" );
		schemasByVersion.put( "1.1", "META-INF/validation-mapping-1.1.xsd" );
		schemasByVersion.put( "2.0", "META-INF/validation-mapping-2.0.xsd" );
		schemasByVersion.put( "3.0", "META-INF/validation-mapping-3.0.xsd" );

		return schemasByVersion;
	}

	public MappingXmlParser(ConstraintCreationContext constraintCreationContext, JavaBeanHelper javaBeanHelper, ClassLoader externalClassLoader) {
		this.constraintCreationContext = constraintCreationContext;
		this.annotationProcessingOptions = new AnnotationProcessingOptionsImpl();
		this.javaBeanHelper = javaBeanHelper;
		this.defaultSequences = newHashMap();
		this.constrainedElements = newHashMap();
		this.xmlParserHelper = new XmlParserHelper();
		this.classLoadingHelper = new ClassLoadingHelper( externalClassLoader, run( GetClassLoader.fromContext() ) );
	}

	/**
	 * Parses the given set of input stream representing XML constraint
	 * mappings.
	 *
	 * @param mappingStreams The streams to parse. Must support the mark/reset contract.
	 */
	public final void parse(Set<InputStream> mappingStreams) {
		ClassLoader previousTccl = run( GetClassLoader.fromContext() );

		try {
			run( SetContextClassLoader.action( MappingXmlParser.class.getClassLoader() ) );

			Set<String> alreadyProcessedConstraintDefinitions = newHashSet();
			for ( InputStream in : mappingStreams ) {
				// the InputStreams passed in parameters support mark and reset
				in.mark( Integer.MAX_VALUE );

				XMLEventReader xmlEventReader = xmlParserHelper.createXmlEventReader( "constraint mapping file", new CloseIgnoringInputStream( in ) );
				String schemaVersion = xmlParserHelper.getSchemaVersion( "constraint mapping file", xmlEventReader );
				xmlEventReader.close();

				in.reset();

				// The validation is done first as StAX builders used below are assuming that the XML file is correct and don't
				// do any validation of the input.
				String schemaResourceName = getSchemaResourceName( schemaVersion );
				Schema schema = xmlParserHelper.getSchema( schemaResourceName );
				if ( schema == null ) {
					throw LOG.unableToGetXmlSchema( schemaResourceName );
				}

				Validator validator = schema.newValidator();
				validator.validate( new StreamSource( new CloseIgnoringInputStream( in ) ) );

				in.reset();

				ConstraintMappingsStaxBuilder constraintMappingsStaxBuilder = new ConstraintMappingsStaxBuilder(
						classLoadingHelper, constraintCreationContext,
						annotationProcessingOptions, javaBeanHelper, defaultSequences
				);

				xmlEventReader = xmlParserHelper.createXmlEventReader( "constraint mapping file", new CloseIgnoringInputStream( in ) );

				while ( xmlEventReader.hasNext() ) {
					constraintMappingsStaxBuilder.process( xmlEventReader, xmlEventReader.nextEvent() );
				}
				constraintMappingsStaxBuilder.build( processedClasses, constrainedElements, alreadyProcessedConstraintDefinitions );
				xmlEventReader.close();
				in.reset();
			}
		}
		catch (IOException | XMLStreamException | SAXException e) {
			throw LOG.getErrorParsingMappingFileException( e );
		}
		finally {
			run( SetContextClassLoader.action( previousTccl ) );
		}
	}

	public final Set<Class<?>> getXmlConfiguredClasses() {
		return processedClasses;
	}

	public final AnnotationProcessingOptions getAnnotationProcessingOptions() {
		return annotationProcessingOptions;
	}

	public final Set<ConstrainedElement> getConstrainedElementsForClass(Class<?> beanClass) {
		if ( constrainedElements.containsKey( beanClass ) ) {
			return constrainedElements.get( beanClass );
		}
		else {
			return Collections.emptySet();
		}
	}

	public final List<Class<?>> getDefaultSequenceForClass(Class<?> beanClass) {
		return defaultSequences.get( beanClass );
	}

	private String getSchemaResourceName(String schemaVersion) {
		String schemaResource = SCHEMAS_BY_VERSION.get( schemaVersion );

		if ( schemaResource == null ) {
			throw LOG.getUnsupportedSchemaVersionException( "constraint mapping file", schemaVersion );
		}

		return schemaResource;
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
