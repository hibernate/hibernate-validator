/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.cfg.defs;

import jakarta.validation.constraints.Negative;

import org.hibernate.validator.cfg.ConstraintDef;

/**
 * A {@link Negative} constraint definition.
 * @author Gunnar Morling
 */
public class NegativeDef extends ConstraintDef<NegativeDef, Negative> {

	public NegativeDef() {
		super( Negative.class );
	}
}
