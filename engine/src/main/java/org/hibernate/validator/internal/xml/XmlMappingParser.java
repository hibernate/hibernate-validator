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
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
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
import javax.validation.Payload;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;

import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptions;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.core.ConstraintOrigin;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.internal.metadata.location.BeanConstraintLocation;
import org.hibernate.validator.internal.util.ReflectionHelper;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationDescriptor;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationFactory;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashMap;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;

/**
 * @author Hardy Ferentschik
 */
public class XmlMappingParser {

	private static final Log log = LoggerFactory.make();
	private static final String MESSAGE_PARAM = "message";
	private static final String GROUPS_PARAM = "groups";
	private static final String PAYLOAD_PARAM = "payload";
	private static final String PACKAGE_SEPARATOR = ".";

	private final Set<Class<?>> processedClasses = newHashSet();
	private final ConstraintHelper constraintHelper;
	private final AnnotationProcessingOptions annotationProcessingOptions;
	private final Map<Class<?>, Set<MetaConstraint<?>>> constraintMap;
	private final Map<Class<?>, List<Member>> cascadedMembers;
	private final Map<Class<?>, List<Class<?>>> defaultSequences;

	private final XmlParserHelper xmlParserHelper = new XmlParserHelper();

	private static final ConcurrentMap<String, String> SCHEMAS_BY_VERSION = new ConcurrentHashMap<String, String>(
			2,
			0.75f,
			1
	);

	static {
		SCHEMAS_BY_VERSION.put( "1.0", "META-INF/validation-mapping-1.0.xsd" );
		SCHEMAS_BY_VERSION.put( "1.1", "META-INF/validation-mapping-1.1.xsd" );
	}

	public XmlMappingParser(ConstraintHelper constraintHelper) {
		this.constraintHelper = constraintHelper;
		this.annotationProcessingOptions = new AnnotationProcessingOptions();
		this.constraintMap = newHashMap();
		this.cascadedMembers = newHashMap();
		this.defaultSequences = newHashMap();
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
					Class<?> beanClass = getClass( bean.getClazz(), defaultPackage );
					checkClassHasNotBeenProcessed( processedClasses, beanClass );
					annotationProcessingOptions.ignoreAnnotationConstraintForClass(
							beanClass,
							bean.getIgnoreAnnotations()
					);
					parseClassLevelOverrides( bean.getClassType(), beanClass, defaultPackage );
					parseFieldLevelOverrides( bean.getField(), beanClass, defaultPackage );
					parsePropertyLevelOverrides( bean.getGetter(), beanClass, defaultPackage );
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

	public final List<Class<?>> getDefaultSequenceForClass(Class<?> beanClass) {
		return defaultSequences.get( beanClass );
	}

	@SuppressWarnings("unchecked")
	private void parseConstraintDefinitions(List<ConstraintDefinitionType> constraintDefinitionList, String defaultPackage) {
		for ( ConstraintDefinitionType constraintDefinition : constraintDefinitionList ) {
			String annotationClassName = constraintDefinition.getAnnotation();

			Class<?> clazz = getClass( annotationClassName, defaultPackage );
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

	private void parseFieldLevelOverrides(List<FieldType> fields, Class<?> beanClass, String defaultPackage) {
		List<String> fieldNames = newArrayList();
		for ( FieldType fieldType : fields ) {
			String fieldName = fieldType.getName();
			if ( fieldNames.contains( fieldName ) ) {
				throw log.getIsDefinedTwiceInMappingXmlForBeanException( fieldName, beanClass.getName() );
			}
			else {
				fieldNames.add( fieldName );
			}
			final boolean containsField = ReflectionHelper.containsDeclaredField( beanClass, fieldName );
			if ( !containsField ) {
				throw log.getBeanDoesNotContainTheFieldException( beanClass.getName(), fieldName );
			}
			final Field field = ReflectionHelper.getDeclaredField( beanClass, fieldName );

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

	private void parsePropertyLevelOverrides(List<GetterType> getters, Class<?> beanClass, String defaultPackage) {
		List<String> getterNames = newArrayList();
		for ( GetterType getterType : getters ) {
			String getterName = getterType.getName();
			if ( getterNames.contains( getterName ) ) {
				throw log.getIsDefinedTwiceInMappingXmlForBeanException( getterName, beanClass.getName() );
			}
			else {
				getterNames.add( getterName );
			}
			boolean containsMethod = ReflectionHelper.containsMethodWithPropertyName( beanClass, getterName );
			if ( !containsMethod ) {
				throw log.getBeanDoesNotContainThePropertyException( beanClass.getName(), getterName );
			}
			final Method method = ReflectionHelper.getMethodFromPropertyName( beanClass, getterName );

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

	private void parseClassLevelOverrides(ClassType classType, Class<?> beanClass, String defaultPackage) {
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

	private List<Class<?>> createGroupSequence(GroupSequenceType groupSequenceType, String defaultPackage) {
		List<Class<?>> groupSequence = newArrayList();
		if ( groupSequenceType != null ) {
			for ( String groupName : groupSequenceType.getValue() ) {
				Class<?> group = getClass( groupName, defaultPackage );
				groupSequence.add( group );
			}
		}
		return groupSequence;
	}

	private <A extends Annotation, T> MetaConstraint<?> createMetaConstraint(ConstraintType constraint, Class<T> beanClass, Member member, String defaultPackage) {
		@SuppressWarnings("unchecked")
		Class<A> annotationClass = (Class<A>) getClass( constraint.getAnnotation(), defaultPackage );
		AnnotationDescriptor<A> annotationDescriptor = new AnnotationDescriptor<A>( annotationClass );

		if ( constraint.getMessage() != null ) {
			annotationDescriptor.setValue( MESSAGE_PARAM, constraint.getMessage() );
		}
		annotationDescriptor.setValue( GROUPS_PARAM, getGroups( constraint.getGroups(), defaultPackage ) );
		annotationDescriptor.setValue( PAYLOAD_PARAM, getPayload( constraint.getPayload(), defaultPackage ) );

		for ( ElementType elementType : constraint.getElement() ) {
			String name = elementType.getName();
			checkNameIsValid( name );
			Class<?> returnType = getAnnotationParameterType( annotationClass, name );
			Object elementValue = getElementValue( elementType, returnType );
			annotationDescriptor.setValue( name, elementValue );
		}

		A annotation;
		try {
			annotation = AnnotationFactory.create( annotationDescriptor );
		}
		catch ( RuntimeException e ) {
			throw log.getUnableToCreateAnnotationForConfiguredConstraintException( e );
		}

		java.lang.annotation.ElementType type = java.lang.annotation.ElementType.TYPE;
		if ( member instanceof Method ) {
			type = java.lang.annotation.ElementType.METHOD;
		}
		else if ( member instanceof Field ) {
			type = java.lang.annotation.ElementType.FIELD;
		}

		// we set initially ConstraintOrigin.DEFINED_LOCALLY for all xml configured constraints
		// later we will make copies of this constraint descriptor when needed and adjust the ConstraintOrigin
		ConstraintDescriptorImpl<A> constraintDescriptor = new ConstraintDescriptorImpl<A>(
				annotation, constraintHelper, type, ConstraintOrigin.DEFINED_LOCALLY
		);

		return new MetaConstraint<A>( constraintDescriptor, new BeanConstraintLocation( beanClass, member ) );
	}

	private <A extends Annotation> Class<?> getAnnotationParameterType(Class<A> annotationClass, String name) {
		Method m = ReflectionHelper.getMethod( annotationClass, name );
		if ( m == null ) {
			throw log.getAnnotationDoesNotContainAParameterException( annotationClass.getName(), name );
		}
		return m.getReturnType();
	}

	private Object getElementValue(ElementType elementType, Class<?> returnType) {
		removeEmptyContentElements( elementType );

		boolean isArray = returnType.isArray();
		if ( !isArray ) {
			if ( elementType.getContent().size() != 1 ) {
				throw log.getAttemptToSpecifyAnArrayWhereSingleValueIsExpectedException();
			}
			return getSingleValue( elementType.getContent().get( 0 ), returnType );
		}
		else {
			List<Object> values = newArrayList();
			for ( Serializable s : elementType.getContent() ) {
				values.add( getSingleValue( s, returnType.getComponentType() ) );
			}
			return values.toArray( (Object[]) Array.newInstance( returnType.getComponentType(), values.size() ) );
		}
	}

	private void removeEmptyContentElements(ElementType elementType) {
		List<Serializable> contentToDelete = newArrayList();
		for ( Serializable content : elementType.getContent() ) {
			if ( content instanceof String && ( (String) content ).matches( "[\\n ].*" ) ) {
				contentToDelete.add( content );
			}
		}
		elementType.getContent().removeAll( contentToDelete );
	}

	private Object getSingleValue(Serializable serializable, Class<?> returnType) {

		Object returnValue;
		if ( serializable instanceof String ) {
			String value = (String) serializable;
			returnValue = convertStringToReturnType( returnType, value );
		}
		else if ( serializable instanceof JAXBElement && ( (JAXBElement<?>) serializable ).getDeclaredType()
				.equals( String.class ) ) {
			JAXBElement<?> elem = (JAXBElement<?>) serializable;
			String value = (String) elem.getValue();
			returnValue = convertStringToReturnType( returnType, value );
		}
		else if ( serializable instanceof JAXBElement && ( (JAXBElement<?>) serializable ).getDeclaredType()
				.equals( AnnotationType.class ) ) {
			JAXBElement<?> elem = (JAXBElement<?>) serializable;
			AnnotationType annotationType = (AnnotationType) elem.getValue();
			try {
				@SuppressWarnings("unchecked")
				Class<Annotation> annotationClass = (Class<Annotation>) returnType;
				returnValue = createAnnotation( annotationType, annotationClass );
			}
			catch ( ClassCastException e ) {
				throw log.getUnexpectedParameterValueException( e );
			}
		}
		else {
			throw log.getUnexpectedParameterValueException();
		}
		return returnValue;

	}

	private <A extends Annotation> Annotation createAnnotation(AnnotationType annotationType, Class<A> returnType) {
		AnnotationDescriptor<A> annotationDescriptor = new AnnotationDescriptor<A>( returnType );
		for ( ElementType elementType : annotationType.getElement() ) {
			String name = elementType.getName();
			Class<?> parameterType = getAnnotationParameterType( returnType, name );
			Object elementValue = getElementValue( elementType, parameterType );
			annotationDescriptor.setValue( name, elementValue );
		}
		return AnnotationFactory.create( annotationDescriptor );
	}

	private Object convertStringToReturnType(Class<?> returnType, String value) {
		Object returnValue;
		if ( returnType.getName().equals( byte.class.getName() ) ) {
			try {
				returnValue = Byte.parseByte( value );
			}
			catch ( NumberFormatException e ) {
				throw log.getInvalidNumberFormatException( "byte", e );
			}
		}
		else if ( returnType.getName().equals( short.class.getName() ) ) {
			try {
				returnValue = Short.parseShort( value );
			}
			catch ( NumberFormatException e ) {
				throw log.getInvalidNumberFormatException( "short", e );
			}
		}
		else if ( returnType.getName().equals( int.class.getName() ) ) {
			try {
				returnValue = Integer.parseInt( value );
			}
			catch ( NumberFormatException e ) {
				throw log.getInvalidNumberFormatException( "int", e );
			}
		}
		else if ( returnType.getName().equals( long.class.getName() ) ) {
			try {
				returnValue = Long.parseLong( value );
			}
			catch ( NumberFormatException e ) {
				throw log.getInvalidNumberFormatException( "long", e );
			}
		}
		else if ( returnType.getName().equals( float.class.getName() ) ) {
			try {
				returnValue = Float.parseFloat( value );
			}
			catch ( NumberFormatException e ) {
				throw log.getInvalidNumberFormatException( "float", e );
			}
		}
		else if ( returnType.getName().equals( double.class.getName() ) ) {
			try {
				returnValue = Double.parseDouble( value );
			}
			catch ( NumberFormatException e ) {
				throw log.getInvalidNumberFormatException( "double", e );
			}
		}
		else if ( returnType.getName().equals( boolean.class.getName() ) ) {
			returnValue = Boolean.parseBoolean( value );
		}
		else if ( returnType.getName().equals( char.class.getName() ) ) {
			if ( value.length() != 1 ) {
				throw log.getInvalidCharValueException( value );
			}
			returnValue = value.charAt( 0 );
		}
		else if ( returnType.getName().equals( String.class.getName() ) ) {
			returnValue = value;
		}
		else if ( returnType.getName().equals( Class.class.getName() ) ) {
			returnValue = ReflectionHelper.loadClass( value, this.getClass() );
		}
		else {
			try {
				@SuppressWarnings("unchecked")
				Class<Enum> enumClass = (Class<Enum>) returnType;
				returnValue = Enum.valueOf( enumClass, value );
			}
			catch ( ClassCastException e ) {
				throw log.getInvalidReturnTypeException( returnType, e );
			}
		}
		return returnValue;
	}

	private void checkNameIsValid(String name) {
		if ( MESSAGE_PARAM.equals( name ) || GROUPS_PARAM.equals( name ) ) {
			throw log.getReservedParameterNamesException( MESSAGE_PARAM, GROUPS_PARAM, PAYLOAD_PARAM );
		}
	}

	private Class<?>[] getGroups(GroupsType groupsType, String defaultPackage) {
		if ( groupsType == null ) {
			return new Class[] { };
		}

		List<Class<?>> groupList = newArrayList();
		for ( String groupClass : groupsType.getValue() ) {
			groupList.add( getClass( groupClass, defaultPackage ) );
		}
		return groupList.toArray( new Class[groupList.size()] );
	}

	@SuppressWarnings("unchecked")
	private Class<? extends Payload>[] getPayload(PayloadType payloadType, String defaultPackage) {
		if ( payloadType == null ) {
			return new Class[] { };
		}

		List<Class<? extends Payload>> payloadList = newArrayList();
		for ( String groupClass : payloadType.getValue() ) {
			Class<?> payload = getClass( groupClass, defaultPackage );
			if ( !Payload.class.isAssignableFrom( payload ) ) {
				throw log.getWrongPayloadClassException( payload.getName() );
			}
			else {
				payloadList.add( (Class<? extends Payload>) payload );
			}
		}
		return payloadList.toArray( new Class[payloadList.size()] );
	}

	private Class<?> getClass(String clazz, String defaultPackage) {
		String fullyQualifiedClass;
		if ( isQualifiedClass( clazz ) ) {
			fullyQualifiedClass = clazz;
		}
		else {
			fullyQualifiedClass = defaultPackage + PACKAGE_SEPARATOR + clazz;
		}
		return ReflectionHelper.loadClass( fullyQualifiedClass, this.getClass() );
	}

	private boolean isQualifiedClass(String clazz) {
		return clazz.contains( PACKAGE_SEPARATOR );
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
