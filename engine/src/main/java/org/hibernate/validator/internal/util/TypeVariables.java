/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.util;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import org.hibernate.validator.internal.engine.valueextraction.AnnotatedObject;
import org.hibernate.validator.internal.engine.valueextraction.ArrayElement;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * Provides some utility methods for TypeVariables.
 *
 * @author Guillaume Smet
 */
public class TypeVariables {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private TypeVariables() {
	}

	public static Class<?> getContainerClass(TypeVariable<?> typeParameter) {
		if ( isAnnotatedObject( typeParameter ) ) {
			return null;
		}
		else if ( isArrayElement( typeParameter ) ) {
			return ( (ArrayElement) typeParameter ).getContainerClass();
		}
		else {
			return getDeclaringClass( typeParameter );
		}
	}

	public static TypeVariable<?> getActualTypeParameter(TypeVariable<?> typeParameter) {
		if ( isInternal( typeParameter ) ) {
			return null;
		}
		else {
			return typeParameter;
		}
	}

	public static boolean isInternal(TypeVariable<?> typeParameter) {
		return isAnnotatedObject( typeParameter ) || isArrayElement( typeParameter );
	}

	public static boolean isAnnotatedObject(TypeVariable<?> typeParameter) {
		return typeParameter == AnnotatedObject.INSTANCE;
	}

	public static boolean isArrayElement(TypeVariable<?> typeParameter) {
		return typeParameter instanceof ArrayElement;
	}

	public static String getTypeParameterName(Class<?> clazz, int typeParameterIndex) {
		if ( typeParameterIndex >= clazz.getTypeParameters().length ) {
			throw LOG.getUnableToFindTypeParameterInClass( clazz, typeParameterIndex );
		}
		return clazz.getTypeParameters()[typeParameterIndex].getName();
	}

	public static Integer getTypeParameterIndex(TypeVariable<?> typeParameter) {
		if ( typeParameter == null || isArrayElement( typeParameter ) ) {
			return null;
		}

		TypeVariable<?>[] typeParameters = typeParameter.getGenericDeclaration().getTypeParameters();
		for ( int i = 0; i < typeParameters.length; i++ ) {
			if ( typeParameter.getName().equals( typeParameters[i].getName() ) ) {
				return i;
			}
		}
		throw LOG.getUnableToFindTypeParameterInClass( (Class<?>) typeParameter.getGenericDeclaration(), typeParameter.getName() );
	}

	public static Type getContainerElementType(Type type, TypeVariable<?> typeParameter) {
		if ( type instanceof ParameterizedType ) {
			Type[] typeArguments = ( (ParameterizedType) type ).getActualTypeArguments();

			return typeArguments[getTypeParameterIndex( typeParameter )];
		}
		else if ( type instanceof GenericArrayType ) {
			return ( (GenericArrayType) type ).getGenericComponentType();
		}
		else {
			return null;
		}
	}

	private static Class<?> getDeclaringClass(TypeVariable<?> typeParameter) {
		return (Class<?>) typeParameter.getGenericDeclaration();
	}
}
