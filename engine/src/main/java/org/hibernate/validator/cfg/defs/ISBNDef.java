/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.cfg.defs;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.ISBN;

/**
 * An {@link ISBN} constraint definition.
 * @author Marko Bekhta
 * @since 6.0.6
 */
public class ISBNDef extends ConstraintDef<ISBNDef, ISBN> {

	public ISBNDef() {
		super( ISBN.class );
	}

	public ISBNDef type(ISBN.Type type) {
		addParameter( "type", type );
		return this;
	}
}
