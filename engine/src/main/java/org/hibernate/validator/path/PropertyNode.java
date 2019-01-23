/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.path;

/**
 * Node representing a property, providing Hibernate Validator specific functionality.
 *
 * @author Gunnar Morling
 */
public interface PropertyNode extends javax.validation.Path.PropertyNode {

	/**
	 * @return Returns the value of the bean property represented by this node.
	 */
	Object getValue();
}
