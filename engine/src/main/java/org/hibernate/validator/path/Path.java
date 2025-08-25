/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.path;

import org.hibernate.validator.Incubating;

/**
 * An extended representation of the validation path, provides Hibernate Validator specific functionality.
 */
@Incubating
public interface Path extends jakarta.validation.Path {

	/**
	 * @return The leaf node of the current path.
	 * This is a shortcut method to iterating over the path till the last node.
	 */
	jakarta.validation.Path.Node getLeafNode();

}
