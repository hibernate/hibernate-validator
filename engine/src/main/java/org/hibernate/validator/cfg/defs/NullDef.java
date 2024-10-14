/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.cfg.defs;

import jakarta.validation.constraints.Null;

import org.hibernate.validator.cfg.ConstraintDef;

/**
 * A {@link Null} constraint definition.
 *
 * @author Hardy Ferentschik
 */
public class NullDef extends ConstraintDef<NullDef, Null> {

	public NullDef() {
		super( Null.class );
	}
}
