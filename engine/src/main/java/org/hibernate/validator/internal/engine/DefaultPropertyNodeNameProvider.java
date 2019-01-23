/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine;

import java.io.Serializable;

import org.hibernate.validator.spi.nodenameprovider.Property;
import org.hibernate.validator.spi.nodenameprovider.PropertyNodeNameProvider;

public class DefaultPropertyNodeNameProvider implements PropertyNodeNameProvider, Serializable {
	@Override
	public String getName(String propertyName, Property property) {
		return propertyName;
	}
}
