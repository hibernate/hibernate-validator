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

/**
 * Provides some utility methods for TypeVariables.
 *
 * @author Guillaume Smet
 */
public class TypeVariables {

	private TypeVariables() {
	}

	public static boolean isInternal(TypeVariable<?> typeParameter) {
		return typeParameter == AnnotatedObject.INSTANCE || typeParameter == ArrayElement.INSTANCE;
	}

}
