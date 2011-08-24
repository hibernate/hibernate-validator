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
package org.hibernate.validator.metadata;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

/**
 * @author Gunnar Morling
 *
 */
public class AbstractConstraintMetaData implements ConstraintMetaData {

	protected final Set<MetaConstraint<?>> constraints;
	
	private final ConstrainedElementKind constrainedElementKind;
	
	/**
	 * @param constraints
	 * @param constrainedElementKind
	 */
	public AbstractConstraintMetaData( Set<MetaConstraint<?>> constraints, ConstrainedElementKind constrainedElementKind) {

		this.constraints = Collections.unmodifiableSet( constraints );
		this.constrainedElementKind = constrainedElementKind;
	}

	public Iterator<MetaConstraint<?>> iterator() {
		return constraints.iterator();
	}

	public ConstrainedElementKind getConstrainedElementKind() {
		return constrainedElementKind;
	}

	@Override
	public String toString() {
		return "AbstractAggregatedConstrainedElement [constraints="
				+ constraints + ", constrainedElementKind="
				+ constrainedElementKind + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((constrainedElementKind == null) ? 0
						: constrainedElementKind.hashCode());
		result = prime * result
				+ ((constraints == null) ? 0 : constraints.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractConstraintMetaData other = (AbstractConstraintMetaData) obj;
		if (constrainedElementKind != other.constrainedElementKind)
			return false;
		if (constraints == null) {
			if (other.constraints != null)
				return false;
		} else if (!constraints.equals(other.constraints))
			return false;
		return true;
	}
	
}
