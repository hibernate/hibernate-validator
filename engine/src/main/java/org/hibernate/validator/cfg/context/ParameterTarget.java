/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.cfg.context;

/**
 * Facet of a constraint mapping creational context which allows to select a method or constructor parameter to
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
