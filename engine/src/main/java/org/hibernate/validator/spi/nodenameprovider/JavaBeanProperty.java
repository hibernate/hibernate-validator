/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
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
