/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.metadata.facets;

/**
 * Provides a unified view on validatable elements of all kinds, be it Java
 * beans, the arguments passed to a method or the value returned from a method.
 * Allows a unified handling of these elements in the validation routine.
 *
 * @author Gunnar Morling
 */
public interface Validatable {

	/**
	 * Returns the cascaded elements of this validatable, e.g. the properties of
	 * a bean or the parameters of a method annotated with {@code @Valid}.
	 *
	 * @return The cascaded elements of this validatable.
	 */
	Iterable<Cascadable> getCascadables();

	/**
	 * Returns {@code true} if this validatable has at least one cascadable element, {@code false} otherwise.
	 */
	boolean hasCascadables();
}
