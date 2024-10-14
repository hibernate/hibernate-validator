/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
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
