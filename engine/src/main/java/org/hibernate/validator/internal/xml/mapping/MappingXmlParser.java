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
import org.hibernate.validator.internal.util.actions.GetClassLoader;
import org.hibernate.validator.internal.util.actions.SetContextClassLoader;
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
	private final Set<ConstraintMappingsStaxBuilder> mappingBuilders;

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
		this.mappingBuilders = newHashSet();
		this.xmlParserHelper = new XmlParserHelper();
		this.classLoadingHelper = new ClassLoadingHelper( externalClassLoader, GetClassLoader.fromContext() );
	}

	/**
	 * Parses the given set of input stream representing XML constraint
	 * mappings.
	 *
	 * @param mappingStreams The streams to parse. Must support the mark/reset contract.
	 */
	public final void parse(Set<InputStream> mappingStreams) {
		ClassLoader previousTccl = GetClassLoader.fromContext();

		try {
			SetContextClassLoader.action( MappingXmlParser.class.getClassLoader() );

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
				// at this point we only build the constraint definitions.
				// we want to fully populate the constraint helper and get the final rules for which
				// validators will be applied before we build any constrained elements that contribute to
				// final bean metadata.
				constraintMappingsStaxBuilder.buildConstraintDefinitions( alreadyProcessedConstraintDefinitions );
				// we only add the builder to process it later if it has anything related to bean's constraints,
				// otherwise it was only about constraint definition, and we've processed it already.
				if ( constraintMappingsStaxBuilder.hasBeanBuilders() ) {
					mappingBuilders.add( constraintMappingsStaxBuilder );
				}
				xmlEventReader.close();
				in.reset();
			}
		}
		catch (IOException | XMLStreamException | SAXException e) {
			throw LOG.getErrorParsingMappingFileException( e );
		}
		finally {
			SetContextClassLoader.action( previousTccl );
		}
	}

	public final boolean createConstrainedElements() {
		for ( ConstraintMappingsStaxBuilder builder : mappingBuilders ) {
			builder.buildConstrainedElements( processedClasses, constrainedElements );
		}

		// If there are no mappings means that we've only got some constraint definitions passed to us through XML.
		// so we don't need to create an XML metadata provider since it won't contribute anything anyway.
		return !mappingBuilders.isEmpty();
	}

	public final Set<Class<?>> getXmlConfiguredClasses() {
		return processedClasses;
	}

	public final AnnotationProcessingOptions getAnnotationProcessingOptions() {
		return annotationProcessingOptions;
	}

	public final Set<ConstrainedElement> getConstrainedElementsForClass(Class<?> beanClass) {
		return constrainedElements.getOrDefault( beanClass, Collections.emptySet() );
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

}
