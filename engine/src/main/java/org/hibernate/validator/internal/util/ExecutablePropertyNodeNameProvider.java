/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.util;

import java.io.Serializable;

import org.hibernate.validator.spi.nodenameprovider.PropertyNodeNameProvider;

public class ExecutablePropertyNodeNameProvider implements Serializable {
	private final PropertyNodeNameProvider propertyNodeNameProvider;
	private final Object bean;

	public ExecutablePropertyNodeNameProvider(PropertyNodeNameProvider propertyNodeNameProvider, Object bean) {
		this.propertyNodeNameProvider = propertyNodeNameProvider;
		this.bean = bean;
	}

	public String getName(String propertyName) {
		return propertyNodeNameProvider.getName( propertyName, bean );
	}
}
