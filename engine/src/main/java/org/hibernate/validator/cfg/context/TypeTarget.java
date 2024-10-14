/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.cfg.context;

/**
 * Facet of a constraint mapping creational context which allows to select the bean
 * type to which the next operations shall apply.
 *
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 * @author Gunnar Morling
 */
public interface TypeTarget {
	/**
	 * Selects the type to which the next operations shall apply. A given type may only be configured once.
	 *
	 * @param <C> The type to select.
	 * @param type The type to select.
	 *
	 * @return A creational context representing the selected type.
	 */
	<C> TypeConstraintMappingContext<C> type(Class<C> type);
}
