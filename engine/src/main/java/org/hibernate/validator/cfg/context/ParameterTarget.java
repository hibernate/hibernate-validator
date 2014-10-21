/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.cfg.context;

/**
 * Facet of a constraint mapping creational context which allows to the select a method or constructor parameter to
 * which the next operations shall apply.
 *
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 * @author Gunnar Morling
 */
public interface ParameterTarget {
	/**
	 * Changes the parameter for which added constraints apply. A given parameter may only be configured once.
	 *
	 * @param index The parameter index.
	 *
	 * @return A creational context representing the selected parameter.
	 */
	ParameterConstraintMappingContext parameter(int index);
}
