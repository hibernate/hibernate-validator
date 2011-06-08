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

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.hibernate.validator.util.CollectionHelper.newArrayList;

/**
 * Represents a method of a Java type and all its associated meta-data relevant
 * in the context of bean validation, for instance the constraints at it's
 * parameters or return value.
 *
 * @author Gunnar Morling
 */
public class MethodMetaData implements Iterable<MethodMetaConstraint<?>> {

	private final Method method;

	/**
	 * Constrained-related meta data for this method's parameters.
	 */
	private final List<ParameterMetaData> parameterMetaData;

	private final List<MethodMetaConstraint<?>> returnValueConstraints;

	private final boolean isCascading;

	private final boolean hasParameterConstraints;

	public MethodMetaData(
			Method method,
			List<MethodMetaConstraint<?>> constraints,
			boolean isCascading) {

		this( method, Collections.<ParameterMetaData>emptyList(), constraints, isCascading );
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
	public MethodMetaData(
			Method method,
			List<ParameterMetaData> parameterMetaData,
			List<MethodMetaConstraint<?>> returnValueConstraints,
			boolean isCascading) {

		if ( parameterMetaData.size() != method.getParameterTypes().length ) {
			throw new IllegalArgumentException(
					String.format(
							"Method %s has %s parameters, but the passed list of parameter meta data has a size of %s.",
							method, method.getParameterTypes().length, parameterMetaData.size()
					)
			);
		}

		this.method = method;
		this.parameterMetaData = Collections.unmodifiableList( parameterMetaData );
		this.returnValueConstraints = Collections.unmodifiableList( returnValueConstraints );
		this.isCascading = isCascading;
		this.hasParameterConstraints = hasParameterConstraints( parameterMetaData );
	}

	private boolean hasParameterConstraints(List<ParameterMetaData> parameterMetaData) {

		for ( ParameterMetaData oneParameter : parameterMetaData ) {
			if ( oneParameter.isConstrained() ) {
				return true;
			}
		}

		return false;
	}

	/**
	 * The method represented by this meta data object.
	 *
	 * @return The method represented by this meta data object.
	 */
	public Method getMethod() {
		return method;
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
	public ParameterMetaData getParameterMetaData(int parameterIndex) {

		if ( parameterIndex < 0 || parameterIndex > parameterMetaData.size() - 1 ) {
			throw new IllegalArgumentException( "Method " + method + " doesn't have a parameter with index " + parameterIndex );
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
	public List<ParameterMetaData> getAllParameterMetaData() {
		return parameterMetaData;
	}

	/**
	 * An iterator with the return value constraints of the represented method.
	 */
	public Iterator<MethodMetaConstraint<?>> iterator() {
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

	public MethodMetaData merge(MethodMetaData otherMetaData) {

		boolean isCascading = isCascading() || otherMetaData.isCascading();

		// 1 - aggregate return value constraints
		List<MethodMetaConstraint<?>> mergedReturnValueConstraints = newArrayList(
				this, otherMetaData
		);

		// 2 - aggregate parameter metaData. The two method MetaData have the same signature, consequently they
		// have the same number of parameters.
		List<ParameterMetaData> mergedParameterMetaData = newArrayList();
		for ( ParameterMetaData oneParameterMetaData : getAllParameterMetaData() ) {
			mergedParameterMetaData.add(
					oneParameterMetaData.merge(
							otherMetaData.getParameterMetaData( oneParameterMetaData.getIndex() )
					)
			);
		}
		return new MethodMetaData( method, mergedParameterMetaData, mergedReturnValueConstraints, isCascading );
	}

	@Override
	public String toString() {
		return "MethodMetaData [method=" + method + ", parameterMetaData="
				+ parameterMetaData + ", constraints=" + returnValueConstraints
				+ ", isCascading=" + isCascading + ", hasParameterConstraints="
				+ hasParameterConstraints + "]";
	}

	/**
	 * It is expected that there is exactly one instance of this type for a
	 * given method in a type system. This method is therefore only based on the
	 * hash code defined by the represented {@link Method}.
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ( ( method == null ) ? 0 : method.hashCode() );
		return result;
	}

	/**
	 * It is expected that there is exactly one instance of this type for a
	 * given method in a type system. This method is therefore only based on
	 * the equality as defined by the represented {@link Method}.
	 */
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
		MethodMetaData other = (MethodMetaData) obj;
		if ( method == null ) {
			if ( other.method != null ) {
				return false;
			}
		}
		else if ( !method.equals( other.method ) ) {
			return false;
		}
		return true;
	}
}
