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
package org.hibernate.validator.metadata.constrained;

import java.util.Set;

import org.hibernate.validator.metadata.MetaConstraint;
import org.hibernate.validator.metadata.location.ConstraintLocation;

/**
 * Represents a (potentially) constrained element such as a type or field. Such
 * an element has a set of {@link MetaConstraints} and can be marked for a
 * cascaded validation.
 *
 * @author Gunnar Morling
 */
public interface ConstrainedElement extends Iterable<MetaConstraint<?>> {

	/**
	 * The kind of a {@link ConstrainedElement}. Can be used to determine an
	 * element's type when traversing over a collection of constrained elements.
	 *
	 * @author Gunnar Morling
	 */
	public enum ConstrainedElementKind {
		TYPE, FIELD, METHOD, PARAMETER;
	}

	public enum ConfigurationSource {
		ANNOTATION, XML, API
	}

	ConstrainedElementKind getConstrainedElementKind();

	ConstraintLocation getLocation();

	Set<MetaConstraint<?>> getConstraints();

	/**
	 * Whether cascading validation for the represented element shall be
	 * performed or not.
	 *
	 * @return <code>True</code>, if cascading validation for the represented
	 *         element shall be performed, <code>false</code> otherwise.
	 */
	boolean isCascading();

	/**
	 * Whether this element is constrained or not. This is the case, if this
	 * element has at least one constraint or a cascaded validation shall be
	 * performed for it.
	 *
	 * @return <code>True</code>, if this element is constrained,
	 *         <code>false</code> otherwise.
	 */
	boolean isConstrained();

}
