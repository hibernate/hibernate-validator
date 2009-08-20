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
package org.hibernate.validation.xml;

import java.io.InputStream;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.AccessController;
import java.util.ArrayList;
import java.util.Collections;
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

import org.hibernate.validation.metadata.AnnotationIgnores;
import org.hibernate.validation.metadata.ConstraintDescriptorImpl;
import org.hibernate.validation.metadata.ConstraintHelper;
import org.hibernate.validation.metadata.MetaConstraint;
import org.hibernate.validation.util.ContainsField;
import org.hibernate.validation.util.ContainsMethod;
import org.hibernate.validation.util.GetClassLoader;
import org.hibernate.validation.util.GetDeclaredField;
import org.hibernate.validation.util.GetMethod;
import org.hibernate.validation.util.GetMethodFromPropertyName;
import org.hibernate.validation.util.LoadClass;
import org.hibernate.validation.util.LoggerFactory;
import org.hibernate.validation.util.annotationfactory.AnnotationDescriptor;
import org.hibernate.validation.util.annotationfactory.AnnotationFactory;

/**
 * @author Hardy Ferentschik
 */
public class XmlMappingParser {

	private static final Logger log = LoggerFactory.make();
	private static final String VALIDATION_MAPPING_XSD = "META-INF/validation-mapping-1.0.xsd";
	private static final String MESSAGE_PARAM = "message";
	private static final String GROUPS_PARAM = "groups";
	private static final String PACKAGE_SEPARATOR = ".";

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
			ConstraintMappingsType mapping = getValidationConfig( in );
			String defaultPackage = mapping.getDefaultPackage();

			parseConstraintDefinitions( mapping.getConstraintDefinition(), defaultPackage );

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
	}

	public Set<Class<?>> getProcessedClasses() {
		return processedClasses;
	}

	public AnnotationIgnores getAnnotationIgnores() {
		return annotationIgnores;
	}

	public <T> List<MetaConstraint<T, ? extends Annotation>> getConstraintsForClass(Class<T> beanClass) {
		List<MetaConstraint<T, ? extends Annotation>> list = new ArrayList<MetaConstraint<T, ? extends Annotation>>();
		if ( constraintMap.containsKey( beanClass ) ) {
			for ( MetaConstraint<?, ? extends Annotation> metaConstraint : constraintMap.get( beanClass ) ) {
				@SuppressWarnings("unchecked") // safe cast since the list of meta constraints is always specific to the bean type
						MetaConstraint<T, ? extends Annotation> boundMetaConstraint = ( MetaConstraint<T, ? extends Annotation> ) metaConstraint;
				list.add( boundMetaConstraint );
			}
			return list;
		}
		else {
			return Collections.emptyList();
		}
	}

	public List<Member> getCascadedMembersForClass(Class<?> beanClass) {
		if ( cascadedMembers.containsKey( beanClass ) ) {
			return cascadedMembers.get( beanClass );
		}
		else {
			return Collections.emptyList();
		}
	}

	public List<Class<?>> getDefaultSequenceForClass(Class<?> beanClass) {
		if ( defaultSequences.containsKey( beanClass ) ) {
			return defaultSequences.get( beanClass );
		}
		else {
			return Collections.emptyList();
		}
	}

	@SuppressWarnings("unchecked")
	private void parseConstraintDefinitions(List<ConstraintDefinitionType> constraintDefinitionList, String defaultPackage) {
		for ( ConstraintDefinitionType constraintDefinition : constraintDefinitionList ) {
			String annotationClassName = constraintDefinition.getAnnotation();

			Class<?> clazz = getClass( annotationClassName, defaultPackage );
			if ( !clazz.isAnnotation() ) {
				throw new ValidationException( annotationClassName + " is not an annotation" );
			}
			Class<? extends Annotation> annotationClass = (Class<? extends Annotation>) clazz;

			ValidatedByType validatedByType = constraintDefinition.getValidatedBy();
			List<Class<? extends ConstraintValidator<? extends Annotation, ?>>> constraintValidatorClasses = new ArrayList<Class<? extends ConstraintValidator<? extends Annotation, ?>>>();
			if ( validatedByType.isIncludeExistingValidators() != null && validatedByType.isIncludeExistingValidators() ) {
				constraintValidatorClasses.addAll( findConstraintValidatorClasses( annotationClass ) );
			}
			for ( JAXBElement<String> validatorClassName : validatedByType.getValue() ) {
				Class<? extends ConstraintValidator<?, ?>> validatorClass;
				validatorClass = ( Class<? extends ConstraintValidator<?, ?>> ) loadClass(
						validatorClassName.getValue(),
						this.getClass()
				);


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

	private Class<?> loadClass(String className, Class<?> caller) {
		LoadClass action = LoadClass.action( className, caller );
		if ( System.getSecurityManager() != null ) {
			return AccessController.doPrivileged( action );
		}
		else {
			return action.run();
		}
	}

	private List<Class<? extends ConstraintValidator<? extends Annotation, ?>>> findConstraintValidatorClasses(Class<? extends Annotation> annotationType) {
		List<Class<? extends ConstraintValidator<? extends Annotation, ?>>> constraintValidatorDefinitionClasses = new ArrayList<Class<? extends ConstraintValidator<? extends Annotation, ?>>>();
		if ( constraintHelper.isBuiltinConstraint( annotationType ) ) {
			constraintValidatorDefinitionClasses.addAll( constraintHelper.getBuiltInConstraints( annotationType ) );
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
				constraintValidatorDefinitionClasses.add( safeValidator );
			}
		}
		return constraintValidatorDefinitionClasses;
	}

	private void checkClassHasNotBeenProcessed(Set<Class<?>> processedClasses, Class<?> beanClass) {
		if ( processedClasses.contains( beanClass ) ) {
			throw new ValidationException( beanClass.getName() + " has already be configured in xml." );
		}
	}

	private void parseFieldLevelOverrides(List<FieldType> fields, Class<?> beanClass, String defaultPackage) {
		List<String> fieldNames = new ArrayList<String>();
		for ( FieldType fieldType : fields ) {
			String fieldName = fieldType.getName();
			if ( fieldNames.contains( fieldName ) ) {
				throw new ValidationException( fieldName + " is defined twice in mapping xml for bean " + beanClass.getName() );
			}
			else {
				fieldNames.add( fieldName );
			}
			final boolean containsField;
			ContainsField containsAction = ContainsField.action( beanClass, fieldName );
			if ( System.getSecurityManager() != null ) {
				containsField = AccessController.doPrivileged( containsAction );
			}
			else {
				containsField = containsAction.run();
			}
			if ( !containsField ) {
				throw new ValidationException( beanClass.getName() + " does not contain the fieldType  " + fieldName );
			}
			GetDeclaredField action = GetDeclaredField.action( beanClass, fieldName );
			final Field field;
			if ( System.getSecurityManager() != null ) {
				field = AccessController.doPrivileged( action );
			}
			else {
				field = action.run();
			}

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
		List<String> getterNames = new ArrayList<String>();
		for ( GetterType getterType : getters ) {
			String getterName = getterType.getName();
			if ( getterNames.contains( getterName ) ) {
				throw new ValidationException( getterName + " is defined twice in mapping xml for bean " + beanClass.getName() );
			}
			else {
				getterNames.add( getterName );
			}
			ContainsMethod cmAction = ContainsMethod.action( beanClass, getterName );
			boolean containsMethod;
			if ( System.getSecurityManager() != null ) {
				containsMethod = AccessController.doPrivileged( cmAction );
			}
			else {
				containsMethod = cmAction.run();
			}
			if ( !containsMethod ) {
				throw new ValidationException( beanClass.getName() + " does not contain the property  " + getterName );
			}
			final Method method;
			GetMethodFromPropertyName action = GetMethodFromPropertyName.action( beanClass, getterName );
			if ( System.getSecurityManager() != null ) {
				method = AccessController.doPrivileged( action );
			}
			else {
				method = action.run();
			}

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
			Class<?> returnType = getAnnotationParameterType( annotationClass, name );
			Object elementValue = getElementValue( elementType, returnType );
			annotationDescriptor.setValue( name, elementValue );
		}

		A annotation = AnnotationFactory.create( annotationDescriptor );
		ConstraintDescriptorImpl<A> constraintDescriptor = new ConstraintDescriptorImpl<A>(
				annotation, constraintHelper
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

	private <A extends Annotation> Class<?> getAnnotationParameterType(Class<A> annotationClass, String name) {
		Method m;
		GetMethod action = GetMethod.action( annotationClass, name );
		if ( System.getSecurityManager() != null ) {
			m = AccessController.doPrivileged( action );
		}
		else {
			m = action.run();
		}

		if ( m == null ) {
			throw new ValidationException( "Annotation of type " + annotationClass.getName() + " does not contain a parameter " + name + "." );
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
				throw new ValidationException( "Unexpected parameter value" );
			}
		}
		else {
			throw new ValidationException( "Unexpected parameter value" );
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
			returnValue = loadClass( value, this.getClass() );
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
			throw new ValidationException( MESSAGE_PARAM + " and " + GROUPS_PARAM + " are reserved parameter names." );
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
			fullyQualifiedClass = defaultPackage + PACKAGE_SEPARATOR + clazz;
		}
		return loadClass( fullyQualifiedClass, this.getClass() );
	}

	private boolean isQualifiedClass(String clazz) {
		return clazz.contains( PACKAGE_SEPARATOR );
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
		boolean isSecured = System.getSecurityManager() != null;
		GetClassLoader action = GetClassLoader.fromClass( XmlMappingParser.class );
		ClassLoader loader = isSecured ? AccessController.doPrivileged( action ) : action.run();
		URL schemaUrl = loader.getResource( VALIDATION_MAPPING_XSD );
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
