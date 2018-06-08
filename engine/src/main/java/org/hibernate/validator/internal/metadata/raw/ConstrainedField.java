/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.raw;

import java.util.Set;

import org.hibernate.validator.internal.metadata.aggregated.CascadingMetaDataBuilder;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.properties.Field;

/**
 * Represents a field of a Java type and all its associated meta-data relevant
 * in the context of bean validation, for instance its constraints.
 *
 * @author Gunnar Morling
 * @author Guillaume Smet
 * @author Marko Bekhta
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
	 * @param cascadingMetaDataBuilder The cascaded validation metadata for this element and its container elements.
	 */
	public ConstrainedField(ConfigurationSource source,
			Field field,
			Set<MetaConstraint<?>> constraints,
			Set<MetaConstraint<?>> typeArgumentConstraints,
			CascadingMetaDataBuilder cascadingMetaDataBuilder) {

		super( source, ConstrainedElementKind.FIELD, constraints, typeArgumentConstraints, cascadingMetaDataBuilder );

		this.field = field;

	}

	public Field getField() {
		return field;
	}
	@Override
	public String toString() {
		return "ConstrainedField [field=" + field.getName() + "]";
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + this.field.hashCode();
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
		return this.field.equals( other.field );
	}
}
