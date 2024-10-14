/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
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
