/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.util;

import java.io.Serializable;

import org.hibernate.validator.spi.damir.PropertyPathNodeNameProvider;

public class PropertyPathNodeNameProviderWrapper implements Serializable {
	private final PropertyPathNodeNameProvider delegate;

	public PropertyPathNodeNameProviderWrapper(PropertyPathNodeNameProvider delegate) {
		this.delegate = delegate;
	}

	public String getName(String propertyName, Object object) {
		return delegate.getName( propertyName, object );
	}
}
