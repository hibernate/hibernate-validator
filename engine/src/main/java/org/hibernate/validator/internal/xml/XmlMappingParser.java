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
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ParameterNameProvider;
import javax.validation.ValidationException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;

import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptions;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.internal.metadata.location.BeanConstraintLocation;
import org.hibernate.validator.internal.metadata.location.CrossParameterConstraintLocation;
import org.hibernate.validator.internal.metadata.location.ExecutableConstraintLocation;
import org.hibernate.validator.internal.metadata.raw.ConfigurationSource;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement;
import org.hibernate.validator.internal.metadata.raw.ConstrainedExecutable;
import org.hibernate.validator.internal.metadata.raw.ConstrainedParameter;
import org.hibernate.validator.internal.metadata.raw.ExecutableElement;
import org.hibernate.validator.internal.util.ReflectionHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

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
	private final AnnotationProcessingOptions annotationProcessingOptions;
	private final Map<Class<?>, Set<MetaConstraint<?>>> constraintMap;
	private final Map<Class<?>, List<Member>> cascadedMembers;
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
		this.annotationProcessingOptions = new AnnotationProcessingOptions();
		this.constraintMap = newHashMap();
		this.cascadedMembers = newHashMap();
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
			JAXBContext jc = JAXBContext.newInstance( ConstraintMappingsType.class );

			for ( InputStream in : mappingStreams ) {
				String schemaVersion = xmlParserHelper.getSchemaVersion( "constraint mapping file", in );
				String schemaResourceName = getSchemaResourceName( schemaVersion );
				Schema schema = xmlParserHelper.getSchema( schemaResourceName );

				Unmarshaller unmarshaller = jc.createUnmarshaller();
				unmarshaller.setSchema( schema );

				ConstraintMappingsType mapping = getValidationConfig( in, unmarshaller );
				String defaultPackage = mapping.getDefaultPackage();

				parseConstraintDefinitions( mapping.getConstraintDefinition(), defaultPackage );

				for ( BeanType bean : mapping.getBean() ) {
					Class<?> beanClass = ReflectionHelper.loadClass( bean.getClazz(), defaultPackage );
					checkClassHasNotBeenProcessed( processedClasses, beanClass );
					annotationProcessingOptions.ignoreAnnotationConstraintForClass(
							beanClass,
							bean.getIgnoreAnnotations()
					);
					parseClassLevelConstraints( bean.getClassType(), beanClass, defaultPackage );
					parseFieldLevelConstraints( bean.getField(), beanClass, defaultPackage );
					parsePropertyLevelConstraints( bean.getGetter(), beanClass, defaultPackage );
					parseConstructorConstraints( bean.getConstructor(), beanClass, defaultPackage );
					parseMethodConstraints( bean.getMethod(), beanClass, defaultPackage );
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

	public final <T> Set<MetaConstraint<?>> getConstraintsForClass(Class<T> beanClass) {
		Set<MetaConstraint<?>> theValue = constraintMap.get( beanClass );

		return theValue != null ? theValue : Collections.<MetaConstraint<?>>emptySet();
	}

	public final List<Member> getCascadedMembersForClass(Class<?> beanClass) {
		if ( cascadedMembers.containsKey( beanClass ) ) {
			return cascadedMembers.get( beanClass );
		}
		else {
			return Collections.emptyList();
		}
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
	private void parseConstraintDefinitions(List<ConstraintDefinitionType> constraintDefinitionList, String defaultPackage) {
		for ( ConstraintDefinitionType constraintDefinition : constraintDefinitionList ) {
			String annotationClassName = constraintDefinition.getAnnotation();

			Class<?> clazz = ReflectionHelper.loadClass( annotationClassName, defaultPackage );
			if ( !clazz.isAnnotation() ) {
				throw log.getIsNotAnAnnotationException( annotationClassName );
			}
			Class<? extends Annotation> annotationClass = (Class<? extends Annotation>) clazz;

			ValidatedByType validatedByType = constraintDefinition.getValidatedBy();
			List<Class<? extends ConstraintValidator<? extends Annotation, ?>>> constraintValidatorClasses = newArrayList();
			if ( validatedByType.getIncludeExistingValidators() != null && validatedByType.getIncludeExistingValidators() ) {
				constraintValidatorClasses.addAll( findConstraintValidatorClasses( annotationClass ) );
			}
			for ( String validatorClassName : validatedByType.getValue() ) {
				Class<? extends ConstraintValidator<?, ?>> validatorClass;
				validatorClass = (Class<? extends ConstraintValidator<?, ?>>) ReflectionHelper.loadClass(
						validatorClassName,
						this.getClass()
				);


				if ( !ConstraintValidator.class.isAssignableFrom( validatorClass ) ) {
					throw log.getIsNotAConstraintValidatorClassException( validatorClass );
				}

				constraintValidatorClasses.add( validatorClass );
			}
			constraintHelper.addConstraintValidatorDefinition(
					annotationClass, constraintValidatorClasses
			);
		}
	}

	private List<Class<? extends ConstraintValidator<? extends Annotation, ?>>> findConstraintValidatorClasses(Class<? extends Annotation> annotationType) {
		List<Class<? extends ConstraintValidator<? extends Annotation, ?>>> constraintValidatorDefinitionClasses = newArrayList();
		if ( constraintHelper.isBuiltinConstraint( annotationType ) ) {
			constraintValidatorDefinitionClasses.addAll( constraintHelper.getBuiltInConstraints( annotationType ) );
		}
		else {
			Class<? extends ConstraintValidator<?, ?>>[] validatedBy = annotationType
					.getAnnotation( Constraint.class )
					.validatedBy();
			constraintValidatorDefinitionClasses.addAll( Arrays.asList( validatedBy ) );
		}
		return constraintValidatorDefinitionClasses;
	}

	private void checkClassHasNotBeenProcessed(Set<Class<?>> processedClasses, Class<?> beanClass) {
		if ( processedClasses.contains( beanClass ) ) {
			throw log.getBeanClassHasAlreadyBeConfiguredInXmlException( beanClass.getName() );
		}
	}

	private void parseFieldLevelConstraints(List<FieldType> fields, Class<?> beanClass, String defaultPackage) {
		List<String> fieldNames = newArrayList();
		for ( FieldType fieldType : fields ) {
			String fieldName = fieldType.getName();
			if ( fieldNames.contains( fieldName ) ) {
				throw log.getIsDefinedTwiceInMappingXmlForBeanException( fieldName, beanClass.getName() );
			}
			else {
				fieldNames.add( fieldName );
			}

			final Field field = ReflectionHelper.getDeclaredField( beanClass, fieldName );
			if ( field == null ) {
				throw log.getBeanDoesNotContainTheFieldException( beanClass.getName(), fieldName );
			}

			// ignore annotations
			boolean ignoreFieldAnnotation = fieldType.getIgnoreAnnotations() == null ? false : fieldType.getIgnoreAnnotations();
			if ( ignoreFieldAnnotation ) {
				annotationProcessingOptions.ignorePropertyLevelConstraintAnnotationsOnMember( field );
			}

			// valid
			if ( fieldType.getValid() != null ) {
				addCascadedMember( beanClass, field );
			}

			// constraints
			for ( ConstraintType constraint : fieldType.getConstraint() ) {
				MetaConstraint<?> metaConstraint = createMetaConstraint(
						constraint, beanClass, field, defaultPackage
				);
				addMetaConstraint( beanClass, metaConstraint );
			}
		}
	}

	private void parsePropertyLevelConstraints(List<GetterType> getters, Class<?> beanClass, String defaultPackage) {
		List<String> getterNames = newArrayList();
		for ( GetterType getterType : getters ) {
			String getterName = getterType.getName();
			if ( getterNames.contains( getterName ) ) {
				throw log.getIsDefinedTwiceInMappingXmlForBeanException( getterName, beanClass.getName() );
			}
			else {
				getterNames.add( getterName );
			}

			final Method method = ReflectionHelper.getMethodFromPropertyName( beanClass, getterName );
			if ( method == null ) {
				throw log.getBeanDoesNotContainThePropertyException( beanClass.getName(), getterName );
			}

			// ignore annotations
			boolean ignoreGetterAnnotation = getterType.getIgnoreAnnotations() == null ? false : getterType.getIgnoreAnnotations();
			if ( ignoreGetterAnnotation ) {
				annotationProcessingOptions.ignorePropertyLevelConstraintAnnotationsOnMember( method );
			}

			// valid
			if ( getterType.getValid() != null ) {
				addCascadedMember( beanClass, method );
			}

			// constraints
			for ( ConstraintType constraint : getterType.getConstraint() ) {
				MetaConstraint<?> metaConstraint = createMetaConstraint(
						constraint, beanClass, method, defaultPackage
				);
				addMetaConstraint( beanClass, metaConstraint );
			}
		}
	}

	private void parseMethodConstraints(List<MethodType> methods, Class<?> beanClass, String defaultPackage) {
		for ( MethodType methodType : methods ) {
			// parse the parameters
			List<Class<?>> parameterTypes = createParameterTypes(
					methodType.getParameter(),
					beanClass,
					defaultPackage
			);

			String methodName = methodType.getName();

			final Method method = ReflectionHelper.getDeclaredMethod(
					beanClass,
					methodName,
					parameterTypes.toArray( new Class[parameterTypes.size()] )
			);

			if ( method == null ) {
				throw log.getBeanDoesNotContainMethodException(
						beanClass.getName(),
						methodName,
						parameterTypes
				);
			}

			ExecutableElement constructorExecutableElement = ExecutableElement.forMethod( method );

			parseExecutableType(
					beanClass,
					defaultPackage,
					methodType.getParameter(),
					methodType.getCrossParameterConstraint(),
					methodType.getReturnValue(),
					constructorExecutableElement
			);

			// ignore annotations
			boolean ignoreConstructorAnnotations = methodType.getIgnoreAnnotations() == null ? false : methodType
					.getIgnoreAnnotations();
			if ( ignoreConstructorAnnotations ) {
				annotationProcessingOptions.ignoreConstraintAnnotationsOnMethod( method );
			}
		}
	}

	private void parseConstructorConstraints(List<ConstructorType> constructors, Class<?> beanClass, String defaultPackage) {
		for ( ConstructorType constructorType : constructors ) {
			// parse the parameters
			List<Class<?>> constructorParameterTypes = createParameterTypes(
					constructorType.getParameter(),
					beanClass,
					defaultPackage
			);

			final Constructor constructor = ReflectionHelper.getConstructor(
					beanClass,
					constructorParameterTypes.toArray( new Class[constructorParameterTypes.size()] )
			);
			if ( constructor == null ) {
				throw log.getBeanDoesNotContainConstructorException( beanClass.getName(), constructorParameterTypes );
			}

			ExecutableElement constructorExecutableElement = ExecutableElement.forConstructor( constructor );

			parseExecutableType(
					beanClass,
					defaultPackage,
					constructorType.getParameter(),
					constructorType.getCrossParameterConstraint(),
					constructorType.getReturnValue(),
					constructorExecutableElement
			);

			// ignore annotations
			boolean ignoreConstructorAnnotations = constructorType.getIgnoreAnnotations() == null ? false : constructorType
					.getIgnoreAnnotations();
			if ( ignoreConstructorAnnotations ) {
				annotationProcessingOptions.ignoreConstraintAnnotationsOnConstructor( constructor );
			}
		}
	}

	private void parseExecutableType(Class<?> beanClass,
									 String defaultPackage,
									 List<ParameterType> parameterTypeList,
									 List<ConstraintType> crossParameterConstraintList,
									 ReturnValueType returnValueType,
									 ExecutableElement executableElement) {
		List<ConstrainedParameter> parameterMetaData = ConstraintParameterBuilder.buildParameterConstraints(
				parameterTypeList,
				executableElement,
				defaultPackage,
				constraintHelper,
				parameterNameProvider
		);

		Set<MetaConstraint<?>> crossParameterConstraints = newHashSet();
		CrossParameterConstraintLocation constraintLocation = new CrossParameterConstraintLocation(
				executableElement
		);
		for ( ConstraintType constraintType : crossParameterConstraintList ) {
			ConstraintDescriptorImpl<?> constraintDescriptor = ConstraintBuilder.buildConstraintDescriptor(
					constraintType,
					executableElement.getElementType(),
					defaultPackage,
					constraintHelper
			);
			@SuppressWarnings("unchecked")
			MetaConstraint<?> metaConstraint = new MetaConstraint( constraintDescriptor, constraintLocation );
			crossParameterConstraints.add( metaConstraint );
		}

		// parse the return value
		Set<MetaConstraint<?>> returnValueConstraints = newHashSet();
		Map<Class<?>, Class<?>> groupConversions = newHashMap();
		boolean isCascaded = parseReturnValueType(
				returnValueType,
				executableElement,
				returnValueConstraints,
				groupConversions,
				defaultPackage
		);

		ConstrainedExecutable constrainedExecutable = new ConstrainedExecutable(
				ConfigurationSource.XML,
				new ExecutableConstraintLocation( executableElement ),
				parameterMetaData,
				crossParameterConstraints,
				returnValueConstraints,
				groupConversions,
				isCascaded
		);

		addConstrainedElement( beanClass, constrainedExecutable );
	}

	private boolean parseReturnValueType(ReturnValueType returnValueType,
										 ExecutableElement executableElement,
										 Set<MetaConstraint<?>> returnValueConstraints,
										 Map<Class<?>, Class<?>> groupConversions,
										 String defaultPackage) {
		if ( returnValueType == null ) {
			return false;
		}

		ExecutableConstraintLocation constraintLocation = new ExecutableConstraintLocation(
				executableElement
		);
		for ( ConstraintType constraintType : returnValueType.getConstraint() ) {
			ConstraintDescriptorImpl<?> constraintDescriptor = ConstraintBuilder.buildConstraintDescriptor(
					constraintType,
					executableElement.getElementType(),
					defaultPackage,
					constraintHelper
			);
			@SuppressWarnings("unchecked")
			MetaConstraint<?> metaConstraint = new MetaConstraint( constraintDescriptor, constraintLocation );
			returnValueConstraints.add( metaConstraint );
		}
		groupConversions.putAll(
				GroupConversionBuilder.buildGroupConversionMap(
						returnValueType.getConvertGroup(),
						defaultPackage
				)
		);

		return returnValueType.getValid() != null;
	}

	private List<Class<?>> createParameterTypes(List<ParameterType> parameterList,
												Class<?> beanClass,
												String defaultPackage) {
		List<Class<?>> parameterTypes = newArrayList();
		for ( ParameterType parameterType : parameterList ) {
			String type = null;
			try {
				type = parameterType.getType();
				Class<?> parameterClass = ReflectionHelper.loadClass( type, defaultPackage );
				parameterTypes.add( parameterClass );
			}
			catch ( ValidationException e ) {
				throw log.getInvalidParameterTypeException( type, beanClass.getName() );
			}
		}

		return parameterTypes;
	}

	private void parseClassLevelConstraints(ClassType classType, Class<?> beanClass, String defaultPackage) {
		if ( classType == null ) {
			return;
		}

		// ignore annotation
		if ( classType.getIgnoreAnnotations() != null ) {
			annotationProcessingOptions.ignoreClassLevelConstraintAnnotations(
					beanClass,
					classType.getIgnoreAnnotations()
			);
		}

		// group sequence
		List<Class<?>> groupSequence = createGroupSequence( classType.getGroupSequence(), defaultPackage );
		if ( !groupSequence.isEmpty() ) {
			defaultSequences.put( beanClass, groupSequence );
		}

		// constraints
		for ( ConstraintType constraint : classType.getConstraint() ) {
			MetaConstraint<?> metaConstraint = createMetaConstraint( constraint, beanClass, null, defaultPackage );
			addMetaConstraint( beanClass, metaConstraint );
		}
	}

	private void addMetaConstraint(Class<?> beanClass, MetaConstraint<?> metaConstraint) {
		if ( constraintMap.containsKey( beanClass ) ) {
			constraintMap.get( beanClass ).add( metaConstraint );
		}
		else {
			Set<MetaConstraint<?>> constraintList = newHashSet();
			constraintList.add( metaConstraint );
			constraintMap.put( beanClass, constraintList );
		}
	}

	private void addCascadedMember(Class<?> beanClass, Member member) {
		if ( cascadedMembers.containsKey( beanClass ) ) {
			cascadedMembers.get( beanClass ).add( member );
		}
		else {
			List<Member> tmpList = newArrayList();
			tmpList.add( member );
			cascadedMembers.put( beanClass, tmpList );
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

	private List<Class<?>> createGroupSequence(GroupSequenceType groupSequenceType, String defaultPackage) {
		List<Class<?>> groupSequence = newArrayList();
		if ( groupSequenceType != null ) {
			for ( String groupName : groupSequenceType.getValue() ) {
				Class<?> group = ReflectionHelper.loadClass( groupName, defaultPackage );
				groupSequence.add( group );
			}
		}
		return groupSequence;
	}

	private <A extends Annotation, T> MetaConstraint<?> createMetaConstraint(ConstraintType constraint,
																			 Class<T> beanClass,
																			 Member member,
																			 String defaultPackage) {
		java.lang.annotation.ElementType type = java.lang.annotation.ElementType.TYPE;
		if ( member instanceof Method ) {
			type = java.lang.annotation.ElementType.METHOD;
		}
		else if ( member instanceof Field ) {
			type = java.lang.annotation.ElementType.FIELD;
		}

		ConstraintDescriptorImpl<A> constraintDescriptor = ConstraintBuilder.buildConstraintDescriptor(
				constraint,
				type,
				defaultPackage,
				constraintHelper
		);

		return new MetaConstraint<A>( constraintDescriptor, new BeanConstraintLocation( beanClass, member ) );
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
			JAXBElement<ConstraintMappingsType> root = unmarshaller.unmarshal( stream, ConstraintMappingsType.class );
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
		catch ( JAXBException e ) {
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
