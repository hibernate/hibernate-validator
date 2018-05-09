/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.spi.properties;

import java.lang.reflect.Type;

/**
 * A class that describes JavaBeans method. Is used for determining if a given
 * JavaBeans method is a property or not.
 *
 * @author Marko Bekhta
 * @since 6.1.0
 */
public interface ConstrainableExecutable {

	/**
	 * @return the return type for the method this object represents
	 */
	Class<?> getReturnType();

	/**
	 * @return the name of the method represented by this {@code ConstrainableExecutable} object
	 */
	String getName();

	/**
	 * @return the parameter types for the executable this object represents
	 */
	Type[] getParameterTypes();
}
