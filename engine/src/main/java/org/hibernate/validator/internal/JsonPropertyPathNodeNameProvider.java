/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Objects;

import org.hibernate.validator.spi.damir.PropertyPathNodeNameProvider;

public class JsonPropertyPathNodeNameProvider implements PropertyPathNodeNameProvider, Serializable {
	@Override
	public String getName(String propertyName, Object object) {
		if ( Objects.isNull(object)) {
			return propertyName;
		}

		for ( Field field : object.getClass().getDeclaredFields() ) {
			Class type = field.getType();
			String name = field.getName();
			Annotation[] annotations = field.getDeclaredAnnotations();

			// check if it's jsonProperty
			// return value
		}
		return propertyName;
	}
}
