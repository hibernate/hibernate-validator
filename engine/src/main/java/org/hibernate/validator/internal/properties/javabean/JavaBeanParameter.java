/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.properties.javabean;

import static org.hibernate.validator.internal.util.logging.Messages.MESSAGES;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * @author Guillaume Smet
 */
public class JavaBeanParameter implements JavaBeanAnnotatedElement {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private static final Annotation[] EMPTY_PARAMETER_ANNOTATIONS = new Annotation[0];

	private final int index;

	private final Parameter parameter;

	private final Class<?> type;

	private final Type genericType;

	JavaBeanParameter(int index, Parameter parameter, Class<?> type, Type genericType) {
		this.index = index;
		this.parameter = parameter;
		this.type = type;
		this.genericType = genericType;
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
		return parameter.getAnnotatedType();
	}

	@Override
	public Annotation[] getDeclaredAnnotations() {
		try {
			return parameter.getDeclaredAnnotations();
		}
		catch (ArrayIndexOutOfBoundsException ex) {
			// This looks like a JVM bug we are trying to work around, kept as is for now
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
		return parameter.getType().getTypeParameters();
	}

	@Override
	public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
		return parameter.getAnnotation( annotationClass );
	}
}
