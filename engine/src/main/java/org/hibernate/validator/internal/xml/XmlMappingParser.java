/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.xml;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.validation.ConstraintValidator;
import javax.validation.ParameterNameProvider;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLEventReader;
import javax.xml.validation.Schema;

import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptions;
import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptionsImpl;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement;
import org.hibernate.validator.internal.metadata.raw.ConstrainedExecutable;
import org.hibernate.validator.internal.metadata.raw.ConstrainedField;
import org.hibernate.validator.internal.metadata.raw.ConstrainedType;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.util.privilegedactions.NewJaxbContext;
import org.hibernate.validator.internal.util.privilegedactions.Unmarshal;

import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashMap;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;

/**
 * XML parser for validation-mapping files.
 *
 * @author Hardy Ferentschik
 */
public class XmlMappingParser {

	private static final Log log = LoggerFactory.make();

	private final Set<Class<?>> processedClasses = newHashSet();
	private final ConstraintHelper constraintHelper;
	private final AnnotationProcessingOptionsImpl annotationProcessingOptions;
	private final Map<Class<?>, List<Class<?>>> defaultSequences;
	private final Map<Class<?>, Set<ConstrainedElement>> constrainedElements;

	private final XmlParserHelper xmlParserHelper;
	private final ParameterNameProvider parameterNameProvider;

	private final ClassLoadingHelper classLoadingHelper;

	private static final ConcurrentMap<String, String> SCHEMAS_BY_VERSION = new ConcurrentHashMap<String, String>(
			2,
			0.75f,
			1
	);

	static {
		SCHEMAS_BY_VERSION.put( "1.0", "META-INF/validation-mapping-1.0.xsd" );
		SCHEMAS_BY_VERSION.put( "1.1", "META-INF/validation-mapping-1.1.xsd" );
	}

	public XmlMappingParser(ConstraintHelper constraintHelper, ParameterNameProvider parameterNameProvider,
			ClassLoader externalClassLoader) {
		this.constraintHelper = constraintHelper;
		this.annotationProcessingOptions = new AnnotationProcessingOptionsImpl();
		this.defaultSequences = newHashMap();
		this.constrainedElements = newHashMap();
		this.xmlParserHelper = new XmlParserHelper();
		this.parameterNameProvider = parameterNameProvider;
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
					constraintHelper
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
					parameterNameProvider,
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

				// check whether mark is supported, if so we can reset the stream in order to allow reuse of Configuration
				boolean markSupported = in.markSupported();
				if ( markSupported ) {
					in.mark( Integer.MAX_VALUE );
				}

				XMLEventReader xmlEventReader = xmlParserHelper.createXmlEventReader( "constraint mapping file", new CloseIgnoringInputStream( in ) );
				String schemaVersion = xmlParserHelper.getSchemaVersion( "constraint mapping file", xmlEventReader );
				String schemaResourceName = getSchemaResourceName( schemaVersion );
				Schema schema = xmlParserHelper.getSchema( schemaResourceName );

				Unmarshaller unmarshaller = jc.createUnmarshaller();
				unmarshaller.setSchema( schema );

				ConstraintMappingsType mapping = getValidationConfig( xmlEventReader, unmarshaller );
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

				if ( markSupported ) {
					try {
						in.reset();
					}
					catch ( IOException e ) {
						log.debug( "Unable to reset input stream." );
					}
				}
			}
		}
		catch ( JAXBException e ) {
			throw log.getErrorParsingMappingFileException( e );
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
				throw log.getOverridingConstraintDefinitionsInMultipleMappingFilesException( annotationClassName );
			}
			else {
				alreadyProcessedConstraintDefinitions.add( annotationClassName );
			}

			Class<?> clazz = classLoadingHelper.loadClass( annotationClassName, defaultPackage );
			if ( !clazz.isAnnotation() ) {
				throw log.getIsNotAnAnnotationException( annotationClassName );
			}
			Class<? extends Annotation> annotationClass = (Class<? extends Annotation>) clazz;

			addValidatorDefinitions( annotationClass, defaultPackage, constraintDefinition.getValidatedBy() );
		}
	}

	private <A extends Annotation> void addValidatorDefinitions(Class<A> annotationClass, String defaultPackage,
			ValidatedByType validatedByType) {
		List<Class<? extends ConstraintValidator<A, ?>>> constraintValidatorClasses = newArrayList();

		for ( String validatorClassName : validatedByType.getValue() ) {
			@SuppressWarnings("unchecked")
			Class<? extends ConstraintValidator<A, ?>> validatorClass = (Class<? extends ConstraintValidator<A, ?>>) classLoadingHelper
					.loadClass( validatorClassName, defaultPackage );

			if ( !ConstraintValidator.class.isAssignableFrom( validatorClass ) ) {
				throw log.getIsNotAConstraintValidatorClassException( validatorClass );
			}

			constraintValidatorClasses.add( validatorClass );
		}
		constraintHelper.putValidatorClasses(
				annotationClass,
				constraintValidatorClasses,
				Boolean.TRUE.equals( validatedByType.getIncludeExistingValidators() )
		);
	}

	private void checkClassHasNotBeenProcessed(Set<Class<?>> processedClasses, Class<?> beanClass) {
		if ( processedClasses.contains( beanClass ) ) {
			throw log.getBeanClassHasAlreadyBeConfiguredInXmlException( beanClass.getName() );
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
				for ( ConstrainedElement existingConstrainedElement : existingConstrainedElements ) {
					if ( existingConstrainedElement.getLocation().getMember() != null &&
							existingConstrainedElement.getLocation().getMember().equals(
									constrainedElement.getLocation().getMember()
							) ) {
						ConstraintLocation location = constrainedElement.getLocation();
						throw log.getConstrainedElementConfiguredMultipleTimesException(
								location.getMember().toString()
						);
					}
				}
				existingConstrainedElements.add( constrainedElement );
			}
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
		catch ( Exception e ) {
			throw log.getErrorParsingMappingFileException( e );
		}
		return constraintMappings;
	}

	private String getSchemaResourceName(String schemaVersion) {
		String schemaResource = SCHEMAS_BY_VERSION.get( schemaVersion );

		if ( schemaResource == null ) {
			throw log.getUnsupportedSchemaVersionException( "constraint mapping file", schemaVersion );
		}

		return schemaResource;
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
		catch ( JAXBException e ) {
			throw e;
		}
		catch ( Exception e ) {
			throw log.getErrorParsingMappingFileException( e );
		}
	}

	// HV-1025 - On some JVMs (eg the IBM JVM) the JAXB implementation closes the underlying input stream.
	// To prevent this we wrap the input stream to be able to ignore the close event. It is the responsibility
	// of the client API to close the stream (as per Bean Validation spec, see javax.validation.Configuration).
	private static class CloseIgnoringInputStream extends FilterInputStream {
		public CloseIgnoringInputStream(InputStream in) {
			super( in );
		}

		@Override
		public void close() {
			// do nothing
		}
	}
}
