/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.cfg.propertyholder;

import org.hibernate.validator.cfg.propertyholder.GroupConversionTargetContext;

/**
 * Adds internal method to {@link GroupConversionTargetContext} that allows adding group conversions.
 *
 * @author Marko Bekhta
 */
interface GroupConversionTargetContextHelper<C> {
	/**
	 * Adds a group conversion for this element.
	 *
	 * @param from the source group of the conversion
	 * @param to the target group of the conversion
	 */
	void addGroupConversion(Class<?> from, Class<?> to);
}
