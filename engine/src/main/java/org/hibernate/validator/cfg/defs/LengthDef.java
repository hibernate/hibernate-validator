/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */

package org.hibernate.validator.cfg.defs;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.Length;

/**
 * A {@link Length} constraint definition.
 * @author Hardy Ferentschik
 */
public class LengthDef extends ConstraintDef<LengthDef, Length> {

	public LengthDef() {
		super( Length.class );
	}

	public LengthDef min(int min) {
		addParameter( "min", min );
		return this;
	}

	public LengthDef max(int max) {
		addParameter( "max", max );
		return this;
	}
}
