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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Represents a method of a Java type and all its associated meta-data relevant
 * in the context of bean validation, for instance the constraints at it's
 * parameters or return value.
 *
 * @author Gunnar Morling
 */
public class MethodMetaData implements Iterable<BeanMetaConstraint<?, ? extends Annotation>> {

	private final Method method;

	private final Map<Integer, ParameterMetaData> parameterMetaData;

	private final List<BeanMetaConstraint<?, ? extends Annotation>> constraints;

	private final boolean isCascading;

	private final boolean hasParameterConstraints;

	public MethodMetaData(
			Method method,
			List<BeanMetaConstraint<?, ? extends Annotation>> constraints,
			boolean isCascading) {

		this( method, Collections.<Integer, ParameterMetaData>emptyMap(), constraints, isCascading );
	}

	public MethodMetaData(
			Method method,
			Map<Integer, ParameterMetaData> parameterMetaData,
			List<BeanMetaConstraint<?, ? extends Annotation>> constraints,
			boolean isCascading) {

		this.method = method;
		this.parameterMetaData = parameterMetaData;
		this.constraints = constraints;
		this.isCascading = isCascading;

		boolean foundParameterConstraint = false;

		for ( Entry<Integer, ParameterMetaData> oneParameter : parameterMetaData.entrySet() ) {
			if ( oneParameter.getValue().isCascading() || oneParameter.getValue().iterator().hasNext() ) {
				foundParameterConstraint = true;
				break;
			}
		}

		this.hasParameterConstraints = foundParameterConstraint;
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
	 * @param parameterIndex The index in this method's parameter array of the parameter of interest.
	 *
	 * @return Meta data for the specified parameter. Will never be <code>null</code.>
	 *
	 * @throws IllegalArgumentException In case this method doesn't have a parameter with the specified index.
	 */
	public ParameterMetaData getParameterMetaData(int parameterIndex) {

		ParameterMetaData theValue = parameterMetaData.get( parameterIndex );

		if ( theValue == null ) {
			throw new IllegalArgumentException( "Method " + method + " doesn't have a parameter with index " + parameterIndex );
		}

		return theValue;
	}

	/**
	 * An iterator with the return value constraints of the represented method.
	 */
	public Iterator<BeanMetaConstraint<?, ? extends Annotation>> iterator() {
		return constraints.iterator();
	}

	/**
	 * Whether cascading validation for the represented method shall be performed or not.
	 *
	 * @return <code>True</code>, if cascading validation for the represented method shall be performed, <code>false</code> otherwise.
	 */
	public boolean isCascading() {
		return isCascading;
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

	@Override
	public String toString() {
		return "MethodMetaData [method=" + method + ", parameterMetaData="
				+ parameterMetaData + ", constraints=" + constraints
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
