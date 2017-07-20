/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.raw;

import java.util.Collections;
import java.util.Set;

import org.hibernate.validator.internal.metadata.aggregated.CascadingMetaDataBuilder;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;

/**
 * Represents a Java type and all its associated meta-data relevant in the
 * context of bean validation, for instance its constraints. Only class level
 * meta-data is represented by this type, but not meta-data for any members.
 *
 * @author Gunnar Morling
 */
public class ConstrainedType extends AbstractConstrainedElement {

	private final Class<?> beanClass;

	/**
	 * Creates a new type meta data object.
	 *
	 * @param source The source of meta data.
	 * @param beanClass The represented type.
	 * @param constraints The constraints of the represented type, if any.
	 */
	public ConstrainedType(ConfigurationSource source, Class<?> beanClass, Set<MetaConstraint<?>> constraints) {

		super(
				source,
				ConstrainedElementKind.TYPE,
				constraints,
				Collections.emptySet(),
				CascadingMetaDataBuilder.nonCascading()
		);

		this.beanClass = beanClass;
	}

	public Class<?> getBeanClass() {
		return beanClass;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ( ( beanClass == null ) ? 0 : beanClass.hashCode() );
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
		if ( beanClass == null ) {
			if ( other.beanClass != null ) {
				return false;
			}
		}
		else if ( !beanClass.equals( other.beanClass ) ) {
			return false;
		}
		return true;
	}
}
