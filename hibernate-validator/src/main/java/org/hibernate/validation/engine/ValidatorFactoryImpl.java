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
package org.hibernate.validation.engine;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.MessageInterpolator;
import javax.validation.TraversableResolver;
import javax.validation.ValidationException;
import javax.validation.Validator;
import javax.validation.ValidatorContext;
import javax.validation.ValidatorFactory;
import javax.validation.spi.ConfigurationState;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.slf4j.Logger;
import org.xml.sax.SAXException;

import org.hibernate.validation.util.LoggerFactory;
import org.hibernate.validation.util.ReflectionHelper;
import org.hibernate.validation.util.annotationfactory.AnnotationDescriptor;
import org.hibernate.validation.util.annotationfactory.AnnotationFactory;
import org.hibernate.validation.xml.BeanType;
import org.hibernate.validation.xml.ClassType;
import org.hibernate.validation.xml.ConstraintMappingsType;
import org.hibernate.validation.xml.ConstraintType;
import org.hibernate.validation.xml.ElementType;
import org.hibernate.validation.xml.FieldType;
import org.hibernate.validation.xml.GetterType;
import org.hibernate.validation.xml.GroupsType;

/**
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 */
public class ValidatorFactoryImpl implements ValidatorFactory {

	private static final Logger log = LoggerFactory.make();
	private static final String VALIDATION_MAPPING_XSD = "META-INF/validation-mapping-1.0.xsd";
	private static final String MESSAGE_PARAM = "message";
	private static final String GROUPS_PARAM = "groups";

	private final MessageInterpolator messageInterpolator;
	private final TraversableResolver traversableResolver;
	private final ConstraintValidatorFactory constraintValidatorFactory;
	private final ConstraintHelper constraintHelper = new ConstraintHelper();
	private static final String PACKAGE_SEPERATOR = ".";

	private final Map<Class<?>, Boolean> ignoreAnnotationDefaults = new HashMap<Class<?>, Boolean>();
	private final Map<Class<?>, List<Member>> ignoreAnnotationOnMember = new HashMap<Class<?>, List<Member>>();
	private final List<Class<?>> ignoreAnnotationOnClass = new ArrayList<Class<?>>();
	private final Map<Class<?>, List<MetaConstraint<?, ?>>> constraintMap = new HashMap<Class<?>, List<MetaConstraint<?, ?>>>();

	public ValidatorFactoryImpl(ConfigurationState configurationState) {
		this.messageInterpolator = configurationState.getMessageInterpolator();
		this.constraintValidatorFactory = configurationState.getConstraintValidatorFactory();
		this.traversableResolver = configurationState.getTraversableResolver();

		parseMappingFiles( configurationState.getMappingStreams() );
		initBeanMetaData();
	}

	/**
	 * {@inheritDoc}
	 */
	public Validator getValidator() {
		return usingContext().getValidator();
	}

	/**
	 * {@inheritDoc}
	 */
	public MessageInterpolator getMessageInterpolator() {
		return messageInterpolator;
	}

	/**
	 * {@inheritDoc}
	 */
	public ValidatorContext usingContext() {
		return new ValidatorContextImpl(
				constraintValidatorFactory, messageInterpolator, traversableResolver, constraintHelper
		);
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

	private void parseMappingFiles(Set<InputStream> mappingStreams) {
		Set<Class<?>> processedClasses = new HashSet<Class<?>>();
		for ( InputStream in : mappingStreams ) {
			try {
				ConstraintMappingsType mappings = getValidationConfig( in );
				String defaultPackage = mappings.getDefaultPackage();
				for ( BeanType bean : mappings.getBean() ) {
					Class<?> beanClass = getClass( bean.getClazz(), defaultPackage );
					checkClassHasNotBeenProcessed( processedClasses, beanClass );
					Boolean ignoreAnnotations = bean.isIgnoreAnnotations() == null ? false : bean.isIgnoreAnnotations();
					ignoreAnnotationDefaults.put( beanClass, ignoreAnnotations );
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
			boolean ignoreFieldAnnotation = fieldType.isIgnoreAnnotations() == null ? false : fieldType.isIgnoreAnnotations();
			if ( ignoreFieldAnnotation ) {
				ignoreAnnotationOnMember.put( beanClass, null );
			}
			for ( ConstraintType constraint : fieldType.getConstraint() ) {
				MetaConstraint<?, ?> metaConstraint = createMetaConstraint( constraint, beanClass, field, defaultPackage );
				addMetaConstraint( beanClass, metaConstraint );
			}
		}
	}

	private void parsePropertyLevelOverrides(List<GetterType> getters, Class<?> beanClass, String defaultPackage) {
		for ( GetterType getter : getters ) {
			String getterName = getter.getName();
			if ( !ReflectionHelper.containsMethod( beanClass, getterName ) ) {
				throw new ValidationException( beanClass.getName() + " does not contain the property  " + getterName );
			}
			Method method = ReflectionHelper.getMethod( beanClass, getterName );
			boolean ignoreGetterAnnotation = getter.isIgnoreAnnotations() == null ? false : getter.isIgnoreAnnotations();
			if ( ignoreGetterAnnotation ) {
				ignoreAnnotationOnMember.put( beanClass, null );
			}
			for ( ConstraintType constraint : getter.getConstraint() ) {
				MetaConstraint<?, ?> metaConstraint = createMetaConstraint( constraint, beanClass, method, defaultPackage );
				addMetaConstraint( beanClass, metaConstraint );
			}
		}
	}

	private void parseClassLevelOverrides(ClassType classType, Class<?> beanClass, String defaultPackage) {
		if ( classType == null ) {
			return;
		}
		boolean ignoreClassAnnotation = classType.isIgnoreAnnotations() == null ? false : classType.isIgnoreAnnotations();
		if ( ignoreClassAnnotation ) {
			ignoreAnnotationOnClass.add( beanClass );
		}
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
			List<MetaConstraint<?, ?>> constraintList = new ArrayList<MetaConstraint<?, ?>>();
			constraintList.add( metaConstraint );
			constraintMap.put( beanClass, constraintList );
		}
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

	private void checkNameIsValid(String name) {
		if ( MESSAGE_PARAM.equals( name ) || GROUPS_PARAM.equals( name ) ) {
			throw new ValidationException( MESSAGE_PARAM + " and " + GROUPS_PARAM + " are reserved paramter names." );
		}
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
		String value;
		if ( serializable instanceof String ) {
			value = ( String ) serializable;
		}
		else if ( serializable instanceof JAXBElement ) {
			JAXBElement<?> elem = ( JAXBElement<?> ) serializable;
			// this is safe due to the underlying schema
			value = ( String ) elem.getValue();
		}
		else {
			throw new ValidationException( "Unexpected paramter value" );
		}

		return convertStringToReturnType( returnType, value );
	}

	private Object convertStringToReturnType(Class<?> returnType, String value) {
		Object returnValue;
		if ( returnType.isPrimitive() && returnType.getName().equals( byte.class.getName() ) ) {
			try {
				returnValue = Byte.parseByte( value );
			}
			catch ( NumberFormatException e ) {
				throw new ValidationException( "Invalid byte format", e );
			}
		}
		else if ( returnType.isPrimitive() && returnType.getName().equals( short.class.getName() ) ) {
			try {
				returnValue = Short.parseShort( value );
			}
			catch ( NumberFormatException e ) {
				throw new ValidationException( "Invalid short format", e );
			}
		}
		else if ( returnType.isPrimitive() && returnType.getName().equals( int.class.getName() ) ) {
			try {
				returnValue = Integer.parseInt( value );
			}
			catch ( NumberFormatException e ) {
				throw new ValidationException( "Invalid int format", e );
			}
		}
		else if ( returnType.isPrimitive() && returnType.getName().equals( long.class.getName() ) ) {
			try {
				returnValue = Long.parseLong( value );
			}
			catch ( NumberFormatException e ) {
				throw new ValidationException( "Invalid long format", e );
			}
		}
		else if ( returnType.isPrimitive() && returnType.getName().equals( float.class.getName() ) ) {
			try {
				returnValue = Float.parseFloat( value );
			}
			catch ( NumberFormatException e ) {
				throw new ValidationException( "Invalid float format", e );
			}
		}
		else if ( returnType.isPrimitive() && returnType.getName().equals( double.class.getName() ) ) {
			try {
				returnValue = Double.parseDouble( value );
			}
			catch ( NumberFormatException e ) {
				throw new ValidationException( "Invalid double format", e );
			}
		}
		else if ( returnType.isPrimitive() && returnType.getName().equals( boolean.class.getName() ) ) {
			returnValue = Boolean.parseBoolean( value );
		}
		else if ( returnType.isPrimitive() && returnType.getName().equals( char.class.getName() ) ) {
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
			throw new ValidationException( "Invalid return type: " + returnType );
		}
		return returnValue;
	}

	private Class<?>[] getGroups(GroupsType groupsType, String defaultPackage) {
		if ( groupsType == null ) {
			return new Class[] { };
		}

		List<Class<?>> groupList = new ArrayList<Class<?>>();
		for ( String groupClass : groupsType.getValue() ) {
			groupList.add( getClass( groupClass, defaultPackage ) );
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

	private void initBeanMetaData() {
		for ( Map.Entry<Class<?>, List<MetaConstraint<?, ?>>> entry : constraintMap.entrySet() ) {
			BeanMetaDataImpl<?> metaData = new BeanMetaDataImpl( entry.getKey(), constraintHelper );
			for ( MetaConstraint<?, ?> metaConstraint : entry.getValue() ) {
				metaData.addMetaConstraint( metaConstraint );
			}
			BeanMetaDataCache.addBeanMetaData( entry.getKey(), metaData );
		}
	}
}
