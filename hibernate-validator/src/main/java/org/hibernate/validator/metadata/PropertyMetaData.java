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

import java.lang.reflect.Member;
import java.util.Iterator;
import java.util.Set;

import org.hibernate.validator.metadata.location.BeanConstraintLocation;
import org.hibernate.validator.util.ReflectionHelper;

/**
 * @author Gunnar Morling
 */
public class PropertyMetaData implements Iterable<BeanMetaConstraint<?>> {

	private final Set<BeanMetaConstraint<?>> constraints;

	private final BeanConstraintLocation location;

	private final boolean isCascading;

	/**
	 * @param constraints
	 * @param location
	 * @param isCascading
	 */
	public PropertyMetaData(Set<BeanMetaConstraint<?>> constraints,
							BeanConstraintLocation location, boolean isCascading) {

		this.constraints = constraints;
		this.location = location;
		this.isCascading = isCascading;

		Member member = location.getMember();
		if ( member != null && isConstrained() ) {
			ReflectionHelper.setAccessibility( member );
		}
	}

	public BeanConstraintLocation getLocation() {
		return location;
	}

	public Iterator<BeanMetaConstraint<?>> iterator() {
		return constraints.iterator();
	}

	public Set<BeanMetaConstraint<?>> getConstraints() {
		return constraints;
	}

	public boolean isCascading() {
		return isCascading;
	}

	private boolean isConstrained() {
		return isCascading || !constraints.isEmpty();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ( ( constraints == null ) ? 0 : constraints.hashCode() );
		result = prime * result + ( isCascading ? 1231 : 1237 );
		result = prime * result
				+ ( ( location == null ) ? 0 : location.hashCode() );
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
		PropertyMetaData other = (PropertyMetaData) obj;
		if ( constraints == null ) {
			if ( other.constraints != null ) {
				return false;
			}
		}
		else if ( !constraints.equals( other.constraints ) ) {
			return false;
		}
		if ( isCascading != other.isCascading ) {
			return false;
		}
		if ( location == null ) {
			if ( other.location != null ) {
				return false;
			}
		}
		else if ( !location.equals( other.location ) ) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "PropertyMetaData [constraints=" + constraints + ", location="
				+ location + ", isCascading=" + isCascading + "]";
	}

}
