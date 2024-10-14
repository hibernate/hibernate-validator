/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.cfg.defs;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.Range;

/**
 * A {@link Range} constraint definition.
 *
 * @author Hardy Ferentschik
 */
public class RangeDef extends ConstraintDef<RangeDef, Range> {

	public RangeDef() {
		super( Range.class );
	}

	public RangeDef min(long min) {
		addParameter( "min", min );
		return this;
	}

	public RangeDef max(long max) {
		addParameter( "max", max );
		return this;
	}
}
