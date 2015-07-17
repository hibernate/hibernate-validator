/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.cfg.context;

/**
 * Creational context which allows to set the target group of a group conversion configured via
 * {@link Cascadable#convertGroup(Class)}.
 *
 * @author Gunnar Morling
 */
public interface GroupConversionTargetContext<C> {

	/**
	 * Sets the target group of the conversion to be configured.
	 *
	 * @param to the target group of the conversion
	 *
	 * @return The current creational context following the method chaining pattern.
	 */
	C to(Class<?> to);
}
