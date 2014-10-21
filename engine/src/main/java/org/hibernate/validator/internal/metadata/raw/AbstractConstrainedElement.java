/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.raw;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.hibernate.validator.internal.engine.valuehandling.UnwrapMode;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;

/**
 * Base implementation of with functionality common to all {@link ConstrainedElement} implementations.
 *
 * @author Gunnar Morling
 * @author Hardy Ferentschik
 */
public abstract class AbstractConstrainedElement implements ConstrainedElement {
	private final ConstrainedElementKind kind;
	protected final ConfigurationSource source;
	protected final ConstraintLocation location;
	protected final Set<MetaConstraint<?>> constraints;
	protected final Map<Class<?>, Class<?>> groupConversions;
	protected final boolean isCascading;
	protected final UnwrapMode unwrapMode;

	public AbstractConstrainedElement(ConfigurationSource source,
									  ConstrainedElementKind kind,
									  ConstraintLocation location,
									  Set<MetaConstraint<?>> constraints,
									  Map<Class<?>, Class<?>> groupConversions,
									  boolean isCascading,
									  UnwrapMode unwrapMode) {
		this.kind = kind;
		this.source = source;
		this.location = location;
		this.constraints = constraints != null ? Collections.unmodifiableSet( constraints ) : Collections.<MetaConstraint<?>>emptySet();
		this.groupConversions = Collections.unmodifiableMap( groupConversions );
		this.isCascading = isCascading;
		this.unwrapMode = unwrapMode;
	}

	@Override
	public ConstrainedElementKind getKind() {
		return kind;
	}

	@Override
	public ConstraintLocation getLocation() {
		return location;
	}

	@Override
	public Iterator<MetaConstraint<?>> iterator() {
		return constraints.iterator();
	}

	@Override
	public Set<MetaConstraint<?>> getConstraints() {
		return constraints;
	}

	@Override
	public Map<Class<?>, Class<?>> getGroupConversions() {
		return groupConversions;
	}

	@Override
	public boolean isCascading() {
		return isCascading;
	}

	@Override
	public boolean isConstrained() {
		return isCascading || !constraints.isEmpty();
	}

	@Override
	public UnwrapMode unwrapMode() {
		return unwrapMode;
	}

	@Override
	public String toString() {
		return "AbstractConstrainedElement [kind=" + kind + ", source="
				+ source + ", location=" + location + ", constraints="
				+ constraints + ", groupConversions=" + groupConversions
				+ ", isCascading=" + isCascading + ", unwrapMode="
				+ unwrapMode + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ( ( source == null ) ? 0 : source.hashCode() );
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		if ( obj == null ) {
			return false;
		}
		if ( getClass() != obj.getClass() ) {
			return false;
		}
		AbstractConstrainedElement other = (AbstractConstrainedElement) obj;
		if ( source != other.source ) {
			return false;
		}
		return true;
	}
}
