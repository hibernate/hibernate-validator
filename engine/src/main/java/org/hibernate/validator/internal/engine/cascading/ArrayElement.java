/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.cascading;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

/**
 * A pseudo type variable which is used to represent constraints applied to the elements of an array.
 *
 * @author Gunnar Morling
 */
public class ArrayElement implements TypeVariable<Class<?>> {

	public static final ArrayElement INSTANCE = new ArrayElement();

	private ArrayElement() {
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
		return "ArrayElement.INSTANCE";
	}
}
