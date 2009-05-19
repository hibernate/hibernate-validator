// $Id$
/*
* JBoss, Home of Professional Open Source
* Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.hibernate.validation.engine.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ValidationException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.slf4j.Logger;
import org.xml.sax.SAXException;

import org.hibernate.validation.engine.ConstraintDescriptorImpl;
import org.hibernate.validation.engine.ConstraintHelper;
import org.hibernate.validation.engine.MetaConstraint;
import org.hibernate.validation.util.LoggerFactory;
import org.hibernate.validation.util.ReflectionHelper;
import org.hibernate.validation.util.annotationfactory.AnnotationDescriptor;
import org.hibernate.validation.util.annotationfactory.AnnotationFactory;
import org.hibernate.validation.xml.AnnotationType;
import org.hibernate.validation.xml.BeanType;
import org.hibernate.validation.xml.ClassType;
import org.hibernate.validation.xml.ConstraintDefinitionType;
import org.hibernate.validation.xml.ConstraintMappingsType;
import org.hibernate.validation.xml.ConstraintType;
import org.hibernate.validation.xml.ElementType;
import org.hibernate.validation.xml.FieldType;
import org.hibernate.validation.xml.GetterType;
import org.hibernate.validation.xml.GroupSequenceType;
import org.hibernate.validation.xml.GroupsType;
import org.hibernate.validation.xml.ValidatedByType;

/**
 * @author Hardy Ferentschik
 */
public class XmlMappingParser {

	private static final Logger log = LoggerFactory.make();
	private static final String VALIDATION_MAPPING_XSD = "META-INF/validation-mapping-1.0.xsd";
	private static final String MESSAGE_PARAM = "message";
	private static final String GROUPS_PARAM = "groups";
	private static final String PACKAGE_SEPERATOR = ".";

	private final Set<Class<?>> processedClasses = new HashSet<Class<?>>();
	private final ConstraintHelper constraintHelper;
	private final AnnotationIgnores annotationIgnores;
	private final Map<Class<?>, List<MetaConstraint<?, ? extends Annotation>>> constraintMap;
	private final Map<Class<?>, List<Member>> cascadedMembers;
	private final Map<Class<?>, List<Class<?>>> defaultSequences;

	public XmlMappingParser(ConstraintHelper constraintHelper) {
		this.constraintHelper = constraintHelper;
		this.annotationIgnores = new AnnotationIgnores();
		this.constraintMap = new HashMap<Class<?>, List<MetaConstraint<?, ? extends Annotation>>>();
		this.cascadedMembers = new HashMap<Class<?>, List<Member>>();
		this.defaultSequences = new HashMap<Class<?>, List<Class<?>>>();
	}

	public void parse(Set<InputStream> mappingStreams) {
		for ( InputStream in : mappingStreams ) {
			try {
				ConstraintMappingsType mapping = getValidationConfig( in );
				parseConstraintDefinitions( mapping.getConstraintDefinition() );
				String defaultPackage = mapping.getDefaultPackage();
				for ( BeanType bean : mapping.getBean() ) {
					Class<?> beanClass = getClass( bean.getClazz(), defaultPackage );
					checkClassHasNotBeenProcessed( processedClasses, beanClass );
					annotationIgnores.setDefaultIgnoreAnnotation( beanClass, bean.isIgnoreAnnotations() );
					parseClassLevelOverrides( bean.getClassType(), beanClass, defaultPackage );
					parseFieldLevelOverrides( bean.getField(), beanClass, defaultPackage );
					parsePropertyLevelOverrides( bean.getGetter(), beanClass, defaultPackage );
					processedClasses.add( beanClass );
				}
			}
			finally {
				try {
					in.close();
				}
				catch ( IOException e ) {
					log.warn( "Error closing input stream: {}", e.getMessage() );
				}
			}
		}
	}

	public Set<Class<?>> getProcessedClasses() {
		return processedClasses;
	}

	public AnnotationIgnores getAnnotationIgnores() {
		return annotationIgnores;
	}

	public Map<Class<?>, List<MetaConstraint<?, ? extends Annotation>>> getConstraintMap() {
		return constraintMap;
	}

	public Map<Class<?>, List<Member>> getCascadedMembers() {
		return cascadedMembers;
	}

	public Map<Class<?>, List<Class<?>>> getDefaultSequences() {
		return defaultSequences;
	}

	@SuppressWarnings("unchecked")
	private void parseConstraintDefinitions(List<ConstraintDefinitionType> constraintDefinitionList) {
		for ( ConstraintDefinitionType constraintDefinition : constraintDefinitionList ) {
			String annotationClassName = constraintDefinition.getAnnotation();
			Class<? extends Annotation> annotationClass;
			try {
				annotationClass = ( Class<? extends Annotation> ) ReflectionHelper.classForName(
						annotationClassName, this.getClass()
				);
			}
			catch ( ClassNotFoundException e ) {
				throw new ValidationException( "Unable to load class " + annotationClassName );
			}

			if ( !annotationClass.isAnnotation() ) {
				throw new ValidationException( annotationClassName + " is not an annotation" );
			}

			ValidatedByType validatedByType = constraintDefinition.getValidatedBy();
			List<Class<? extends ConstraintValidator<? extends Annotation, ?>>> constraintValidatorClasses = new ArrayList<Class<? extends ConstraintValidator<? extends Annotation, ?>>>();
			if ( validatedByType.isIncludeExistingValidators() != null && validatedByType.isIncludeExistingValidators() ) {
				constraintValidatorClasses.addAll( findConstraintValidatorClasses( annotationClass ) );
			}
			for ( JAXBElement<String> validatorClassName : validatedByType.getValue() ) {
				Class<? extends ConstraintValidator<?, ?>> validatorClass;
				try {
					validatorClass = ( Class<? extends ConstraintValidator<?, ?>> ) ReflectionHelper.classForName(
							validatorClassName.getValue(),
							this.getClass()
					);
				}
				catch ( ClassNotFoundException e ) {
					throw new ValidationException( "Unable to load class " + validatorClassName );
				}

				if ( !ConstraintValidator.class.isAssignableFrom( validatorClass ) ) {
					throw new ValidationException( validatorClass + " is not a constraint validator class" );
				}

				constraintValidatorClasses.add( validatorClass );
			}
			constraintHelper.addConstraintValidatorDefinition(
					annotationClass, constraintValidatorClasses
			);
		}
	}

	private List<Class<? extends ConstraintValidator<? extends Annotation, ?>>> findConstraintValidatorClasses(Class<? extends Annotation> annotationType) {
		List<Class<? extends ConstraintValidator<? extends Annotation, ?>>> constraintValidatorDefinitonClasses = new ArrayList<Class<? extends ConstraintValidator<? extends Annotation, ?>>>();
		if ( constraintHelper.isBuiltinConstraint( annotationType ) ) {
			constraintValidatorDefinitonClasses.addAll( constraintHelper.getBuiltInConstraints( annotationType ) );
		}
		else {
			Class<? extends ConstraintValidator<?, ?>>[] validatedBy = annotationType
					.getAnnotation( Constraint.class )
					.validatedBy();
			for ( Class<? extends ConstraintValidator<?, ?>> validator : validatedBy ) {
				//FIXME does this create a CCE at runtime?
				//FIXME if yes wrap into VE, if no we need to test the type here
				//Once resolved,we can @SuppressWarning("unchecked") on the cast
				Class<? extends ConstraintValidator<? extends Annotation, ?>> safeValidator = validator;
				constraintValidatorDefinitonClasses.add( safeValidator );
			}
		}
		return constraintValidatorDefinitonClasses;
	}

	private void checkClassHasNotBeenProcessed(Set<Class<?>> processedClasses, Class<?> beanClass) {
		if ( processedClasses.contains( beanClass ) ) {
			throw new ValidationException( beanClass.getName() + " has already be configured in xml." );
		}
	}

	private void parseFieldLevelOverrides(List<FieldType> fields, Class<?> beanClass, String defaultPackage) {
		for ( FieldType fieldType : fields ) {
			String fieldName = fieldType.getName();
			if ( !ReflectionHelper.containsField( beanClass, fieldName ) ) {
				throw new ValidationException( beanClass.getName() + " does not contain the fieldType  " + fieldName );
			}
			Field field = ReflectionHelper.getField( beanClass, fieldName );

			// ignore annotations
			boolean ignoreFieldAnnotation = fieldType.isIgnoreAnnotations() == null ? false : fieldType.isIgnoreAnnotations();
			if ( ignoreFieldAnnotation ) {
				annotationIgnores.setIgnoreAnnotationsOnMember( field );
			}

			// valid
			if ( fieldType.getValid() != null ) {
				addCascadedMember( beanClass, field );
			}

			// constraints
			for ( ConstraintType constraint : fieldType.getConstraint() ) {
				MetaConstraint<?, ?> metaConstraint = createMetaConstraint(
						constraint, beanClass, field, defaultPackage
				);
				addMetaConstraint( beanClass, metaConstraint );
			}
		}
	}

	private void parsePropertyLevelOverrides(List<GetterType> getters, Class<?> beanClass, String defaultPackage) {
		for ( GetterType getterType : getters ) {
			String getterName = getterType.getName();
			if ( !ReflectionHelper.containsMethod( beanClass, getterName ) ) {
				throw new ValidationException( beanClass.getName() + " does not contain the property  " + getterName );
			}
			Method method = ReflectionHelper.getMethod( beanClass, getterName );

			// ignore annotations
			boolean ignoreGetterAnnotation = getterType.isIgnoreAnnotations() == null ? false : getterType.isIgnoreAnnotations();
			if ( ignoreGetterAnnotation ) {
				annotationIgnores.setIgnoreAnnotationsOnMember( method );
			}

			// valid
			if ( getterType.getValid() != null ) {
				addCascadedMember( beanClass, method );
			}

			// constraints
			for ( ConstraintType constraint : getterType.getConstraint() ) {
				MetaConstraint<?, ?> metaConstraint = createMetaConstraint(
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
		boolean ignoreClassAnnotation = classType.isIgnoreAnnotations() == null ? false : classType.isIgnoreAnnotations();
		if ( ignoreClassAnnotation ) {
			annotationIgnores.setIgnoreAnnotationsOnClass( beanClass );
		}

		// group sequence
		List<Class<?>> groupSequence = createGroupSequence( classType.getGroupSequence(), defaultPackage );
		if ( !groupSequence.isEmpty() ) {
			defaultSequences.put( beanClass, groupSequence );
		}

		// constraints
		for ( ConstraintType constraint : classType.getConstraint() ) {
			MetaConstraint<?, ?> metaConstraint = createMetaConstraint( constraint, beanClass, null, defaultPackage );
			addMetaConstraint( beanClass, metaConstraint );
		}
	}

	private void addMetaConstraint(Class<?> beanClass, MetaConstraint<?, ?> metaConstraint) {
		if ( constraintMap.containsKey( beanClass ) ) {
			constraintMap.get( beanClass ).add( metaConstraint );
		}
		else {
			List<MetaConstraint<?, ? extends Annotation>> constraintList = new ArrayList<MetaConstraint<?, ? extends Annotation>>();
			constraintList.add( metaConstraint );
			constraintMap.put( beanClass, constraintList );
		}
	}

	private void addCascadedMember(Class<?> beanClass, Member member) {
		if ( cascadedMembers.containsKey( beanClass ) ) {
			cascadedMembers.get( beanClass ).add( member );
		}
		else {
			List<Member> tmpList = new ArrayList<Member>();
			tmpList.add( member );
			cascadedMembers.put( beanClass, tmpList );
		}
	}

	private List<Class<?>> createGroupSequence(GroupSequenceType groupSequenceType, String defaultPackage) {
		List<Class<?>> groupSequence = new ArrayList<Class<?>>();
		for ( JAXBElement<String> groupName : groupSequenceType.getValue() ) {
			Class<?> group = getClass( groupName.getValue(), defaultPackage );
			groupSequence.add( group );
		}
		return groupSequence;
	}

	private <A extends Annotation, T> MetaConstraint<?, ?> createMetaConstraint(ConstraintType constraint, Class<T> beanClass, Member member, String defaultPackage) {
		@SuppressWarnings("unchecked")
		Class<A> annotationClass = ( Class<A> ) getClass( constraint.getAnnotation(), defaultPackage );
		AnnotationDescriptor<A> annotationDescriptor = new AnnotationDescriptor<A>( annotationClass );

		if ( constraint.getMessage() != null ) {
			annotationDescriptor.setValue( MESSAGE_PARAM, constraint.getMessage() );
		}
		annotationDescriptor.setValue( GROUPS_PARAM, getGroups( constraint.getGroups(), defaultPackage ) );

		for ( ElementType elementType : constraint.getElement() ) {
			String name = elementType.getName();
			checkNameIsValid( name );
			Class<?> returnType = getAnnotationParamterType( annotationClass, name );
			Object elementValue = getElementValue( elementType, returnType );
			annotationDescriptor.setValue( name, elementValue );
		}

		A annotation = AnnotationFactory.create( annotationDescriptor );
		ConstraintDescriptorImpl<A> constraintDescriptor = new ConstraintDescriptorImpl<A>(
				annotation, new Class[] { }, constraintHelper
		);

		MetaConstraint<T, A> metaConstraint;
		if ( member == null ) {
			metaConstraint = new MetaConstraint<T, A>( beanClass, constraintDescriptor );
		}
		else {
			metaConstraint = new MetaConstraint<T, A>( member, beanClass, constraintDescriptor );
		}
		return metaConstraint;
	}

	private <A extends Annotation> Class<?> getAnnotationParamterType(Class<A> annotationClass, String name) {
		Method m;
		try {
			m = annotationClass.getMethod( name );
		}
		catch ( NoSuchMethodException e ) {
			throw new ValidationException( "Annotation of type " + annotationClass.getName() + " does not contain a paramter " + name + "." );
		}
		return m.getReturnType();
	}

	private Object getElementValue(ElementType elementType, Class<?> returnType) {
		removeEmptyContentElements( elementType );

		boolean isArray = returnType.isArray();
		if ( !isArray ) {
			if ( elementType.getContent().size() != 1 ) {
				throw new ValidationException( "Attempt to specify an array where single value is expected." );
			}
			return getSingleValue( elementType.getContent().get( 0 ), returnType );
		}
		else {
			List<Object> values = new ArrayList<Object>();
			for ( Serializable s : elementType.getContent() ) {
				values.add( getSingleValue( s, returnType.getComponentType() ) );
			}
			return values.toArray( ( Object[] ) Array.newInstance( returnType.getComponentType(), values.size() ) );
		}
	}

	private void removeEmptyContentElements(ElementType elementType) {
		List<Serializable> contentToDelete = new ArrayList<Serializable>();
		for ( Serializable content : elementType.getContent() ) {
			if ( content instanceof String && ( ( String ) content ).matches( "[\\n ].*" ) ) {
				contentToDelete.add( content );
			}
		}
		elementType.getContent().removeAll( contentToDelete );
	}

	private Object getSingleValue(Serializable serializable, Class<?> returnType) {

		Object returnValue;
		if ( serializable instanceof String ) {
			String value = ( String ) serializable;
			returnValue = convertStringToReturnType( returnType, value );
		}
		else if ( serializable instanceof JAXBElement && ( ( JAXBElement ) serializable ).getDeclaredType()
				.equals( String.class ) ) {
			JAXBElement<?> elem = ( JAXBElement<?> ) serializable;
			String value = ( String ) elem.getValue();
			returnValue = convertStringToReturnType( returnType, value );
		}
		else if ( serializable instanceof JAXBElement && ( ( JAXBElement ) serializable ).getDeclaredType()
				.equals( AnnotationType.class ) ) {
			JAXBElement<?> elem = ( JAXBElement<?> ) serializable;
			AnnotationType annotationType = ( AnnotationType ) elem.getValue();
			try {
				@SuppressWarnings("unchecked")
				Class<Annotation> annotationClass = ( Class<Annotation> ) returnType;
				returnValue = createAnnotation( annotationType, annotationClass );
			}
			catch ( ClassCastException e ) {
				throw new ValidationException( "Unexpected paramter value" );
			}
		}
		else {
			throw new ValidationException( "Unexpected paramter value" );
		}
		return returnValue;

	}

	private <A extends Annotation> Annotation createAnnotation(AnnotationType annotationType, Class<A> returnType) {
		AnnotationDescriptor<A> annotationDescriptor = new AnnotationDescriptor<A>( returnType );
		for ( ElementType elementType : annotationType.getElement() ) {
			String name = elementType.getName();
			Class<?> paramterType = getAnnotationParamterType( returnType, name );
			Object elementValue = getElementValue( elementType, paramterType );
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
				throw new ValidationException( "Invalid byte format", e );
			}
		}
		else if ( returnType.getName().equals( short.class.getName() ) ) {
			try {
				returnValue = Short.parseShort( value );
			}
			catch ( NumberFormatException e ) {
				throw new ValidationException( "Invalid short format", e );
			}
		}
		else if ( returnType.getName().equals( int.class.getName() ) ) {
			try {
				returnValue = Integer.parseInt( value );
			}
			catch ( NumberFormatException e ) {
				throw new ValidationException( "Invalid int format", e );
			}
		}
		else if ( returnType.getName().equals( long.class.getName() ) ) {
			try {
				returnValue = Long.parseLong( value );
			}
			catch ( NumberFormatException e ) {
				throw new ValidationException( "Invalid long format", e );
			}
		}
		else if ( returnType.getName().equals( float.class.getName() ) ) {
			try {
				returnValue = Float.parseFloat( value );
			}
			catch ( NumberFormatException e ) {
				throw new ValidationException( "Invalid float format", e );
			}
		}
		else if ( returnType.getName().equals( double.class.getName() ) ) {
			try {
				returnValue = Double.parseDouble( value );
			}
			catch ( NumberFormatException e ) {
				throw new ValidationException( "Invalid double format", e );
			}
		}
		else if ( returnType.getName().equals( boolean.class.getName() ) ) {
			returnValue = Boolean.parseBoolean( value );
		}
		else if ( returnType.getName().equals( char.class.getName() ) ) {
			if ( value.length() != 1 ) {
				throw new ValidationException( "Invalid char value: " + value );
			}
			returnValue = value.charAt( 0 );
		}
		else if ( returnType.getName().equals( String.class.getName() ) ) {
			returnValue = value;
		}
		else if ( returnType.getName().equals( Class.class.getName() ) ) {
			try {
				returnValue = ReflectionHelper.classForName( value, this.getClass() );
			}
			catch ( ClassNotFoundException e ) {
				throw new ValidationException( "Unable to instantiate class: " + value );
			}
		}
		else {
			try {
				@SuppressWarnings("unchecked")
				Class<Enum> enumClass = ( Class<Enum> ) returnType;
				returnValue = Enum.valueOf( enumClass, value );
			}
			catch ( ClassCastException e ) {
				throw new ValidationException( "Invalid return type: " + returnType + ". Should be a enumeration type." );
			}
		}
		return returnValue;
	}

	private void checkNameIsValid(String name) {
		if ( MESSAGE_PARAM.equals( name ) || GROUPS_PARAM.equals( name ) ) {
			throw new ValidationException( MESSAGE_PARAM + " and " + GROUPS_PARAM + " are reserved paramter names." );
		}
	}

	private Class<?>[] getGroups(GroupsType groupsType, String defaultPackage) {
		if ( groupsType == null ) {
			return new Class[] { };
		}

		List<Class<?>> groupList = new ArrayList<Class<?>>();
		for ( JAXBElement<String> groupClass : groupsType.getValue() ) {
			groupList.add( getClass( groupClass.getValue(), defaultPackage ) );
		}
		return groupList.toArray( new Class[groupList.size()] );
	}

	private Class<?> getClass(String clazz, String defaultPackage) {
		String fullyQualifiedClass;
		if ( isQualifiedClass( clazz ) ) {
			fullyQualifiedClass = clazz;
		}
		else {
			fullyQualifiedClass = defaultPackage + PACKAGE_SEPERATOR + clazz;
		}
		try {
			return ReflectionHelper.classForName( fullyQualifiedClass, this.getClass() );
		}
		catch ( Exception e ) {
			throw new ValidationException( "Unable to instantiate class " + fullyQualifiedClass );
		}
	}

	private boolean isQualifiedClass(String clazz) {
		return clazz.contains( PACKAGE_SEPERATOR );
	}

	private ConstraintMappingsType getValidationConfig(InputStream in) {
		ConstraintMappingsType constraintMappings;
		Schema schema = getMappingSchema();
		try {
			JAXBContext jc = JAXBContext.newInstance( ConstraintMappingsType.class );
			Unmarshaller unmarshaller = jc.createUnmarshaller();
			unmarshaller.setSchema( schema );
			StreamSource stream = new StreamSource( in );
			JAXBElement<ConstraintMappingsType> root = unmarshaller.unmarshal( stream, ConstraintMappingsType.class );
			constraintMappings = root.getValue();
		}
		catch ( JAXBException e ) {
			String msg = "Error parsing mapping file.";
			log.error( msg );
			throw new ValidationException( msg, e );
		}
		return constraintMappings;
	}

	private Schema getMappingSchema() {
		URL schemaUrl = this.getClass().getClassLoader().getResource( VALIDATION_MAPPING_XSD );
		SchemaFactory sf = SchemaFactory.newInstance( javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI );
		Schema schema = null;
		try {
			schema = sf.newSchema( schemaUrl );
		}
		catch ( SAXException e ) {
			log.warn( "Unable to create schema for {}: {}", VALIDATION_MAPPING_XSD, e.getMessage() );
		}
		return schema;
	}
}
