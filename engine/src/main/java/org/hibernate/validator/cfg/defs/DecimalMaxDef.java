/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */

package org.hibernate.validator.cfg.defs;

import jakarta.validation.constraints.DecimalMax;

import org.hibernate.validator.cfg.ConstraintDef;

/**
 * A {@link DecimalMax} constraint definition.
 * @author Hardy Ferentschik
 */
public class DecimalMaxDef extends ConstraintDef<DecimalMaxDef, DecimalMax> {

	public DecimalMaxDef() {
		super( DecimalMax.class );
	}

	public DecimalMaxDef value(String max) {
		addParameter( "value", max );
		return this;
	}

	public DecimalMaxDef inclusive(boolean inclusive) {
		addParameter( "inclusive", inclusive );
		return this;
	}
}
