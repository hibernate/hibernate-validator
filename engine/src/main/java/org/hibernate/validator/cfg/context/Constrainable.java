/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.cfg.context;

import org.hibernate.validator.cfg.ConstraintDef;

/**
 * Facet of a constraint mapping creational context which allows to place
 * constraints on the underlying element.
 *
 * @author Gunnar Morling
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 */
public interface Constrainable<C extends Constrainable<C>> {
	/**
	 * Adds a new constraint.
	 *
	 * @param definition The constraint to add.
	 *
	 * @return The current creational context following the method chaining pattern.
	 */
	C constraint(ConstraintDef<?, ?> definition);
}
