/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.spi.properties;

import org.hibernate.validator.Incubating;

/**
 * Descriptor for a method of a Java class.
 *
 * @author Marko Bekhta
 * @since 6.1.0
 */
@Incubating
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
	Class<?>[] getParameterTypes();
}
