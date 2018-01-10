/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.xml;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashMap;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintValidator;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;

import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorDescriptor;
import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorManager;
import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptions;
import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptionsImpl;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement;
import org.hibernate.validator.internal.metadata.raw.ConstrainedExecutable;
import org.hibernate.validator.internal.metadata.raw.ConstrainedField;
import org.hibernate.validator.internal.metadata.raw.ConstrainedType;
import org.hibernate.validator.internal.util.TypeResolutionHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.util.privilegedactions.GetClassLoader;
import org.hibernate.validator.internal.util.privilegedactions.NewJaxbContext;
import org.hibernate.validator.internal.util.privilegedactions.SetContextClassLoader;
import org.hibernate.validator.internal.util.privilegedactions.Unmarshal;
import org.hibernate.validator.internal.xml.binding.BeanType;
import org.hibernate.validator.internal.xml.binding.ConstraintDefinitionType;
import org.hibernate.validator.internal.xml.binding.ConstraintMappingsType;
import org.hibernate.validator.internal.xml.binding.ValidatedByType;
import org.xml.sax.SAXException;

/**
 * XML parser for validation-mapping files.
 *
 * @author Hardy Ferentschik
 */
public class MappingXmlParser {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private final Set<Class<?>> processedClasses = newHashSet();
	private final ConstraintHelper constraintHelper;
	private final TypeResolutionHelper typeResolutionHelper;
	private final ValueExtractorManager valueExtractorManager;
	private final AnnotationProcessingOptionsImpl annotationProcessingOptions;
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

		return schemasByVersion;
	}

	public MappingXmlParser(ConstraintHelper constraintHelper, TypeResolutionHelper typeResolutionHelper, ValueExtractorManager valueExtractorManager,
			ClassLoader externalClassLoader) {
		this.constraintHelper = constraintHelper;
		this.typeResolutionHelper = typeResolutionHelper;
		this.valueExtractorManager = valueExtractorManager;
		this.annotationProcessingOptions = new AnnotationProcessingOptionsImpl();
		this.defaultSequences = newHashMap();
		this.constrainedElements = newHashMap();
		this.xmlParserHelper = new XmlParserHelper();
		this.classLoadingHelper = new ClassLoadingHelper( externalClassLoader );
	}

	/**
	 * Parses the given set of input stream representing XML constraint
	 * mappings.
	 *
	 * @param mappingStreams The streams to parse. Must support the mark/reset contract.
	 */
	public final void parse(Set<InputStream> mappingStreams) {
		try {
			// JAXBContext#newInstance() requires several permissions internally and doesn't use any privileged blocks
			// itself; Wrapping it here avoids that all calling code bases need to have these permissions as well
			JAXBContext jc = run( NewJaxbContext.action( ConstraintMappingsType.class ) );

			MetaConstraintBuilder metaConstraintBuilder = new MetaConstraintBuilder(
					classLoadingHelper,
					constraintHelper,
					typeResolutionHelper,
					valueExtractorManager
			);
			GroupConversionBuilder groupConversionBuilder = new GroupConversionBuilder( classLoadingHelper );

			ConstrainedTypeBuilder constrainedTypeBuilder = new ConstrainedTypeBuilder(
					classLoadingHelper,
					metaConstraintBuilder,
					annotationProcessingOptions,
					defaultSequences
			);
			ConstrainedFieldBuilder constrainedFieldBuilder = new ConstrainedFieldBuilder(
					metaConstraintBuilder,
					groupConversionBuilder,
					annotationProcessingOptions
			);
			ConstrainedExecutableBuilder constrainedExecutableBuilder = new ConstrainedExecutableBuilder(
					classLoadingHelper,
					metaConstraintBuilder,
					groupConversionBuilder,
					annotationProcessingOptions
			);
			ConstrainedGetterBuilder constrainedGetterBuilder = new ConstrainedGetterBuilder(
					metaConstraintBuilder,
					groupConversionBuilder,
					annotationProcessingOptions
			);

			Set<String> alreadyProcessedConstraintDefinitions = newHashSet();
			for ( InputStream in : mappingStreams ) {
				ConstraintMappingsType mapping = unmarshal( jc, in );
				String defaultPackage = mapping.getDefaultPackage();

				parseConstraintDefinitions(
						mapping.getConstraintDefinition(),
						defaultPackage,
						alreadyProcessedConstraintDefinitions
				);

				for ( BeanType bean : mapping.getBean() ) {
					processBeanType(
							constrainedTypeBuilder,
							constrainedFieldBuilder,
							constrainedExecutableBuilder,
							constrainedGetterBuilder,
							defaultPackage,
							bean
					);
				}

				in.reset();
			}
		}
		catch (JAXBException | SAXException | IOException | XMLStreamException e) {
			throw LOG.getErrorParsingMappingFileException( e );
		}
	}

	private ConstraintMappingsType unmarshal(JAXBContext jc, InputStream in) throws JAXBException, XMLStreamException, IOException, SAXException {
		ClassLoader previousTccl = run( GetClassLoader.fromContext() );

		try {
			run( SetContextClassLoader.action( MappingXmlParser.class.getClassLoader() ) );

			// the InputStreams passed in parameters support mark and reset
			in.mark( Integer.MAX_VALUE );

			XMLEventReader xmlEventReader = xmlParserHelper.createXmlEventReader( "constraint mapping file", new CloseIgnoringInputStream( in ) );
			String schemaVersion = xmlParserHelper.getSchemaVersion( "constraint mapping file", xmlEventReader );
			xmlEventReader.close();

			in.reset();

			// The validation is done first as we manipulate the XML document before pushing it to the unmarshaller
			// and it might not be valid anymore as we might have switched the namespace to the latest namespace
			// supported.
			String schemaResourceName = getSchemaResourceName( schemaVersion );
			Schema schema = xmlParserHelper.getSchema( schemaResourceName );
			Validator validator = schema.newValidator();
			validator.validate( new StreamSource( new CloseIgnoringInputStream( in ) ) );

			in.reset();

			xmlEventReader = xmlParserHelper.createXmlEventReader( "constraint mapping file", new CloseIgnoringInputStream( in ) );
			Unmarshaller unmarshaller = jc.createUnmarshaller();
			ConstraintMappingsType mapping = getValidationConfig( xmlEventReader, unmarshaller );
			xmlEventReader.close();

			return mapping;
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

	private void processBeanType(ConstrainedTypeBuilder constrainedTypeBuilder, ConstrainedFieldBuilder constrainedFieldBuilder, ConstrainedExecutableBuilder constrainedExecutableBuilder, ConstrainedGetterBuilder constrainedGetterBuilder, String defaultPackage, BeanType bean) {
		Class<?> beanClass = classLoadingHelper.loadClass( bean.getClazz(), defaultPackage );
		checkClassHasNotBeenProcessed( processedClasses, beanClass );

		// update annotation ignores
		annotationProcessingOptions.ignoreAnnotationConstraintForClass(
				beanClass,
				bean.getIgnoreAnnotations()
		);

		ConstrainedType constrainedType = constrainedTypeBuilder.buildConstrainedType(
				bean.getClassType(),
				beanClass,
				defaultPackage
		);
		if ( constrainedType != null ) {
			addConstrainedElement( beanClass, constrainedType );
		}

		Set<ConstrainedField> constrainedFields = constrainedFieldBuilder.buildConstrainedFields(
				bean.getField(),
				beanClass,
				defaultPackage
		);
		addConstrainedElements( beanClass, constrainedFields );

		Set<ConstrainedExecutable> constrainedGetters = constrainedGetterBuilder.buildConstrainedGetters(
				bean.getGetter(),
				beanClass,
				defaultPackage

		);
		addConstrainedElements( beanClass, constrainedGetters );

		Set<ConstrainedExecutable> constrainedConstructors = constrainedExecutableBuilder.buildConstructorConstrainedExecutable(
				bean.getConstructor(),
				beanClass,
				defaultPackage
		);
		addConstrainedElements( beanClass, constrainedConstructors );

		Set<ConstrainedExecutable> constrainedMethods = constrainedExecutableBuilder.buildMethodConstrainedExecutable(
				bean.getMethod(),
				beanClass,
				defaultPackage
		);
		addConstrainedElements( beanClass, constrainedMethods );

		processedClasses.add( beanClass );
	}

	@SuppressWarnings("unchecked")
	private void parseConstraintDefinitions(List<ConstraintDefinitionType> constraintDefinitionList,
			String defaultPackage,
			Set<String> alreadyProcessedConstraintDefinitions) {
		for ( ConstraintDefinitionType constraintDefinition : constraintDefinitionList ) {
			String annotationClassName = constraintDefinition.getAnnotation();
			if ( alreadyProcessedConstraintDefinitions.contains( annotationClassName ) ) {
				throw LOG.getOverridingConstraintDefinitionsInMultipleMappingFilesException( annotationClassName );
			}
			else {
				alreadyProcessedConstraintDefinitions.add( annotationClassName );
			}

			Class<?> clazz = classLoadingHelper.loadClass( annotationClassName, defaultPackage );
			if ( !clazz.isAnnotation() ) {
				throw LOG.getIsNotAnAnnotationException( clazz );
			}
			Class<? extends Annotation> annotationClass = (Class<? extends Annotation>) clazz;

			addValidatorDefinitions( annotationClass, defaultPackage, constraintDefinition.getValidatedBy() );
		}
	}

	private <A extends Annotation> void addValidatorDefinitions(Class<A> annotationClass, String defaultPackage,
			ValidatedByType validatedByType) {
		List<ConstraintValidatorDescriptor<A>> constraintValidatorDescriptors = new ArrayList<>( validatedByType.getValue().size() );

		for ( String validatorClassName : validatedByType.getValue() ) {
			@SuppressWarnings("unchecked")
			Class<? extends ConstraintValidator<A, ?>> validatorClass = (Class<? extends ConstraintValidator<A, ?>>) classLoadingHelper
					.loadClass( validatorClassName, defaultPackage );

			if ( !ConstraintValidator.class.isAssignableFrom( validatorClass ) ) {
				throw LOG.getIsNotAConstraintValidatorClassException( validatorClass );
			}

			constraintValidatorDescriptors.add( ConstraintValidatorDescriptor.forClass( validatorClass ) );
		}
		constraintHelper.putValidatorDescriptors(
				annotationClass,
				constraintValidatorDescriptors,
				Boolean.TRUE.equals( validatedByType.getIncludeExistingValidators() )
		);
	}

	private void checkClassHasNotBeenProcessed(Set<Class<?>> processedClasses, Class<?> beanClass) {
		if ( processedClasses.contains( beanClass ) ) {
			throw LOG.getBeanClassHasAlreadyBeConfiguredInXmlException( beanClass );
		}
	}

	private void addConstrainedElement(Class<?> beanClass, ConstrainedElement constrainedElement) {
		if ( constrainedElements.containsKey( beanClass ) ) {
			constrainedElements.get( beanClass ).add( constrainedElement );
		}
		else {
			Set<ConstrainedElement> tmpList = newHashSet();
			tmpList.add( constrainedElement );
			constrainedElements.put( beanClass, tmpList );
		}
	}

	private void addConstrainedElements(Class<?> beanClass, Set<? extends ConstrainedElement> newConstrainedElements) {
		if ( constrainedElements.containsKey( beanClass ) ) {

			Set<ConstrainedElement> existingConstrainedElements = constrainedElements.get( beanClass );

			for ( ConstrainedElement constrainedElement : newConstrainedElements ) {
				if ( existingConstrainedElements.contains( constrainedElement ) ) {
						throw LOG.getConstrainedElementConfiguredMultipleTimesException(
								constrainedElement.toString()
						);
				}
			}

			existingConstrainedElements.addAll( newConstrainedElements );
		}
		else {
			Set<ConstrainedElement> tmpSet = newHashSet();
			tmpSet.addAll( newConstrainedElements );
			constrainedElements.put( beanClass, tmpSet );
		}
	}

	private ConstraintMappingsType getValidationConfig(XMLEventReader xmlEventReader, Unmarshaller unmarshaller) {
		ConstraintMappingsType constraintMappings;
		try {
			// Unmashaller#unmarshal() requires several permissions internally and doesn't use any privileged blocks
			// itself; Wrapping it here avoids that all calling code bases need to have these permissions as well
			JAXBElement<ConstraintMappingsType> root = run(
					Unmarshal.action(
							unmarshaller,
							xmlEventReader,
							ConstraintMappingsType.class
					)
			);
			constraintMappings = root.getValue();
		}
		catch (Exception e) {
			throw LOG.getErrorParsingMappingFileException( e );
		}
		return constraintMappings;
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

	/**
	 * Runs the given privileged action, using a privileged block if required.
	 * <p>
	 * <b>NOTE:</b> This must never be changed into a publicly available method to avoid execution of arbitrary
	 * privileged actions within HV's protection domain.
	 */
	private <T> T run(PrivilegedExceptionAction<T> action) throws JAXBException {
		try {
			return System.getSecurityManager() != null ? AccessController.doPrivileged( action ) : action.run();
		}
		catch (JAXBException e) {
			throw e;
		}
		catch (Exception e) {
			throw LOG.getErrorParsingMappingFileException( e );
		}
	}

}
