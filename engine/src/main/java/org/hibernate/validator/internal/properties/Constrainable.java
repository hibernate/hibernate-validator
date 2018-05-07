/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.properties;

import java.lang.reflect.Type;

/**
 * @author Marko Bekhta
 */
public interface Constrainable {

	String getName();

	Class<?> getDeclaringClass();

	Type getTypeForValidatorResolution();

	Type getType();

	default <T> T as(Class<T> clazz) {
		return ( (T) this );
	}
}
