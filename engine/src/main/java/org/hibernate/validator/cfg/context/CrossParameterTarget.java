/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.cfg.context;

/**
 * Facet of a constraint mapping creational context which allows to the select the cross-parameter element of a method
 * or constructor as target of the next operations.
 *
 * @author Gunnar Morling
 */
public interface CrossParameterTarget {

	/**
	 * Selects the cross-parameter element of a method or constructor as target for the next constraint declaration
	 * operations. May only be configured once for a given method or constructor.
	 *
	 * @return A creational context representing the cross-parameter element of the current method or constructor.
	 */
	CrossParameterConstraintMappingContext crossParameter();
}
