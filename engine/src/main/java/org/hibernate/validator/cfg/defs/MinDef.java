/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.cfg.defs;

import jakarta.validation.constraints.Min;

import org.hibernate.validator.cfg.ConstraintDef;

/**
 * A {@link Min} constraint definition.
 *
 * @author Hardy Ferentschik
 */
public class MinDef extends ConstraintDef<MinDef, Min> {

	public MinDef() {
		super( Min.class );
	}

	public MinDef value(long min) {
		addParameter( "value", min );
		return this;
	}
}
