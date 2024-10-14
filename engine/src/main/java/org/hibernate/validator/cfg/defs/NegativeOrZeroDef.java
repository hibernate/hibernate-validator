/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.cfg.defs;

import jakarta.validation.constraints.NegativeOrZero;

import org.hibernate.validator.cfg.ConstraintDef;

/**
 * A {@link NegativeOrZero} constraint definition.
 *
 * @author Gunnar Morling
 */
public class NegativeOrZeroDef extends ConstraintDef<NegativeOrZeroDef, NegativeOrZero> {

	public NegativeOrZeroDef() {
		super( NegativeOrZero.class );
	}
}
