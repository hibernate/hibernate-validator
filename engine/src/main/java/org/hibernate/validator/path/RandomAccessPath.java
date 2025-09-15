/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.path;

import java.util.RandomAccess;

import org.hibernate.validator.Incubating;

/**
 * An extended representation of the validation path, provides Hibernate Validator specific functionality.
 * Represents a path with access to the nodes by their index.
 *
 * @since 9.1
 */
@Incubating
public interface RandomAccessPath extends Path, RandomAccess {

	/**
	 * @return The first node in the path, i.e. {@code path.iterator().next()}.
	 */
	jakarta.validation.Path.Node getRootNode();

	/**
	 * @param index The index of the node to return.
	 * @return The node in the path for a given index.
	 * @throws IndexOutOfBoundsException if the index is out of range, i.e. {@code index < 0 || index >= length() }
	 */
	jakarta.validation.Path.Node getNode(int index);

	/**
	 * @return The length of the path, i.e. the number of nodes this path contains.
	 */
	int length();
}
