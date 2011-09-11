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
public class AbstractConstraintMetaData implements ConstraintMetaData {

	private final ConstraintMetaDataKind constrainedMetaDataKind;

	private final Set<MetaConstraint<?>> constraints;

	private final boolean isCascading;

	private final boolean isConstrained;

	/**
	 * @param constraints
	 * @param constrainedMetaDataKind
	 * @param isCascading
	 */
	public AbstractConstraintMetaData(Set<MetaConstraint<?>> constraints, ConstraintMetaDataKind constrainedMetaDataKind, boolean isCascading, boolean isConstrained) {

		this.constraints = Collections.unmodifiableSet( constraints );
		this.constrainedMetaDataKind = constrainedMetaDataKind;
		this.isCascading = isCascading;
		this.isConstrained = isConstrained;
	}

	public Iterator<MetaConstraint<?>> iterator() {
		return constraints.iterator();
	}

	public Set<MetaConstraint<?>> getConstraints() {
		return constraints;
	}

	public ConstraintMetaDataKind getConstrainedMetaDataKind() {
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
		return "AbstractConstraintMetaData [constraints="
				+ constraints + ", constrainedMetaDataKind="
				+ constrainedMetaDataKind + "]";
	}

}
