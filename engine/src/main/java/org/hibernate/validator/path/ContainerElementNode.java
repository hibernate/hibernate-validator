/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.path;

/**
 * Node representing a container element, providing Hibernate Validator specific functionality.
 *
 * @author Guillaume Smet
 */
public interface ContainerElementNode extends jakarta.validation.Path.ContainerElementNode {

	/**
	 * @return Returns the value of the container element represented by this node.
	 */
	Object getValue();
}
