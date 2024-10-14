/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.spi.nodenameprovider;

import org.hibernate.validator.Incubating;

/**
 * This interface is used to resolve the name of a property node when creating the property path.
 *
 * @author Damir Alibegovic
 * @since 6.1.0
 */
@Incubating
public interface PropertyNodeNameProvider {
	/**
	 * Returns the resolved name of a property.
	 * <p>
	 * Depending on the subtype of the {@link Property},
	 * a different strategy for name resolution could be applied, defaulting to {@link Property#getName()}. For example:
	 *
	 * <pre>
	 * if (property instanceof {@link JavaBeanProperty}) {
	 *     // for instance, generate a property name based on the annotations of the property
	 * } else {
	 *     return property.getName();
	 * }
	 * </pre>
	 *
	 * @param property who's name needs to be resolved
	 *
	 * @return String representing the resolved name
	 */
	String getName(Property property);
}
