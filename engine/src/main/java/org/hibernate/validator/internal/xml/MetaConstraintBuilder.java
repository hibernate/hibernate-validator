/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.xml;

import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import javax.validation.Payload;
import javax.validation.ValidationException;
import javax.xml.bind.JAXBElement;

import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorManager;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.core.MetaConstraints;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.internal.util.TypeResolutionHelper;
import org.hibernate.validator.internal.util.annotation.AnnotationDescriptor;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.util.privilegedactions.GetMethod;
import org.hibernate.validator.internal.xml.binding.AnnotationType;
import org.hibernate.validator.internal.xml.binding.ConstraintType;
import org.hibernate.validator.internal.xml.binding.ElementType;
import org.hibernate.validator.internal.xml.binding.GroupsType;
import org.hibernate.validator.internal.xml.binding.PayloadType;

/**
 * Build meta constraint from XML
 *
 * @author Hardy Ferentschik
 */
class MetaConstraintBuilder {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private static final Pattern IS_ONLY_WHITESPACE = Pattern.compile( "\\s*" );
	private static final Class[] EMPTY_CLASSES_ARRAY = new Class[0];

	private final ClassLoadingHelper classLoadingHelper;
	private final ConstraintHelper constraintHelper;
	private final TypeResolutionHelper typeResolutionHelper;
	private final ValueExtractorManager valueExtractorManager;

	MetaConstraintBuilder(ClassLoadingHelper classLoadingHelper, ConstraintHelper constraintHelper, TypeResolutionHelper typeResolutionHelper,
			ValueExtractorManager valueExtractorManager) {
		this.classLoadingHelper = classLoadingHelper;
		this.constraintHelper = constraintHelper;
		this.typeResolutionHelper = typeResolutionHelper;
		this.valueExtractorManager = valueExtractorManager;
	}

	@SuppressWarnings("unchecked")
	<A extends Annotation> MetaConstraint<A> buildMetaConstraint(ConstraintLocation constraintLocation,
																			   ConstraintType constraint,
																			   java.lang.annotation.ElementType type,
																			   String defaultPackage,
																			   ConstraintDescriptorImpl.ConstraintType constraintType) {
		Class<A> annotationClass;
		try {
			annotationClass = (Class<A>) classLoadingHelper.loadClass( constraint.getAnnotation(), defaultPackage );
		}
		catch (ValidationException e) {
			throw LOG.getUnableToLoadConstraintAnnotationClassException( constraint.getAnnotation(), e );
		}
		ConstraintAnnotationDescriptor.Builder<A> annotationDescriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( annotationClass );

		if ( constraint.getMessage() != null ) {
			annotationDescriptorBuilder.setMessage( constraint.getMessage() );
		}
		annotationDescriptorBuilder.setGroups( getGroups( constraint.getGroups(), defaultPackage ) )
				.setPayload( getPayload( constraint.getPayload(), defaultPackage ) );

		for ( ElementType elementType : constraint.getElement() ) {
			String name = elementType.getName();
			checkNameIsValid( name );
			Class<?> returnType = getAnnotationParameterType( annotationClass, name );
			Object elementValue = getElementValue( elementType, returnType, defaultPackage );
			annotationDescriptorBuilder.setAttribute( name, elementValue );
		}

		ConstraintAnnotationDescriptor<A> annotationDescriptor;
		try {
			annotationDescriptor = annotationDescriptorBuilder.build();
		}
		catch (RuntimeException e) {
			throw LOG.getUnableToCreateAnnotationForConfiguredConstraintException( e );
		}

		// we set initially ConstraintOrigin.DEFINED_LOCALLY for all xml configured constraints
		// later we will make copies of this constraint descriptor when needed and adjust the ConstraintOrigin
		ConstraintDescriptorImpl<A> constraintDescriptor = new ConstraintDescriptorImpl<A>(
				constraintHelper, constraintLocation.getMember(), annotationDescriptor, type, constraintType
		);

		return MetaConstraints.create( typeResolutionHelper, valueExtractorManager, constraintDescriptor, constraintLocation );
	}

	private <A extends Annotation> Annotation buildAnnotation(AnnotationType annotationType, Class<A> returnType, String defaultPackage) {
		AnnotationDescriptor.Builder<A> annotationDescriptorBuilder = new AnnotationDescriptor.Builder<>( returnType );
		for ( ElementType elementType : annotationType.getElement() ) {
			String name = elementType.getName();
			Class<?> parameterType = getAnnotationParameterType( returnType, name );
			Object elementValue = getElementValue( elementType, parameterType, defaultPackage );
			annotationDescriptorBuilder.setAttribute( name, elementValue );
		}
		return annotationDescriptorBuilder.build().getAnnotation();
	}

	private static void checkNameIsValid(String name) {
		if ( ConstraintHelper.MESSAGE.equals( name ) || ConstraintHelper.GROUPS.equals( name ) ) {
			throw LOG.getReservedParameterNamesException( ConstraintHelper.MESSAGE, ConstraintHelper.GROUPS, ConstraintHelper.PAYLOAD );
		}
	}

	private static <A extends Annotation> Class<?> getAnnotationParameterType(Class<A> annotationClass, String name) {
		Method m = run( GetMethod.action( annotationClass, name ) );
		if ( m == null ) {
			throw LOG.getAnnotationDoesNotContainAParameterException( annotationClass, name );
		}
		return m.getReturnType();
	}

	private Object getElementValue(ElementType elementType, Class<?> returnType, String defaultPackage) {
		removeEmptyContentElements( elementType );

		boolean isArray = returnType.isArray();
		if ( !isArray ) {
			if ( elementType.getContent().size() == 0 ) {
				if ( returnType == String.class ) {
					return "";
				}
				else {
					throw LOG.getEmptyElementOnlySupportedWhenCharSequenceIsExpectedExpection();
				}
			}
			else if ( elementType.getContent().size() > 1 ) {
				throw LOG.getAttemptToSpecifyAnArrayWhereSingleValueIsExpectedException();
			}
			return getSingleValue( elementType.getContent().get( 0 ), returnType, defaultPackage );
		}
		else {
			List<Object> values = newArrayList();
			for ( Serializable s : elementType.getContent() ) {
				values.add( getSingleValue( s, returnType.getComponentType(), defaultPackage ) );
			}
			return values.toArray( (Object[]) Array.newInstance( returnType.getComponentType(), values.size() ) );
		}
	}

	private static void removeEmptyContentElements(ElementType elementType) {
		for ( Iterator<Serializable> contentIterator = elementType.getContent().iterator(); contentIterator.hasNext(); ) {
			Serializable content = contentIterator.next();
			if ( content instanceof String && IS_ONLY_WHITESPACE.matcher( (String) content ).matches() ) {
				contentIterator.remove();
			}
		}
	}

	private Object getSingleValue(Serializable serializable, Class<?> returnType, String defaultPackage) {

		Object returnValue;
		if ( serializable instanceof String ) {
			String value = (String) serializable;
			returnValue = convertStringToReturnType( returnType, value, defaultPackage );
		}
		else if ( serializable instanceof JAXBElement && ( (JAXBElement<?>) serializable ).getDeclaredType()
				.equals( String.class ) ) {
			JAXBElement<?> elem = (JAXBElement<?>) serializable;
			String value = (String) elem.getValue();
			returnValue = convertStringToReturnType( returnType, value, defaultPackage );
		}
		else if ( serializable instanceof JAXBElement && ( (JAXBElement<?>) serializable ).getDeclaredType()
				.equals( AnnotationType.class ) ) {
			JAXBElement<?> elem = (JAXBElement<?>) serializable;
			AnnotationType annotationType = (AnnotationType) elem.getValue();
			try {
				@SuppressWarnings("unchecked")
				Class<Annotation> annotationClass = (Class<Annotation>) returnType;
				returnValue = buildAnnotation( annotationType, annotationClass, defaultPackage );
			}
			catch (ClassCastException e) {
				throw LOG.getUnexpectedParameterValueException( e );
			}
		}
		else {
			throw LOG.getUnexpectedParameterValueException();
		}
		return returnValue;

	}

	private Object convertStringToReturnType(Class<?> returnType, String value, String defaultPackage) {
		Object returnValue;
		if ( returnType == byte.class ) {
			try {
				returnValue = Byte.parseByte( value );
			}
			catch (NumberFormatException e) {
				throw LOG.getInvalidNumberFormatException( "byte", e );
			}
		}
		else if ( returnType == short.class ) {
			try {
				returnValue = Short.parseShort( value );
			}
			catch (NumberFormatException e) {
				throw LOG.getInvalidNumberFormatException( "short", e );
			}
		}
		else if ( returnType == int.class ) {
			try {
				returnValue = Integer.parseInt( value );
			}
			catch (NumberFormatException e) {
				throw LOG.getInvalidNumberFormatException( "int", e );
			}
		}
		else if ( returnType == long.class ) {
			try {
				returnValue = Long.parseLong( value );
			}
			catch (NumberFormatException e) {
				throw LOG.getInvalidNumberFormatException( "long", e );
			}
		}
		else if ( returnType == float.class ) {
			try {
				returnValue = Float.parseFloat( value );
			}
			catch (NumberFormatException e) {
				throw LOG.getInvalidNumberFormatException( "float", e );
			}
		}
		else if ( returnType == double.class ) {
			try {
				returnValue = Double.parseDouble( value );
			}
			catch (NumberFormatException e) {
				throw LOG.getInvalidNumberFormatException( "double", e );
			}
		}
		else if ( returnType == boolean.class ) {
			returnValue = Boolean.parseBoolean( value );
		}
		else if ( returnType == char.class ) {
			if ( value.length() != 1 ) {
				throw LOG.getInvalidCharValueException( value );
			}
			returnValue = value.charAt( 0 );
		}
		else if ( returnType == String.class ) {
			returnValue = value;
		}
		else if ( returnType == Class.class ) {
			returnValue = classLoadingHelper.loadClass( value, defaultPackage );
		}
		else {
			try {
				@SuppressWarnings("unchecked")
				Class<Enum> enumClass = (Class<Enum>) returnType;
				returnValue = Enum.valueOf( enumClass, value );
			}
			catch (ClassCastException e) {
				throw LOG.getInvalidReturnTypeException( returnType, e );
			}
		}
		return returnValue;
	}

	private Class<?>[] getGroups(GroupsType groupsType, String defaultPackage) {
		if ( groupsType == null ) {
			return EMPTY_CLASSES_ARRAY;
		}

		List<Class<?>> groupList = newArrayList();
		for ( String groupClass : groupsType.getValue() ) {
			groupList.add( classLoadingHelper.loadClass( groupClass, defaultPackage ) );
		}
		return groupList.toArray( new Class[groupList.size()] );
	}

	@SuppressWarnings("unchecked")
	private Class<? extends Payload>[] getPayload(PayloadType payloadType, String defaultPackage) {
		if ( payloadType == null ) {
			return EMPTY_CLASSES_ARRAY;
		}

		List<Class<? extends Payload>> payloadList = newArrayList();
		for ( String groupClass : payloadType.getValue() ) {
			Class<?> payload = classLoadingHelper.loadClass( groupClass, defaultPackage );
			if ( !Payload.class.isAssignableFrom( payload ) ) {
				throw LOG.getWrongPayloadClassException( payload );
			}
			else {
				payloadList.add( (Class<? extends Payload>) payload );
			}
		}
		return payloadList.toArray( new Class[payloadList.size()] );
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
}
