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
import org.hibernate.validator.internal.properties.Property;

/**
 * Represents a property of a Java type (either field or getter) and all its associated meta-data relevant
 * in the context of bean validation, for instance its constraints.
 *
 * @author Marko Bekhta
 */
public class ConstrainedProperty extends AbstractConstrainedElement {

	private final Property property;

	/**
	 * Creates a new field meta data object.
	 *
	 * @param source The source of meta data.
	 * @param property The represented property.
	 * @param constraints The constraints of the represented field, if any.
	 * @param typeArgumentConstraints Type arguments constraints, if any.
	 * @param cascadingMetaDataBuilder The cascaded validation metadata for this element and its container elements.
	 */
	private ConstrainedProperty(ConfigurationSource source,
			Property property,
			Set<MetaConstraint<?>> constraints,
			Set<MetaConstraint<?>> typeArgumentConstraints,
			CascadingMetaDataBuilder cascadingMetaDataBuilder) {

		super( source, ConstrainedElementKind.PROPERTY, constraints, typeArgumentConstraints, cascadingMetaDataBuilder );

		this.property = property;
	}

	public static ConstrainedProperty forField(ConfigurationSource source,
			Property property,
			Set<MetaConstraint<?>> constraints,
			Set<MetaConstraint<?>> typeArgumentConstraints,
			CascadingMetaDataBuilder cascadingMetaDataBuilder) {
		return new ConstrainedProperty( source, property, constraints, typeArgumentConstraints, cascadingMetaDataBuilder );
	}

	public Property getProperty() {
		return property;
	}

	@Override
	public String toString() {
		return "ConstrainedProperty [property=" + property.getName() + "]";
	}

	@Override public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + this.property.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object o) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || this.getClass() != o.getClass() ) {
			return false;
		}
		if ( !super.equals( o ) ) {
			return false;
		}

		ConstrainedProperty that = (ConstrainedProperty) o;

		return this.property.equals( that.property );
	}
}
