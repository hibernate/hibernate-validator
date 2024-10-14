/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.path;

/**
 * Node representing a property, providing Hibernate Validator specific functionality.
 *
 * @author Gunnar Morling
 */
public interface PropertyNode extends jakarta.validation.Path.PropertyNode {

	/**
	 * @return Returns the value of the bean property represented by this node.
	 */
	Object getValue();
}
