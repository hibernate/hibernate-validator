/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.cfg.defs;

import jakarta.validation.constraints.AssertTrue;

import org.hibernate.validator.cfg.ConstraintDef;

/**
 * An {@link AssertTrue} constraint definition.
 *
 * @author Hardy Ferentschik
 */
public class AssertTrueDef extends ConstraintDef<AssertTrueDef, AssertTrue> {

	public AssertTrueDef() {
		super( AssertTrue.class );
	}
}
