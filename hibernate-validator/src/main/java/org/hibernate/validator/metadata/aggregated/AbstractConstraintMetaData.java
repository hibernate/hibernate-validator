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
package org.hibernate.validator.metadata.aggregated;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import org.hibernate.validator.metadata.core.MetaConstraint;
import org.hibernate.validator.metadata.descriptor.ConstraintDescriptorImpl;

import static org.hibernate.validator.util.CollectionHelper.newHashSet;

/**
 * Base implementation for {@link ConstraintMetaData} with attributes common
 * to all type of meta data.
 *
 * @author Gunnar Morling
 */
public abstract class AbstractConstraintMetaData implements ConstraintMetaData {

	private final String name;

	private final Class<?> type;

	private final ConstraintMetaDataKind constrainedMetaDataKind;

	private final Set<MetaConstraint<?>> constraints;

	private final boolean isCascading;

	private final boolean isConstrained;

	/**
	 * @param constraints
	 * @param constrainedMetaDataKind
	 * @param isCascading
	 */
	public AbstractConstraintMetaData(String name, Class<?> type, Set<MetaConstraint<?>> constraints, ConstraintMetaDataKind constrainedMetaDataKind, boolean isCascading, boolean isConstrained) {
		this.name = name;
		this.type = type;
		this.constraints = Collections.unmodifiableSet( constraints );
		this.constrainedMetaDataKind = constrainedMetaDataKind;
		this.isCascading = isCascading;
		this.isConstrained = isConstrained;
	}

	public String getName() {
		return name;
	}

	public Class<?> getType() {
		return type;
	}

	public Iterator<MetaConstraint<?>> iterator() {
		return constraints.iterator();
	}

	public Set<MetaConstraint<?>> getConstraints() {
		return constraints;
	}

	public ConstraintMetaDataKind getKind() {
		return constrainedMetaDataKind;
	}

	public boolean isCascading() {
		return isCascading;
	}

	public boolean isConstrained() {
		return isConstrained;
	}

	protected Set<ConstraintDescriptorImpl<?>> asDescriptors(Set<MetaConstraint<?>> constraints) {
		Set<ConstraintDescriptorImpl<?>> theValue = newHashSet();

		for ( MetaConstraint<?> oneConstraint : constraints ) {
			theValue.add( oneConstraint.getDescriptor() );
		}

		return theValue;
	}

	@Override
	public String toString() {
		return "AbstractConstraintMetaData [name=" + name
				+ ", constrainedMetaDataKind=" + constrainedMetaDataKind
				+ ", constraints=" + constraints + ", isCascading="
				+ isCascading + ", isConstrained=" + isConstrained + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ( ( name == null ) ? 0 : name.hashCode() );
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
		if ( name == null ) {
			if ( other.name != null ) {
				return false;
			}
		}
		else if ( !name.equals( other.name ) ) {
			return false;
		}
		return true;
	}

}
