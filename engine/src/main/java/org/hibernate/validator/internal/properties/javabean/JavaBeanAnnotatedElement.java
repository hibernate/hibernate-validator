/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.properties.javabean;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

/**
 * @author Guillaume Smet
 */
public interface JavaBeanAnnotatedElement {

	Type getType();

	AnnotatedType getAnnotatedType();

	Annotation[] getDeclaredAnnotations();

	Type getGenericType();

	TypeVariable<?>[] getTypeParameters();

	<A extends Annotation> A getAnnotation(Class<A> annotationClass);

	default boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
		return getAnnotation( annotationClass ) != null;
	}
}
