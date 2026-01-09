/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.engine;

import java.io.Serializable;

import org.hibernate.validator.spi.nodenameprovider.Property;
import org.hibernate.validator.spi.nodenameprovider.PropertyNodeNameProvider;
import org.hibernate.validator.spi.nodenameprovider.PropertyNodeNameProviderContext;

/**
 * A default {@link PropertyNodeNameProvider} implementation which returns the property name.
 *
 * @author Damir Alibegovic
 */
public class DefaultPropertyNodeNameProvider implements PropertyNodeNameProvider, Serializable {
	@Override
	public String getName(Property property, PropertyNodeNameProviderContext context) {
		return property.getName();
	}
}
