/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.raw;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import org.hibernate.validator.internal.metadata.aggregated.CascadingMetaDataBuilder;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.stereotypes.Immutable;

/**
 * Base implementation of with functionality common to all {@link ConstrainedElement} implementations.
 *
 * @author Gunnar Morling
 * @author Hardy Ferentschik
 */
public abstract class AbstractConstrainedElement implements ConstrainedElement {
	private final ConstrainedElementKind kind;
	protected final ConfigurationSource source;
	@Immutable
	protected final Set<MetaConstraint<?>> constraints;
	protected final CascadingMetaDataBuilder cascadingMetaDataBuilder;
	@Immutable
	protected final Set<MetaConstraint<?>> typeArgumentConstraints;

	public AbstractConstrainedElement(ConfigurationSource source,
									  ConstrainedElementKind kind,
									  Set<MetaConstraint<?>> constraints,
									  Set<MetaConstraint<?>> typeArgumentConstraints,
									  CascadingMetaDataBuilder cascadingMetaDataBuilder) {
		this.kind = kind;
		this.source = source;
		this.constraints = constraints != null ? CollectionHelper.toImmutableSet( constraints ) : Collections.<MetaConstraint<?>>emptySet();
		this.typeArgumentConstraints = typeArgumentConstraints != null ? CollectionHelper.toImmutableSet( typeArgumentConstraints ) : Collections.<MetaConstraint<?>>emptySet();
		this.cascadingMetaDataBuilder = cascadingMetaDataBuilder;
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
	public CascadingMetaDataBuilder getCascadingMetaDataBuilder() {
		return cascadingMetaDataBuilder;
	}

	@Override
	public boolean isConstrained() {
		return cascadingMetaDataBuilder.isMarkedForCascadingOnAnnotatedObjectOrContainerElements()
				|| cascadingMetaDataBuilder.hasGroupConversionsOnAnnotatedObjectOrContainerElements()
				|| !constraints.isEmpty()
				|| !typeArgumentConstraints.isEmpty();
	}

	@Override
	public ConfigurationSource getSource() {
		return source;
	}

	@Override
	public String toString() {
		return "AbstractConstrainedElement [kind=" + kind + ", source="
				+ source + ", constraints="
				+ constraints + ", cascadingMetaDataBuilder="
				+ cascadingMetaDataBuilder + "]";
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
