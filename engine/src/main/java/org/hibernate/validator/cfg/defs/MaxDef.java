/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.cfg.defs;

import jakarta.validation.constraints.Max;

import org.hibernate.validator.cfg.ConstraintDef;

/**
 * A {@link Max} constraint definition.
 *
 * @author Hardy Ferentschik
 */
public class MaxDef extends ConstraintDef<MaxDef, Max> {

	public MaxDef() {
		super( Max.class );
	}

	public MaxDef value(long max) {
		addParameter( "value", max );
		return this;
	}
}
