/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.metadata.aggregated;

import java.lang.reflect.Type;
import java.util.List;

import jakarta.validation.ElementKind;
import jakarta.validation.metadata.ElementDescriptor;

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
	 * @return {@code true}if this object is marked for cascaded validation, {@code false} otherwise.
	 */
	boolean isCascading();

	/**
	 * Whether this meta data object is constrained by any means or not.
	 *
	 * @return {@code true} if this object is marked for cascaded validation or has any constraints, {@code false} otherwise.
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

}
