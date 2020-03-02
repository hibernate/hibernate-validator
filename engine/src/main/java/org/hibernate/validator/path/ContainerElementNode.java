/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
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
