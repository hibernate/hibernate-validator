/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */

// $Id:$
package org.hibernate.validator.constraints;

/**
 * The Enum {@code CompositionType} which is used as argument to the annotation {@code ConstraintComposition}.
 */
public enum CompositionType {
	/**
	 * Used to indicate the disjunction of all constraints it is applied to.
	 */
	OR,

	/**
	 * Used to indicate the conjunction of all the constraints it is applied to.
	 */
	AND,

	/**
	 * ALL_FALSE is a generalisation of the usual NOT operator, which is applied to
	 * a list of conditions rather than just one element.
	 * When the annotation it is used on is composed of a single constraint annotation, then it is equivalent to NOT.
	 */
	ALL_FALSE
}


