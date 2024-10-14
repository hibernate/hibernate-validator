/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.properties.javabean;

import static org.hibernate.validator.internal.util.logging.Messages.MESSAGES;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import org.hibernate.validator.internal.util.TypeHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * @author Guillaume Smet
 */
public class JavaBeanParameter implements JavaBeanAnnotatedElement {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	static final Annotation[] EMPTY_PARAMETER_ANNOTATIONS = new Annotation[0];

	private final int index;

	private final Parameter parameter;

	private final Class<?> type;

	private final Type genericType;

	private final AnnotatedType annotatedType;

	private final Annotation[] annotationsForJDK8303112;

	JavaBeanParameter(int index, Parameter parameter, Class<?> type, AnnotatedType annotatedType,
			Annotation[] annotationsForJDK8303112) {
		this.index = index;
		this.parameter = parameter;
		this.type = type;
		this.genericType = getErasedTypeIfTypeVariable( annotatedType.getType() );
		this.annotatedType = annotatedType;
		this.annotationsForJDK8303112 = annotationsForJDK8303112;
	}

	public int getIndex() {
		return index;
	}

	@Override
	public Class<?> getType() {
		return type;
	}

	@Override
	public AnnotatedType getAnnotatedType() {
		return annotatedType;
	}

	@Override
	public Annotation[] getDeclaredAnnotations() {
		try {
			if ( annotationsForJDK8303112 != null ) {
				// Working around https://bugs.openjdk.org/browse/JDK-8303112
				return annotationsForJDK8303112.clone();
			}
			return parameter.getDeclaredAnnotations();
		}
		catch (ArrayIndexOutOfBoundsException ex) {
			// This looks like our workaround failed... assume there were no annotations and hope for the best.
			LOG.warn( MESSAGES.constraintOnConstructorOfNonStaticInnerClass(), ex );
			return EMPTY_PARAMETER_ANNOTATIONS;
		}
	}

	@Override
	public Type getGenericType() {
		return genericType;
	}

	@Override
	public TypeVariable<?>[] getTypeParameters() {
		return type.getTypeParameters();
	}

	@Override
	public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
		if ( annotationsForJDK8303112 != null ) {
			// Working around https://bugs.openjdk.org/browse/JDK-8303112
			for ( Annotation annotation : annotationsForJDK8303112 ) {
				if ( annotationClass.isAssignableFrom( annotation.annotationType() ) ) {
					@SuppressWarnings("unchecked")
					A castAnnotation = (A) annotation;
					return castAnnotation;
				}
			}
			return null;
		}
		return parameter.getAnnotation( annotationClass );
	}

	private static Type getErasedTypeIfTypeVariable(Type genericType) {
		if ( genericType instanceof TypeVariable ) {
			return TypeHelper.getErasedType( genericType );
		}

		return genericType;
	}
}
