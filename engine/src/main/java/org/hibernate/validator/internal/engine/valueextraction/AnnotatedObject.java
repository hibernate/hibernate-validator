/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.valueextraction;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

/**
 * A pseudo type variable used to express that the annotated element itself (e.g. a list) has been marked for
 * cascaded validation.
 *
 * @author Gunnar Morling
 */
public class AnnotatedObject implements TypeVariable<Class<?>> {

	public static final AnnotatedObject INSTANCE = new AnnotatedObject();

	private AnnotatedObject() {
	}

	@Override
	public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Annotation[] getAnnotations() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Annotation[] getDeclaredAnnotations() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Type[] getBounds() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Class<?> getGenericDeclaration() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getName() {
		throw new UnsupportedOperationException();
	}

	@Override
	public AnnotatedType[] getAnnotatedBounds() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString() {
		return "AnnotatedObject.INSTANCE";
	}
}
