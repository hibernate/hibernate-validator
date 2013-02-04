/*
* JBoss, Home of Professional Open Source
* Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual contributors
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

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.List;
import javax.validation.Payload;
import javax.validation.ValidationException;
import javax.xml.bind.JAXBElement;

import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.core.ConstraintOrigin;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.internal.util.ReflectionHelper;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationDescriptor;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationFactory;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;

/**
 * Build constraint descriptor from XML
 *
 * @author Hardy Ferentschik
 */
public class ConstraintBuilder {
	private static final Log log = LoggerFactory.make();

	private static final String MESSAGE_PARAM = "message";
	private static final String GROUPS_PARAM = "groups";
	private static final String PAYLOAD_PARAM = "payload";

	private ConstraintBuilder() {
	}

	@SuppressWarnings("unchecked")
	public static <A extends Annotation> ConstraintDescriptorImpl<A> buildConstraintDescriptor(ConstraintType constraint,
																							   java.lang.annotation.ElementType type,
																							   String defaultPackage,
																							   ConstraintHelper constraintHelper) {
		Class<A> annotationClass = null;
		try {
			annotationClass = (Class<A>) ReflectionHelper.loadClass( constraint.getAnnotation(), defaultPackage );
		}
		catch ( ValidationException e ) {
			throw log.unableToLoadConstraintAnnotationClassException( constraint.getAnnotation(), e );
		}
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

		// we set initially ConstraintOrigin.DEFINED_LOCALLY for all xml configured constraints
		// later we will make copies of this constraint descriptor when needed and adjust the ConstraintOrigin
		return new ConstraintDescriptorImpl<A>(
				annotation, constraintHelper, type, ConstraintOrigin.DEFINED_LOCALLY
		);
	}

	public static <A extends Annotation> Annotation buildAnnotation(AnnotationType annotationType, Class<A> returnType) {
		AnnotationDescriptor<A> annotationDescriptor = new AnnotationDescriptor<A>( returnType );
		for ( ElementType elementType : annotationType.getElement() ) {
			String name = elementType.getName();
			Class<?> parameterType = getAnnotationParameterType( returnType, name );
			Object elementValue = getElementValue( elementType, parameterType );
			annotationDescriptor.setValue( name, elementValue );
		}
		return AnnotationFactory.create( annotationDescriptor );
	}

	private static void checkNameIsValid(String name) {
		if ( MESSAGE_PARAM.equals( name ) || GROUPS_PARAM.equals( name ) ) {
			throw log.getReservedParameterNamesException( MESSAGE_PARAM, GROUPS_PARAM, PAYLOAD_PARAM );
		}
	}

	private static <A extends Annotation> Class<?> getAnnotationParameterType(Class<A> annotationClass, String name) {
		Method m = ReflectionHelper.getMethod( annotationClass, name );
		if ( m == null ) {
			throw log.getAnnotationDoesNotContainAParameterException( annotationClass.getName(), name );
		}
		return m.getReturnType();
	}

	private static Object getElementValue(ElementType elementType, Class<?> returnType) {
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

	private static void removeEmptyContentElements(ElementType elementType) {
		List<Serializable> contentToDelete = newArrayList();
		for ( Serializable content : elementType.getContent() ) {
			if ( content instanceof String && ( (String) content ).matches( "[\\n ].*" ) ) {
				contentToDelete.add( content );
			}
		}
		elementType.getContent().removeAll( contentToDelete );
	}

	private static Object getSingleValue(Serializable serializable, Class<?> returnType) {

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
				returnValue = ConstraintBuilder.buildAnnotation( annotationType, annotationClass );
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

	private static Object convertStringToReturnType(Class<?> returnType, String value) {
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
			returnValue = ReflectionHelper.loadClass( value, ConstraintBuilder.class );
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

	private static Class<?>[] getGroups(GroupsType groupsType, String defaultPackage) {
		if ( groupsType == null ) {
			return new Class[] { };
		}

		List<Class<?>> groupList = newArrayList();
		for ( String groupClass : groupsType.getValue() ) {
			groupList.add( ReflectionHelper.loadClass( groupClass, defaultPackage ) );
		}
		return groupList.toArray( new Class[groupList.size()] );
	}

	@SuppressWarnings("unchecked")
	private static Class<? extends Payload>[] getPayload(PayloadType payloadType, String defaultPackage) {
		if ( payloadType == null ) {
			return new Class[] { };
		}

		List<Class<? extends Payload>> payloadList = newArrayList();
		for ( String groupClass : payloadType.getValue() ) {
			Class<?> payload = ReflectionHelper.loadClass( groupClass, defaultPackage );
			if ( !Payload.class.isAssignableFrom( payload ) ) {
				throw log.getWrongPayloadClassException( payload.getName() );
			}
			else {
				payloadList.add( (Class<? extends Payload>) payload );
			}
		}
		return payloadList.toArray( new Class[payloadList.size()] );
	}
}


