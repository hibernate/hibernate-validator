/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.raw;

import java.lang.reflect.TypeVariable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.validator.internal.engine.valuehandling.UnwrapMode;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;

/**
 * Base implementation of with functionality common to all {@link ConstrainedElement} implementations.
 *
 * @author Gunnar Morling
 * @author Hardy Ferentschik
 */
public abstract class AbstractConstrainedElement implements ConstrainedElement {
	private final ConstrainedElementKind kind;
	protected final ConfigurationSource source;
	protected final Set<MetaConstraint<?>> constraints;
	protected final Map<Class<?>, Class<?>> groupConversions;
	protected final List<TypeVariable<?>> cascadingTypeParameters;
	protected final Set<MetaConstraint<?>> typeArgumentConstraints;
	protected final UnwrapMode unwrapMode;

	public AbstractConstrainedElement(ConfigurationSource source,
									  ConstrainedElementKind kind,
									  Set<MetaConstraint<?>> constraints,
									  Set<MetaConstraint<?>> typeArgumentConstraints,
									  Map<Class<?>, Class<?>> groupConversions,
									  List<TypeVariable<?>> cascadingTypeParameters,
									  UnwrapMode unwrapMode) {
		this.kind = kind;
		this.source = source;
		this.constraints = constraints != null ? Collections.unmodifiableSet( constraints ) : Collections.<MetaConstraint<?>>emptySet();
		this.typeArgumentConstraints = typeArgumentConstraints != null ? Collections.unmodifiableSet( typeArgumentConstraints ) : Collections.<MetaConstraint<?>>emptySet();
		this.groupConversions = Collections.unmodifiableMap( groupConversions );
		this.cascadingTypeParameters = cascadingTypeParameters;

		this.unwrapMode = unwrapMode;
	}

	@Override
	public ConstrainedElementKind getKind() {
		return kind;
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
	public Set<MetaConstraint<?>> getTypeArgumentConstraints() {
		return typeArgumentConstraints;
	}

	@Override
	public Map<Class<?>, Class<?>> getGroupConversions() {
		return groupConversions;
	}

	@Override
	public boolean isCascading() {
		return !cascadingTypeParameters.isEmpty();
	}

	@Override
	public List<TypeVariable<?>> getCascadingTypeParameters() {
		return cascadingTypeParameters;
	}

	@Override
	public boolean isConstrained() {
		return isCascading() || !constraints.isEmpty() || !typeArgumentConstraints.isEmpty();
	}

	@Override
	public UnwrapMode unwrapMode() {
		return unwrapMode;
	}

	@Override
	public ConfigurationSource getSource() {
		return source;
	}

	@Override
	public String toString() {
		return "AbstractConstrainedElement [kind=" + kind + ", source="
				+ source + ", constraints="
				+ constraints + ", groupConversions=" + groupConversions
				+ ", cascadingTypeParameters=" + cascadingTypeParameters + ", unwrapMode="
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
