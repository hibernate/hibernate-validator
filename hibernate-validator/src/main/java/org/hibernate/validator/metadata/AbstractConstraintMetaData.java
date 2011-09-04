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
 */
public class AbstractConstraintMetaData implements ConstraintMetaData {

	protected final Set<MetaConstraint<?>> constraints;

	private final ConstraintMetaDataKind constrainedMetaDataKind;

	private final boolean isCascading;

	/**
	 * @param constraints
	 * @param constrainedMetaDataKind
	 * @param isCascading
	 */
	public AbstractConstraintMetaData(Set<MetaConstraint<?>> constraints, ConstraintMetaDataKind constrainedMetaDataKind, boolean isCascading) {

		this.constraints = Collections.unmodifiableSet( constraints );
		this.constrainedMetaDataKind = constrainedMetaDataKind;
		this.isCascading = isCascading;
	}

	public Iterator<MetaConstraint<?>> iterator() {
		return constraints.iterator();
	}

	public ConstraintMetaDataKind getConstrainedMetaDataKind() {
		return constrainedMetaDataKind;
	}

	public boolean isCascading() {
		return isCascading;
	}

	@Override
	public String toString() {
		return "AbstractConstraintMetaData [constraints="
				+ constraints + ", constrainedMetaDataKind="
				+ constrainedMetaDataKind + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ( ( constrainedMetaDataKind == null ) ? 0
				: constrainedMetaDataKind.hashCode() );
		result = prime * result
				+ ( ( constraints == null ) ? 0 : constraints.hashCode() );
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
		AbstractConstraintMetaData other = (AbstractConstraintMetaData) obj;
		if ( constrainedMetaDataKind != other.constrainedMetaDataKind ) {
			return false;
		}
		if ( constraints == null ) {
			if ( other.constraints != null ) {
				return false;
			}
		}
		else if ( !constraints.equals( other.constraints ) ) {
			return false;
		}
		return true;
	}

}
