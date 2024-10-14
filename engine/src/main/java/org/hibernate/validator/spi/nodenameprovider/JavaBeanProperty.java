/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.spi.nodenameprovider;

import org.hibernate.validator.Incubating;

/**
 * Contains metadata for a JavaBean property.
 *
 * @author Damir Alibegovic
 * @since 6.1.0
 */
@Incubating
public interface JavaBeanProperty extends Property {
	/**
	 * Owner class of the property.
	 *
	 * @return {@link Class} owning class of the property
	 */
	Class<?> getDeclaringClass();
}
