/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.spi.propertyholder;

import java.lang.reflect.Type;

import org.hibernate.validator.internal.properties.PropertyAccessor;

/**
 * This interface expose the functionality of creating the property accessor based on
 * a string representation of a property.
 *
 * @author Marko Bekhta
 */
public interface PropertyAccessorCreator<T> {

	/**
	 * @return the type of the property holder this creator can be applied to.
	 */
	Class<T> getPropertyHolderType();

	/**
	 * Creates property accessor for a given name and type.
	 *
	 * @param propertyName property name
	 * @param propertyType property type
	 *
	 * @return created property
	 */
	PropertyAccessor create(String propertyName, Type propertyType);
}
