/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.raw;

import java.util.Collections;
import java.util.Set;

import org.hibernate.validator.internal.engine.valuehandling.UnwrapMode;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;

/**
 * Represents a Java type and all its associated meta-data relevant in the
 * context of bean validation, for instance its constraints. Only class level
 * meta-data is represented by this type, but not meta-data for any members.
 *
 * @author Gunnar Morling
 */
public class ConstrainedType extends AbstractConstrainedElement {

	/**
	 * Creates a new type meta data object.
	 *
	 * @param source The source of meta data.
	 * @param location The location of the represented type.
	 * @param constraints The constraints of the represented type, if any.
	 */
	public ConstrainedType(ConfigurationSource source, ConstraintLocation location, Set<MetaConstraint<?>> constraints) {

		super(
				source,
				ConstrainedElementKind.TYPE,
				location,
				constraints,
				Collections.<Class<?>, Class<?>>emptyMap(),
				false,
				UnwrapMode.AUTOMATIC
		);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ( ( getLocation().getDeclaringClass() == null ) ? 0 : getLocation().getDeclaringClass().hashCode() );
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
		ConstrainedType other = (ConstrainedType) obj;
		if ( getLocation().getDeclaringClass() == null ) {
			if ( other.getLocation().getDeclaringClass() != null ) {
				return false;
			}
		}
		else if ( !getLocation().getDeclaringClass().equals( other.getLocation().getDeclaringClass() ) ) {
			return false;
		}
		return true;
	}
}
