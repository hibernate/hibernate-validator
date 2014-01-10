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
package org.hibernate.validator.internal.metadata.aggregated;

import java.lang.reflect.Type;
import java.util.List;
import javax.validation.ElementKind;
import javax.validation.metadata.ElementDescriptor;

import org.hibernate.validator.internal.metadata.core.MetaConstraint;

/**
 * An aggregated view of the constraint related meta data for a given bean/type
 * element and all the elements in the inheritance hierarchy which it overrides
 * or implements.
 *
 * @author Gunnar Morling
 */
public interface ConstraintMetaData extends Iterable<MetaConstraint<?>> {

	/**
	 * Returns the name of this meta data object.
	 *
	 * @return This meta data object's name.
	 */
	String getName();

	/**
	 * Returns the data type of this meta data object, e.g. the type of a bean property or the
	 * return type of a method.
	 *
	 * @return This meta data object's type.
	 */
	Type getType();

	/**
	 * Returns the {@link ElementKind kind} of this meta data object.
	 *
	 * @return The {@link ElementKind kind} of this meta data object.
	 */
	ElementKind getKind();

	/**
	 * Whether this meta data object is marked for cascaded validation or not.
	 *
	 * @return <code>True</code> if this object is marked for cascaded validation, <code>false</code> otherwise.
	 */
	boolean isCascading();

	/**
	 * Whether this meta data object is constrained by any means or not.
	 *
	 * @return <code>True</code> if this object is marked for cascaded validation or has any constraints, <code>false</code> otherwise.
	 */
	boolean isConstrained();

	/**
	 * Returns this meta data object's corresponding representation in the
	 * descriptor model.
	 *
	 * @param defaultGroupSequenceRedefined Whether the bean hosting the represented element has a
	 * redefined default group sequence or not.
	 * @param defaultGroupSequence The default group sequence of the bean hosting the represented
	 * element.
	 *
	 * @return This meta data object's corresponding descriptor model
	 *         representation. Implementations should return a specific sub type
	 *         of {@link ElementDescriptor}.
	 */
	ElementDescriptor asDescriptor(boolean defaultGroupSequenceRedefined, List<Class<?>> defaultGroupSequence);

	/**
	 * Whether the value to be validated needs to be unwrapped (which is the case if this element is annotated with
	 * {@code @UnwrapValidatedValue}) or not.
	 *
	 * @return {@code true} if the value to be validated needs to unwrapped, {@code false} otherwise.
	 */
	boolean requiresUnwrapping();
}
