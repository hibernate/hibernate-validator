/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.cfg.defs;

import jakarta.validation.constraints.DecimalMin;

import org.hibernate.validator.cfg.ConstraintDef;

/**
 * A {@link DecimalMin} constraint definition.
 *
 * @author Hardy Ferentschik
 */
public class DecimalMinDef extends ConstraintDef<DecimalMinDef, DecimalMin> {

	public DecimalMinDef() {
		super( DecimalMin.class );
	}

	public DecimalMinDef value(String min) {
		addParameter( "value", min );
		return this;
	}

	public DecimalMinDef inclusive(boolean inclusive) {
		addParameter( "inclusive", inclusive );
		return this;
	}
}
