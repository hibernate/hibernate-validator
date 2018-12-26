/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.spi.nodenameprovider;


import org.hibernate.validator.Incubating;

/**
 * Base interface for property metadata.
 *
 * @author Damir Alibegovic
 * @since 6.1.0
 */
@Incubating
public interface Property {
	/**
	 * Returns the property name.
	 *
	 * @return {@link String} representing the property name
	 */
	String getName();
}
