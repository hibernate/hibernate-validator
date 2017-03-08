/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.util;

import java.lang.reflect.TypeVariable;

import org.hibernate.validator.internal.engine.cascading.AnnotatedObject;
import org.hibernate.validator.internal.engine.cascading.ArrayElement;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * Provides some utility methods for TypeVariables.
 *
 * @author Guillaume Smet
 */
public class TypeVariables {

	private static final Log LOG = LoggerFactory.make();

	private TypeVariables() {
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

	public static int getTypeParameterIndex(TypeVariable<?> typeParameter) {
		TypeVariable<?>[] typeParameters = typeParameter.getGenericDeclaration().getTypeParameters();
		for ( int i = 0; i < typeParameters.length; i++ ) {
			if ( typeParameter.getName().equals( typeParameters[i].getName() ) ) {
				return i;
			}
		}
		throw LOG.getUnableToFindTypeParameterInClass( (Class<?>) typeParameter.getGenericDeclaration(), typeParameter.getName() );
	}

	public static Class<?> getDeclaringClass(TypeVariable<?> typeParameter) {
		return (Class<?>) typeParameter.getGenericDeclaration();
	}
}
