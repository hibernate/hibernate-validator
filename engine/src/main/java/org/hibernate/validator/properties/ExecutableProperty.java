/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.properties;

import java.lang.reflect.Type;

/**
 * A class to be used for filtering properties.
 *
 * @author Marko Bekhta
 */
public interface ExecutableProperty {

	/**
	 * @return a return type of the method.
	 */
	Type getType();

	/**
	 * @return full name of the method.
	 */
	String getName();

	Type[] getParameterTypes();
}
