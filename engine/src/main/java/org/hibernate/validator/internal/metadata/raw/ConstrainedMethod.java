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
package org.hibernate.validator.internal.metadata.raw;

import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.location.MethodConstraintLocation;
import org.hibernate.validator.internal.util.ReflectionHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.util.privilegedactions.SetAccessibility;

/**
 * Represents a method of a Java type and all its associated meta-data relevant
 * in the context of bean validation, for instance the constraints at it's
 * parameters or return value.
 *
 * @author Gunnar Morling
 */
public class ConstrainedMethod extends AbstractConstrainedElement {

	private static final Log log = LoggerFactory.make();

	/**
	 * Constrained-related meta data for this method's parameters.
	 */
	private final List<ConstrainedParameter> parameterMetaData;

	private final boolean hasParameterConstraints;

	/**
	 * Creates a new method meta data object for a parameter-less method.
	 *
	 * @param source The source of meta data.
	 * @param location The location of the represented method.
	 * @param returnValueConstraints The return value constraints of the represented method, if
	 * any.
	 * @param isCascading Whether a cascaded validation of the represented method's
	 * return value shall be performed or not.
	 */
	public ConstrainedMethod(
			ConfigurationSource source,
			MethodConstraintLocation location,
			Set<MetaConstraint<?>> returnValueConstraints,
			boolean isCascading) {

		this(
				source,
				location,
				Collections.<ConstrainedParameter>emptyList(),
				returnValueConstraints,
				isCascading
		);
	}

	/**
	 * Creates a new method meta data object.
	 *
	 * @param source The source of meta data.
	 * @param location The location of the represented method.
	 * @param parameterMetaData A list with parameter meta data. The length must correspond
	 * with the number of parameters of the represented method. So
	 * this list may be empty (in case of a parameterless method),
	 * but never <code>null</code>.
	 * @param returnValueConstraints The return value constraints of the represented method, if
	 * any.
	 * @param isCascading Whether a cascaded validation of the represented method's
	 * return value shall be performed or not.
	 */
	public ConstrainedMethod(
			ConfigurationSource source,
			MethodConstraintLocation location,
			List<ConstrainedParameter> parameterMetaData,
			Set<MetaConstraint<?>> returnValueConstraints,
			boolean isCascading) {

		super(
				source,
				ConstrainedElementKind.METHOD,
				location,
				returnValueConstraints,
				isCascading
		);

		Method method = location.getMember();

		if ( parameterMetaData.size() != method.getParameterTypes().length ) {
			throw log.getInvalidLengthOfParameterMetaDataListException(
					method,
					method.getParameterTypes().length,
					parameterMetaData.size()
			);
		}

		this.parameterMetaData = Collections.unmodifiableList( parameterMetaData );
		this.hasParameterConstraints = hasParameterConstraints( parameterMetaData );
	}

	private boolean hasParameterConstraints(List<ConstrainedParameter> parameterMetaData) {

		for ( ConstrainedParameter oneParameter : parameterMetaData ) {
			if ( oneParameter.isConstrained() ) {
				return true;
			}
		}

		return false;
	}

	public MethodConstraintLocation getLocation() {
		return (MethodConstraintLocation) super.getLocation();
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
	 * Whether the represented method is constrained or not. This is the case if
	 * it has at least one constrained parameter, at least one parameter marked
	 * for cascaded validation, at least one return value constraint or if the
	 * return value is marked for cascaded validation.
	 *
	 * @return <code>True</code>, if this method is constrained by any means,
	 *         <code>false</code> otherwise.
	 */
	public boolean isConstrained() {

		return super.isConstrained() || hasParameterConstraints;
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
		return ReflectionHelper.isGetterMethod( getLocation().getMember() );
	}

	@Override
	public String toString() {
		return "ConstrainedMethod [location=" + getLocation()
				+ ", parameterMetaData=" + parameterMetaData
				+ ", hasParameterConstraints=" + hasParameterConstraints + "]";
	}

	/**
	 * Runs the given privileged action, using a privileged block if required.
	 * <p>
	 * <b>NOTE:</b> This must never be changed into a publicly available method to avoid execution of arbitrary
	 * privileged actions within HV's protection domain.
	 */
	private static <T> T run(PrivilegedAction<T> action) {
		return System.getSecurityManager() != null ? AccessController.doPrivileged( action ) : action.run();
	}

}
