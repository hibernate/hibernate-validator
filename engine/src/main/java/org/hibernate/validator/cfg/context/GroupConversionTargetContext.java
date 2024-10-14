/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
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
