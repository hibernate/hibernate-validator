/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.cfg.context;

/**
 * Facet of a constraint mapping creational context which allows to mark the underlying element as to be unwrapped prior
 * to validation.
 *
 * @author Gunnar Morling
 * @author Hardy Feretnschik
 *
 * @hv.experimental This API is considered experimental and may change in future revisions
 */
public interface Unwrapable<U extends Unwrapable<U>> {

	/**
	 * Configures explicitly the unwrapping mode of the current element (property, parameter etc.).
	 *
	 * @param unwrap Explicitly set whether to unwrap or not.
	 * @return The current creational context following the method chaining pattern.
	 * @see org.hibernate.validator.valuehandling.UnwrapValidatedValue
	 */
	U unwrapValidatedValue(boolean unwrap);
}
