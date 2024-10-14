/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.cfg.defs;

import jakarta.validation.constraints.Positive;

import org.hibernate.validator.cfg.ConstraintDef;

/**
 * A {@link Positive} constraint definition.
 * @author Gunnar Morling
 */
public class PositiveDef extends ConstraintDef<PositiveDef, Positive> {

	public PositiveDef() {
		super( Positive.class );
	}
}
