/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.raw;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.validator.internal.metadata.cascading.CascadingTypeParameter;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.util.StringHelper;

/**
 * Represents a field of a Java type and all its associated meta-data relevant
 * in the context of bean validation, for instance its constraints.
 *
 * @author Gunnar Morling
 * @author Guillaume Smet
 */
public class ConstrainedField extends AbstractConstrainedElement {

	private final Field field;

	/**
	 * Creates a new field meta data object.
	 *
	 * @param source The source of meta data.
	 * @param field The represented field.
	 * @param constraints The constraints of the represented field, if any.
	 * @param typeArgumentConstraints Type arguments constraints, if any.
	 * @param groupConversions The group conversions of the represented field, if any.
	 * @param cascadingTypeParameters The type parameters marked for cascaded validation, if any.
	 */
	public ConstrainedField(ConfigurationSource source,
							Field field,
							Set<MetaConstraint<?>> constraints,
							Set<MetaConstraint<?>> typeArgumentConstraints,
							Map<Class<?>, Class<?>> groupConversions,
							List<CascadingTypeParameter> cascadingTypeParameters) {

		super( source, ConstrainedElementKind.FIELD, constraints, typeArgumentConstraints, groupConversions, cascadingTypeParameters );

		this.field = field;
	}

	public Field getField() {
		return field;
	}

	@Override
	public String toString() {
		return "ConstrainedField [field=" + StringHelper.toShortString( field ) + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ( ( field == null ) ? 0 : field.hashCode() );
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		if ( !super.equals( obj ) ) {
			return false;
		}
		if ( getClass() != obj.getClass() ) {
			return false;
		}
		ConstrainedField other = (ConstrainedField) obj;
		if ( field == null ) {
			if ( other.field != null ) {
				return false;
			}
		}
		else if ( !field.equals( other.field ) ) {
			return false;
		}
		return true;
	}
}
