/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.cfg.defs;

import jakarta.validation.constraints.PositiveOrZero;

import org.hibernate.validator.cfg.ConstraintDef;

/**
 * @author Gunnar Morling
 */
public class PositiveOrZeroDef extends ConstraintDef<PositiveOrZeroDef, PositiveOrZero> {

	public PositiveOrZeroDef() {
		super( PositiveOrZero.class );
	}
}
