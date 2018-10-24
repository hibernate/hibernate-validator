/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.properties.propertyholder;

import java.lang.reflect.Type;

import org.hibernate.validator.internal.properties.Field;
import org.hibernate.validator.internal.properties.Property;
import org.hibernate.validator.internal.properties.PropertyAccessor;

/**
 * @author Marko Bekhta
 */
public class PropertyHolderProperty implements Property, Field {

	private final Class<?> propertyHolderType;
	private final PropertyAccessor propertyAccessor;

	private final String name;
	private final Class<?> type;

	public PropertyHolderProperty(Class<?> propertyHolderType, PropertyAccessor propertyAccessor, String name, Class<?> type) {
		this.propertyHolderType = propertyHolderType;
		this.propertyAccessor = propertyAccessor;
		this.name = name;
		this.type = type;
	}

	@Override
	public String getPropertyName() {
		return getName();
	}

	@Override
	public PropertyAccessor createAccessor() {
		return propertyAccessor;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Class<?> getDeclaringClass() {
		return propertyHolderType;
	}

	@Override
	public Type getTypeForValidatorResolution() {
		return getType();
	}

	@Override
	public Type getType() {
		return type;
	}

	@Override
	public boolean equals(Object o) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		PropertyHolderProperty that = (PropertyHolderProperty) o;

		if ( !propertyHolderType.equals( that.propertyHolderType ) ) {
			return false;
		}
		if ( !name.equals( that.name ) ) {
			return false;
		}
		if ( !type.equals( that.type ) ) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = propertyHolderType.hashCode();
		result = 31 * result + name.hashCode();
		result = 31 * result + type.hashCode();
		return result;
	}
}
