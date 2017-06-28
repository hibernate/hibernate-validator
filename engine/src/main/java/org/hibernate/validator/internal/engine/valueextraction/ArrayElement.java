/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.valueextraction;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedArrayType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import org.hibernate.validator.internal.util.ReflectionHelper;

/**
 * A pseudo type variable which is used to represent constraints applied to the elements of an array.
 *
 * @author Gunnar Morling
 * @author Guillaume Smet
 */
public class ArrayElement implements TypeVariable<Class<?>> {

	private final Class<?> containerClass;

	public ArrayElement(AnnotatedArrayType annotatedArrayType) {
		Type arrayElementType = annotatedArrayType.getAnnotatedGenericComponentType().getType();
		if ( arrayElementType == boolean.class ) {
			containerClass = boolean[].class;
		}
		else if ( arrayElementType == int.class ) {
			containerClass = int[].class;
		}
		else if ( arrayElementType == long.class ) {
			containerClass = long[].class;
		}
		else if ( arrayElementType == double.class ) {
			containerClass = double[].class;
		}
		else if ( arrayElementType == float.class ) {
			containerClass = float[].class;
		}
		else if ( arrayElementType == byte.class ) {
			containerClass = byte[].class;
		}
		else if ( arrayElementType == short.class ) {
			containerClass = short[].class;
		}
		else if ( arrayElementType == char.class ) {
			containerClass = char[].class;
		}
		else {
			containerClass = Object[].class;
		}
	}

	public ArrayElement(Type arrayType) {
		Class<?> arrayClass = ReflectionHelper.getClassFromType( arrayType );
		if ( arrayClass.getComponentType().isPrimitive() ) {
			this.containerClass = arrayClass;
		}
		else {
			this.containerClass = Object[].class;
		}
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

	public Class<?> getContainerClass() {
		return containerClass;
	}

	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		if ( obj == null ) {
			return false;
		}
		if ( getClass() != obj.getClass() ) {
			return false;
		}
		ArrayElement other = (ArrayElement) obj;

		return this.containerClass.equals( other.containerClass );
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + containerClass.hashCode();
		return result;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append( getClass().getSimpleName() )
				.append( "<" )
				.append( containerClass )
				.append( ">" );
		return sb.toString();
	}
}
