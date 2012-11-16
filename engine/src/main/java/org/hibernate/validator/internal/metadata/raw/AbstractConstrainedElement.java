/*
* JBoss, Home of Professional Open Source
* Copyright 2011, Red Hat, Inc. and/or its affiliates, and individual contributors
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.hibernate.validator.internal.metadata.raw;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

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
	private final ConfigurationSource source;
	private final ConstraintLocation location;
	private final Set<MetaConstraint<?>> constraints;
	private final Map<Class<?>, Class<?>> groupConversions;
	private final boolean isCascading;

	public AbstractConstrainedElement(ConfigurationSource source, ConstrainedElementKind kind, ConstraintLocation location, Set<MetaConstraint<?>> constraints, Map<Class<?>, Class<?>> groupConversions, boolean isCascading) {

		this.kind = kind;
		this.source = source;
		this.location = location;
		this.constraints = constraints != null ? Collections.unmodifiableSet( constraints ) : Collections.<MetaConstraint<?>>emptySet();
		this.groupConversions = Collections.unmodifiableMap( groupConversions );
		this.isCascading = isCascading;
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
	public String toString() {
		return "AbstractConstrainedElement [kind=" + kind + ", source="
				+ source + ", location=" + location + ", constraints="
				+ constraints + ", isCascading=" + isCascading + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ( ( location == null ) ? 0 : location.hashCode() );
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
		if ( location == null ) {
			if ( other.location != null ) {
				return false;
			}
		}
		else if ( !location.equals( other.location ) ) {
			return false;
		}
		if ( source == null ) {
			if ( other.source != null ) {
				return false;
			}
		}
		else if ( !source.equals( other.source ) ) {
			return false;
		}
		return true;
	}
}
