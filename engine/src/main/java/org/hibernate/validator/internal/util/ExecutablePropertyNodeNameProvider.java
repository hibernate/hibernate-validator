/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.util;

import org.hibernate.validator.spi.nodenameprovider.PropertyNodeNameProvider;

import java.io.Serializable;

public class ExecutablePropertyNodeNameProvider implements Serializable {
	private final PropertyNodeNameProvider delegate;

	public ExecutablePropertyNodeNameProvider(PropertyNodeNameProvider delegate) {
		this.delegate = delegate;
	}

	public PropertyNodeNameProvider getDelegate() {
		return this.delegate;
	}

	public String getName(String propertyName, Object object) {
		return delegate.getName( propertyName, object );
	}
}
