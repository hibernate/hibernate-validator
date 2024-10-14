/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.cfg.context;

/**
 * Facet of a constraint mapping creational context which allows to select the bean
 * constructor to which the next operations shall apply.
 *
 * @author Gunnar Morling
 */
public interface ConstructorTarget {

	/**
	 * Selects a constructor to which the next operations shall apply.
	 * <p>
	 * Until this method is called constraints apply on class level. After calling this method constraints
	 * apply to the specified constructor.
	 * </p>
	 * <p>
	 * A given constructor may only be configured once.
	 * </p>
	 *
	 * @param parameterTypes The constructor parameter types.
	 *
	 * @return A creational context representing the selected constructor.
	 */
	ConstructorConstraintMappingContext constructor(Class<?>... parameterTypes);
}
