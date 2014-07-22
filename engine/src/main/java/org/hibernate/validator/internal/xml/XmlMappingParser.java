/*
* JBoss, Home of Professional Open Source
* Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual contributors
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.security.AccessController;
import java.security.PrivilegedAction;
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
import javax.xml.transform.stream.StreamSource;
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
import org.hibernate.validator.internal.util.privilegedactions.LoadClass;
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

	private static final ConcurrentMap<String, String> SCHEMAS_BY_VERSION = new ConcurrentHashMap<String, String>(
			2,
			0.75f,
			1
	);

	static {
		SCHEMAS_BY_VERSION.put( "1.0", "META-INF/validation-mapping-1.0.xsd" );
		SCHEMAS_BY_VERSION.put( "1.1", "META-INF/validation-mapping-1.1.xsd" );
	}

	public XmlMappingParser(ConstraintHelper constraintHelper, ParameterNameProvider parameterNameProvider) {
		this.constraintHelper = constraintHelper;
		this.annotationProcessingOptions = new AnnotationProcessingOptionsImpl();
		this.defaultSequences = newHashMap();
		this.constrainedElements = newHashMap();
		this.xmlParserHelper = new XmlParserHelper();
		this.parameterNameProvider = parameterNameProvider;
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

			Set<String> alreadyProcessedConstraintDefinitions = newHashSet();
			for ( InputStream in : mappingStreams ) {
				String schemaVersion = xmlParserHelper.getSchemaVersion( "constraint mapping file", in );
				String schemaResourceName = getSchemaResourceName( schemaVersion );
				Schema schema = xmlParserHelper.getSchema( schemaResourceName );

				Unmarshaller unmarshaller = jc.createUnmarshaller();
				unmarshaller.setSchema( schema );

				ConstraintMappingsType mapping = getValidationConfig( in, unmarshaller );
				String defaultPackage = mapping.getDefaultPackage();

				parseConstraintDefinitions(
						mapping.getConstraintDefinition(),
						defaultPackage,
						alreadyProcessedConstraintDefinitions
				);

				for ( BeanType bean : mapping.getBean() ) {
					Class<?> beanClass = ClassLoadingHelper.loadClass( bean.getClazz(), defaultPackage );
					checkClassHasNotBeenProcessed( processedClasses, beanClass );

					// update annotation ignores
					annotationProcessingOptions.ignoreAnnotationConstraintForClass(
							beanClass,
							bean.getIgnoreAnnotations()
					);

					ConstrainedType constrainedType = ConstrainedTypeBuilder.buildConstrainedType(
							bean.getClassType(),
							beanClass,
							defaultPackage,
							constraintHelper,
							annotationProcessingOptions,
							defaultSequences
					);
					if ( constrainedType != null ) {
						addConstrainedElement( beanClass, constrainedType );
					}

					Set<ConstrainedField> constrainedFields = ConstrainedFieldBuilder.buildConstrainedFields(
							bean.getField(),
							beanClass,
							defaultPackage,
							constraintHelper,
							annotationProcessingOptions
					);
					addConstrainedElements( beanClass, constrainedFields );

					Set<ConstrainedExecutable> constrainedGetters = ConstrainedGetterBuilder.buildConstrainedGetters(
							bean.getGetter(),
							beanClass,
							defaultPackage,
							constraintHelper,
							annotationProcessingOptions
					);
					addConstrainedElements( beanClass, constrainedGetters );

					Set<ConstrainedExecutable> constrainedConstructors = ConstrainedExecutableBuilder.buildConstructorConstrainedExecutable(
							bean.getConstructor(),
							beanClass,
							defaultPackage,
							parameterNameProvider,
							constraintHelper,
							annotationProcessingOptions
					);
					addConstrainedElements( beanClass, constrainedConstructors );

					Set<ConstrainedExecutable> constrainedMethods = ConstrainedExecutableBuilder.buildMethodConstrainedExecutable(
							bean.getMethod(),
							beanClass,
							defaultPackage,
							parameterNameProvider,
							constraintHelper,
							annotationProcessingOptions
					);
					addConstrainedElements( beanClass, constrainedMethods );

					processedClasses.add( beanClass );
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

			Class<?> clazz = ClassLoadingHelper.loadClass( annotationClassName, defaultPackage );
			if ( !clazz.isAnnotation() ) {
				throw log.getIsNotAnAnnotationException( annotationClassName );
			}
			Class<? extends Annotation> annotationClass = (Class<? extends Annotation>) clazz;

			addValidatorDefinitions( annotationClass, constraintDefinition.getValidatedBy() );
		}
	}

	private <A extends Annotation> void addValidatorDefinitions(Class<A> annotationClass, ValidatedByType validatedByType) {
		List<Class<? extends ConstraintValidator<A, ?>>> constraintValidatorClasses = newArrayList();

		for ( String validatorClassName : validatedByType.getValue() ) {
			@SuppressWarnings("unchecked")
			Class<? extends ConstraintValidator<A, ?>> validatorClass = (Class<? extends ConstraintValidator<A, ?>>) run(
					LoadClass.action( validatorClassName, this.getClass() )
			);


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

	private ConstraintMappingsType getValidationConfig(InputStream in, Unmarshaller unmarshaller) {
		ConstraintMappingsType constraintMappings;
		try {
			// check whether mark is supported, if so we can reset the stream in order to allow reuse of Configuration
			boolean markSupported = in.markSupported();
			if ( markSupported ) {
				in.mark( Integer.MAX_VALUE );
			}

			StreamSource stream = new StreamSource( new CloseIgnoringInputStream( in ) );

			// Unmashaller#unmarshal() requires several permissions internally and doesn't use any privileged blocks
			// itself; Wrapping it here avoids that all calling code bases need to have these permissions as well
			JAXBElement<ConstraintMappingsType> root = run( Unmarshal.action( unmarshaller, stream, ConstraintMappingsType.class ) );
			constraintMappings = root.getValue();

			if ( markSupported ) {
				try {
					in.reset();
				}
				catch ( IOException e ) {
					log.debug( "Unable to reset input stream." );
				}
			}
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
	private <T> T run(PrivilegedAction<T> action) {
		return System.getSecurityManager() != null ? AccessController.doPrivileged( action ) : action.run();
	}

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

	// JAXB closes the underlying input stream
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
