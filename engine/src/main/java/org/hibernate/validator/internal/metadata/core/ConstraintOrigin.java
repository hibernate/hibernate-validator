/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.core;

/**
 * Visibility looked at when discovering constraints.
 *
 * @author Hardy Ferentschik
 */
public enum ConstraintOrigin {
	/**
	 * Constraint is defined on the root class
	 */
	DEFINED_LOCALLY,

	/**
	 * Constraint is defined in a super-class or interface of the root class.
	 */
	DEFINED_IN_HIERARCHY
}
