/*
* JBoss, Home of Professional Open Source
* Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.hibernate.validator.metadata.location.MethodConstraintLocation;
import org.hibernate.validator.util.ReflectionHelper;

import static org.hibernate.validator.util.CollectionHelper.newArrayList;
import static org.hibernate.validator.util.CollectionHelper.newHashSet;

/**
 * Represents a method of a Java type and all its associated meta-data relevant
 * in the context of bean validation, for instance the constraints at it's
 * parameters or return value.
 *
 * @author Gunnar Morling
 */
public class ConstrainedMethod implements ConstrainedElement {

	private final MethodConstraintLocation location;

	/**
	 * Constrained-related meta data for this method's parameters.
	 */
	private final List<ConstrainedParameter> parameterMetaData;

	private final Set<MetaConstraint<?>> returnValueConstraints;

	private final boolean isCascading;

	private final boolean hasParameterConstraints;

	public ConstrainedMethod(
			Method method,
			Set<MetaConstraint<?>> returnValueConstraints,
			boolean isCascading) {

		this(
				method,
				Collections.<ConstrainedParameter>emptyList(),
				returnValueConstraints,
				isCascading
		);
	}

	/**
	 * Creates a new method meta data object.
	 *
	 * @param method The method to represent.
	 * @param parameterMetaData A list with parameter meta data. The length must correspond
	 * with the number of parameters of the represented method. So
	 * this list may be empty returned (in case of a parameterless
	 * method), but never <code>null</code>.
	 * @param returnValueConstraints The return value constraints of the represented method, if
	 * any.
	 * @param isCascading Whether a cascaded validation of the represented method's
	 * return value shall be performed or not.
	 */
	public ConstrainedMethod(
			Method method,
			List<ConstrainedParameter> parameterMetaData,
			Set<MetaConstraint<?>> returnValueConstraints,
			boolean isCascading) {

		if ( parameterMetaData.size() != method.getParameterTypes().length ) {
			throw new IllegalArgumentException(
					String.format(
							"Method %s has %s parameters, but the passed list of parameter meta data has a size of %s.",
							method, method.getParameterTypes().length, parameterMetaData.size()
					)
			);
		}

		this.location = new MethodConstraintLocation( method );
		this.parameterMetaData = Collections.unmodifiableList( parameterMetaData );
		this.returnValueConstraints = Collections.unmodifiableSet( returnValueConstraints );
		this.isCascading = isCascading;
		this.hasParameterConstraints = hasParameterConstraints( parameterMetaData );

		Member member = location.getMember();
		if ( isConstrained() ) {
			ReflectionHelper.setAccessibility( member );
		}
	}

	private boolean hasParameterConstraints(List<ConstrainedParameter> parameterMetaData) {

		for ( ConstrainedParameter oneParameter : parameterMetaData ) {
			if ( oneParameter.isConstrained() ) {
				return true;
			}
		}

		return false;
	}

	public ConstrainedElementKind getConstrainedElementKind() {
		return ConstrainedElementKind.METHOD;
	}

	public MethodConstraintLocation getLocation() {
		return location;
	}

	/**
	 * Constraint meta data for the specified parameter.
	 *
	 * @param parameterIndex The index in this method's parameter array of the parameter of
	 * interest.
	 *
	 * @return Meta data for the specified parameter. Will never be
	 *         <code>null</code>.
	 *
	 * @throws IllegalArgumentException In case this method doesn't have a parameter with the
	 * specified index.
	 */
	public ConstrainedParameter getParameterMetaData(int parameterIndex) {

		if ( parameterIndex < 0 || parameterIndex > parameterMetaData.size() - 1 ) {
			throw new IllegalArgumentException( "Method " + location.getMethod() + " doesn't have a parameter with index " + parameterIndex );
		}

		return parameterMetaData.get( parameterIndex );
	}

	/**
	 * Returns meta data for all parameters of the represented method.
	 *
	 * @return A list with parameter meta data. The length corresponds to the
	 *         number of parameters of the method represented by this meta data
	 *         object, so an empty list may be returned (in case of a
	 *         parameterless method), but never <code>null</code>.
	 */
	public List<ConstrainedParameter> getAllParameterMetaData() {
		return parameterMetaData;
	}

	/**
	 * An iterator with the return value constraints of the represented method.
	 */
	public Iterator<MetaConstraint<?>> iterator() {
		return returnValueConstraints.iterator();
	}

	/**
	 * Whether cascading validation for the return value of the represented
	 * method shall be performed or not.
	 *
	 * @return <code>True</code>, if cascading validation for the represented
	 *         method's return value shall be performed, <code>false</code>
	 *         otherwise.
	 */
	public boolean isCascading() {
		return isCascading;
	}

	/**
	 * Whether the represented method is constrained or not. This is the case if
	 * it has at least one constrained parameter, at least one parameter marked
	 * for cascaded validation, at least one return value constraint or if the
	 * return value is marked for cascaded validation.
	 *
	 * @return <code>True</code>, if this method is constrained by any means,
	 *         <code>false</code> otherwise.
	 */
	public boolean isConstrained() {

		return isCascading || !returnValueConstraints.isEmpty() || hasParameterConstraints;
	}

	/**
	 * Whether this method has at least one cascaded parameter or at least one
	 * parameter with constraints.
	 *
	 * @return <code>True</code>, if this method has at least one cascading or
	 *         constrained parameter, <code>false</code> otherwise.
	 */
	public boolean hasParameterConstraints() {
		return hasParameterConstraints;
	}

	/**
	 * Whether the represented method is a JavaBeans getter method or not.
	 *
	 * @return <code>True</code>, if this method is a getter method,
	 *         <code>false</code> otherwise.
	 */
	public boolean isGetterMethod() {
		return ReflectionHelper.isGetterMethod( location.getMethod() );
	}

	public ConstrainedMethod merge(ConstrainedMethod otherMetaData) {

		boolean isCascading = isCascading() || otherMetaData.isCascading();

		// 1 - aggregate return value constraints
		Set<MetaConstraint<?>> mergedReturnValueConstraints = newHashSet(
				this.returnValueConstraints, otherMetaData.returnValueConstraints
		);

		// 2 - aggregate parameter metaData. The two method MetaData have the same signature, consequently they
		// have the same number of parameters.
		List<ConstrainedParameter> mergedParameterMetaData = newArrayList();
		for ( ConstrainedParameter oneParameterMetaData : getAllParameterMetaData() ) {
			mergedParameterMetaData.add(
					oneParameterMetaData.merge(
							otherMetaData.getParameterMetaData( oneParameterMetaData.getLocation().getParameterIndex() )
					)
			);
		}
		return new ConstrainedMethod(
				location.getMethod(),
				mergedParameterMetaData,
				mergedReturnValueConstraints,
				isCascading
		);
	}

	@Override
	public String toString() {
		return "ConstrainedMethod [location=" + location + ", parameterMetaData="
				+ parameterMetaData + ", returnValueConstraints="
				+ returnValueConstraints + ", isCascading=" + isCascading
				+ ", hasParameterConstraints=" + hasParameterConstraints + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ( hasParameterConstraints ? 1231 : 1237 );
		result = prime * result + ( isCascading ? 1231 : 1237 );
		result = prime * result
				+ ( ( location == null ) ? 0 : location.hashCode() );
		result = prime
				* result
				+ ( ( parameterMetaData == null ) ? 0 : parameterMetaData
				.hashCode() );
		result = prime
				* result
				+ ( ( returnValueConstraints == null ) ? 0
				: returnValueConstraints.hashCode() );
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
		ConstrainedMethod other = (ConstrainedMethod) obj;
		if ( hasParameterConstraints != other.hasParameterConstraints ) {
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
		if ( parameterMetaData == null ) {
			if ( other.parameterMetaData != null ) {
				return false;
			}
		}
		else if ( !parameterMetaData.equals( other.parameterMetaData ) ) {
			return false;
		}
		if ( returnValueConstraints == null ) {
			if ( other.returnValueConstraints != null ) {
				return false;
			}
		}
		else if ( !returnValueConstraints.equals( other.returnValueConstraints ) ) {
			return false;
		}
		return true;
	}

}
